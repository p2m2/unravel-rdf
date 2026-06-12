package fr.inrae.metabohub.semantic_web

import fr.inrae.metabohub.semantic_web.configuration.OptionPickler
import fr.inrae.metabohub.semantic_web.exception._
import fr.inrae.metabohub.semantic_web.event._
import fr.inrae.metabohub.semantic_web.node._
import fr.inrae.metabohub.semantic_web.rdf.{Var, SparqlDefinition, URI}
import fr.inrae.metabohub.semantic_web.sparql.QueryResult
import fr.inrae.metabohub.semantic_web.strategy._
import wvlet.log.Logger.rootLogger.{debug, trace}

import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success, Try}


object UnravelQuery {
  implicit val rw: OptionPickler.ReadWriter[UnravelQuery] = OptionPickler.macroRW
}

case class UnravelQuery(sw : UnravelSession = UnravelSession())
    extends Subscriber[UnravelRequestEvent,StrategyRequest]
{
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  def notify(pub: StrategyRequest, event: UnravelRequestEvent): Unit = {
    notify(event)
  }

  private val _prom_raw: Promise[ujson.Value] = Promise[ujson.Value]()
  val raw: Future[ujson.Value] =  _prom_raw.future
  var currentRequestEvent: String = UnravelStateRequestEvent.START.toString

  private var countEvent: Int = 1

  private var _progressionCallBack = Seq[Double => Unit]()

  def progression(  callBack  : Double => Unit  ): UnravelQuery = {
    _progressionCallBack = _progressionCallBack :+ callBack
    this
  }

  private var _requestEventCallBack = Seq[String => Unit]()

  def requestEvent(callBack  : String => Unit  ): UnravelQuery = {
    _requestEventCallBack = _requestEventCallBack :+ callBack
    this
  }

  def notify(event: UnravelRequestEvent): Unit = {
    currentRequestEvent = event.state.toString
    countEvent = countEvent + 1

    _progressionCallBack.foreach (f => f(UnravelStateRequestEvent.getPercentProgression(event.state)))

    _requestEventCallBack.foreach(f => f(currentRequestEvent))
  }

  def abort(): Unit = {

    /*
      http request should be cancelled
    * SWResults should be a publish[DiscoveryCancelEvent] => SW/QueryManager/HttpRequest => Subscriber[DiscoveryCancelEvent,SWResults]
    */
    currentRequestEvent = UnravelStateRequestEvent.ABORTED_BY_THE_USER.toString

    _prom_raw failure UnravelException("aborted by the user.")
  }


  private def process_datatype(
  root: Root,
  qr: QueryResult,
  datatypeNode: DatatypeNode,
  lUris: Seq[SparqlDefinition]
): Seq[Future[Unit]] = {
  debug(" -- process_datatype --")

  val labelProperty = datatypeNode.property.reference()

  lUris
    .grouped(sw.config.settings.sizeBatchProcessing)
    .toList
    .map { lSubUris =>
      trace("datatype batch ===========================")
      trace("datatypeNode      : " + datatypeNode.toString)
      trace("labelProperty     : " + labelProperty)
      trace("all uris batch    : " + lSubUris.toString)

      val onlyUris = lSubUris.collect { case uri: URI => uri }

      trace("filtered URI only : " + onlyUris.toString)

      val tx = UnravelSession(sw.config)
        .prefixes(root.getPrefixes)
        .something("val_uri",
          _.setList(onlyUris).addNodeAndRestoreFocus(datatypeNode.property)
        ).select(List("val_uri", labelProperty)).commit()

      tx.raw.map { json =>
        trace("datatype raw json =======================")
        trace(json.toString)
        val bindings =
          try {
            json("results")("bindings").arr.toSeq
          } catch {
            case e: Throwable =>
              trace("datatype bindings extraction error: " + e.toString)
              Seq.empty
          }

        val datatypeMap = bindings.flatMap { rec =>
          val maybeUri =
            try {
              Some(rec("val_uri")("value").value.toString)
            } catch {
              case _: Throwable =>
                trace("missing val_uri in record: " + rec.toString)
                None
            }

          val maybeValue =
            try {
              Some(rec(labelProperty))
            } catch {
              case _: Throwable =>
                trace("missing property [" + labelProperty + "] in record: " + rec.toString)
                None
            }

          (maybeUri, maybeValue) match {
            case (Some(uri), Some(value)) =>
              trace(s"datatype pair -> $uri => $value")
              Some(uri -> value)
            case _ =>
              None
          }
        }.toMap

        trace("datatypeMap final ======================")
        trace(datatypeMap.toString)

        qr.setDatatype(labelProperty, datatypeMap)
      }.recover { case e =>
        trace("datatype request failed =================")
        trace(e.toString)
        throw e
      }
    }
}

  def commit() : UnravelQuery = {
    notify(UnravelRequestEvent(UnravelStateRequestEvent.START))

    val lSelectedVariable : Seq[Var] = sw.rootNode.getChild(Projection(List(),"")).lastOption match {
      case Some(proj) =>proj.variables.distinct
      case None =>
        notify(UnravelRequestEvent(UnravelStateRequestEvent.ERROR_REQUEST_DEFINITION))
        throw UnravelException("projection/selected required variables are not defined.")
    }

    val lDatatype: Seq[DatatypeNode] =
      sw.rootNode.getChild[DatatypeNode](DatatypeNode("",SubjectOf("", URI(""),Var("")),"unk"))
        .filter(ld => lSelectedVariable.map(_.name).contains(ld.property.reference()))

    if ( lDatatype.count(datatypeNode => lSelectedVariable.map(_.name).contains(datatypeNode.refNode)) != lDatatype.length )
      {
        notify(UnravelRequestEvent(UnravelStateRequestEvent.ERROR_REQUEST_DEFINITION))
        throw UnravelException("The user have to select node of interest before setup a desired datatype ["+lDatatype.map(d=>d.idRef + "->"+d.refNode).mkString(" ,")+"]")
      }

    Try(StrategyRequestBuilder.build(sw.config)) match {
      case Failure(e) => _prom_raw failure e
      case Success(driver) =>
        
        driver.subscribe(this.asInstanceOf[Subscriber[UnravelRequestEvent,Publisher[UnravelRequestEvent]]])
        driver.execute(this)
          /* manage datatype decoration */
          .map((qr: QueryResult) => {
            notify(UnravelRequestEvent(UnravelStateRequestEvent.DATATYPE_BUILD))
            /* create an empty set of datatype */
            qr.json("results").update("datatype", ujson.Obj())
            trace(qr.json)
            /* manage datatype */
            trace("  lDatatype ====> " + lDatatype.toString())
            Future.sequence(lDatatype.map(datatypeNode => {
              trace("datatype node:" + datatypeNode)

              sw.rootNode.getRdfNode(datatypeNode.refNode) match {
                case Some(_) =>

                  /* find uris value inside results to decorate */
                  val lUris: Seq[SparqlDefinition] =
                    try {
                      qr.getValues(datatypeNode.refNode)
                    } catch {
                      case _: Throwable => List()
                    }
                    trace("ICI1:"+qr.toString)
                  Future.sequence(process_datatype(sw.rootNode,qr, datatypeNode, lUris))
                case None =>
                  trace("ICI2:"+qr.toString)
                  Future {}
              }
            })) onComplete {
              case Success(_) =>
                notify(UnravelRequestEvent(UnravelStateRequestEvent.DATATYPE_DONE))
                _prom_raw success qr.json
                notify(UnravelRequestEvent(UnravelStateRequestEvent.REQUEST_DONE))
              case Failure(e) =>
                _prom_raw failure e
            }
          }).recover(exception => {
          _prom_raw failure exception
        })
    }
    this
  }

  case class ProjectionExpressionIncrement(v : String) {
    def manage(n:AggregateNode) : UnravelQuery = {
      // get the Last Projection Node
      sw.rootNode.lSolutionSequenceModifierNode.lastOption match {
        case Some(proj) =>
          sw.copy(fn = Some(proj.idRef)) // on se position sur le noeud Projection des SolutionSequenceModifier
          .addNodeAndRestoreFocus(ProjectionExpression(Var(v),n,v)).transaction
        case None => throw UnravelException(s"Can not find Project node int the RootNode :: $sw")
      }
    }

    def count(lRef : Seq[String],distinct: Boolean=false) : UnravelQuery =
        manage(Count(lRef.map(Var(_)),distinct,sw.getUniqueRef()))
  }

  def aggregate(`var` : String) : ProjectionExpressionIncrement = ProjectionExpressionIncrement(`var`)

  def projection  : UnravelQuery = {
    /* check if a projection exist or create a new one */
    sw.rootNode.getChild(Projection(Seq(),"")).lastOption match {
      case Some(p) => sw.copy(fn = Some(p.idRef)).transaction
      case None => sw.root.addNodeAndRestoreFocus(Projection(Seq(),sw.getUniqueRef())).transaction
    }
  }

  def projection( lRef: Seq[String] )  : UnravelQuery = {
    sw.rootNode.getChild(Projection(Seq(),"")).lastOption match {
      case Some(p) =>
        val listVariable : Seq[Var] = p.variables ++  lRef.map(Var(_))
        sw.copy(fn = Some(p.idRef)).addNodeAndRestoreFocus(
          Projection(listVariable,p.idRef,p.children))
          .transaction
      case None =>
        sw.root.addNodeAndRestoreFocus(Projection(lRef.map(Var(_)),sw.getUniqueRef())).transaction
    }

  }

  def distinct : UnravelQuery = sw.root.addNodeAndRestoreFocus(Distinct(sw.getUniqueRef())).transaction

  def reduced : UnravelQuery = sw.root.addNodeAndRestoreFocus(Reduced(sw.getUniqueRef())).transaction

  def limit( value : Int ) : UnravelQuery = sw.root.addNodeAndRestoreFocus(Limit(value,sw.getUniqueRef())).transaction

  def offset( value : Int ) : UnravelQuery = sw.root.addNodeAndRestoreFocus(Offset(value,sw.getUniqueRef())).transaction

  def orderByAsc( ref: String ) : UnravelQuery =
    sw.refExist(ref).root.addNodeAndRestoreFocus(OrderByAsc(Seq(Var(ref)),sw.getUniqueRef())).transaction

  def orderByAsc( lRef: Seq[String] ) : UnravelQuery = {
    lRef.foreach( sw.refExist )
    sw.root.addNodeAndRestoreFocus(OrderByAsc(lRef.map(Var(_)),sw.getUniqueRef())).transaction
  }

  def orderByDesc( ref: String ) : UnravelQuery =
    sw.refExist(ref).root.addNodeAndRestoreFocus(OrderByDesc(Seq(Var(ref)),sw.getUniqueRef())).transaction

  def orderByDesc( lRef: Seq[String] ) : UnravelQuery = {
    lRef.foreach( sw.refExist )
    sw.root.addNodeAndRestoreFocus(OrderByDesc(lRef.map(Var(_)),sw.getUniqueRef())).transaction
  }
  def getSerializedString : String = OptionPickler.write(this)
  def setSerializedString(query : String) : UnravelQuery = OptionPickler.read[UnravelQuery](query)
  def console : UnravelQuery = sw.console.transaction
}
