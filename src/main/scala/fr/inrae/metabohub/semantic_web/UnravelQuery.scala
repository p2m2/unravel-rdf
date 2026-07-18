// SPDX-FileCopyrightText: 2020-2026 INRAE
// SPDX-License-Identifier: GPL-3.0-or-later

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
  implicit val ec: scala.concurrent.ExecutionContext = scala.scalajs.concurrent.JSExecutionContext.queue

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
      val emptyDatatypeValues: Map[String, ujson.Arr] =
        onlyUris
          .map { uri =>
            uri.localName -> ujson.Arr()
          }
          .toMap

      qr.setDatatype(labelProperty, emptyDatatypeValues)

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

        val datatypeMap = bindings
          .flatMap { rec =>
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
                  trace(
                    "missing property [" + labelProperty + "] in record: " + rec.toString
                  )
                  None
              }

            (maybeUri, maybeValue) match {
              case (Some(uri), Some(value)) =>
                trace(s"datatype pair -> $uri => $value")
                Some(uri -> value)

              case _ =>
                None
            }
          }
          .groupMap(_._1)(_._2)
          .view
          .mapValues(values => ujson.Arr.from(values))
          .toMap

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
  def commit(): UnravelQuery = {
    notify(UnravelRequestEvent(UnravelStateRequestEvent.START))

    val selectedVariables: Seq[Var] =
      sw.rootNode
        .getChild(Projection(List(), ""))
        .lastOption match {
        case Some(projection) =>
          projection.variables.distinct

        case None =>
          notify(
            UnravelRequestEvent(
              UnravelStateRequestEvent.ERROR_REQUEST_DEFINITION
            )
          )

          throw UnravelException(
            "Projection/selected required variables are not defined."
          )
      }

    val selectedVariableNames: Set[String] =
      selectedVariables.map(_.name).toSet

    /*
     * A DatatypeNode is an optional post-processing request.
     * It is activated only when its result reference is requested in select(...).
     */
    val datatypeNodes: Seq[DatatypeNode] =
      sw.rootNode
        .getChild[DatatypeNode](
          DatatypeNode(
            "",
            SubjectOf("", URI(""), Var("")),
            "unk"
          )
        )
        .filter { datatypeNode =>
          selectedVariableNames.contains(
            datatypeNode.property.reference()
          )
        }

    /*
     * A datatype query needs the IRI of its parent resource.
     * The parent variable must therefore be part of the main SELECT projection.
     */
    val datatypesWithMissingResource: Seq[DatatypeNode] =
      datatypeNodes.filter { datatypeNode =>
        !selectedVariableNames.contains(datatypeNode.refNode)
      }

    if (datatypesWithMissingResource.nonEmpty) {
      notify(
        UnravelRequestEvent(
          UnravelStateRequestEvent.ERROR_REQUEST_DEFINITION
        )
      )

      val details =
        datatypesWithMissingResource
          .map { datatypeNode =>
            s"  - datatype '${datatypeNode.property.reference()}' " +
              s"requires resource '?${datatypeNode.refNode}'"
          }
          .mkString("\n")

      val missingResources =
        datatypesWithMissingResource
          .map(_.refNode)
          .distinct
          .map(ref => s"'$ref'")
          .mkString(", ")

      throw UnravelException(
        s"""|Cannot retrieve requested datatype values because their parent
            |resource variable(s) are not selected: $missingResources.
            |
            |$details
            |
            |Add the missing resource variable(s) to select(...).
            |""".stripMargin
      )
    }

    Try(StrategyRequestBuilder.build(sw.config)) match {
      case Failure(error) =>
        if (!_prom_raw.isCompleted) {
          _prom_raw.failure(error)
        }

      case Success(driver) =>
        driver.subscribe(
          this.asInstanceOf[
            Subscriber[
              UnravelRequestEvent,
              Publisher[UnravelRequestEvent]
            ]
          ]
        )

        driver
          .execute(this)
          .flatMap { queryResult =>
            notify(
              UnravelRequestEvent(
                UnravelStateRequestEvent.DATATYPE_BUILD
              )
            )

            queryResult.json("results").update(
              "datatypes",
              ujson.Obj()
            )

            val datatypeRequests: Seq[Future[Unit]] =
              datatypeNodes.flatMap { datatypeNode =>
                sw.rootNode.getRdfNode(datatypeNode.refNode) match {
                  case Some(_) =>
                    val uris =
                      try {
                        queryResult.getValues(datatypeNode.refNode)
                      } catch {
                        case _: Throwable =>
                          Seq.empty
                      }

                    process_datatype(
                      root = sw.rootNode,
                      qr = queryResult,
                      datatypeNode = datatypeNode,
                      lUris = uris
                    )

                  case None =>
                    Seq.empty
                }
              }

            Future.sequence(datatypeRequests).map { _ =>
              queryResult
            }
          }
          .map { queryResult =>
            notify(
              UnravelRequestEvent(
                UnravelStateRequestEvent.DATATYPE_DONE
              )
            )

            if (!_prom_raw.isCompleted) {
              _prom_raw.success(queryResult.json)
            }

            notify(
              UnravelRequestEvent(
                UnravelStateRequestEvent.REQUEST_DONE
              )
            )
          }
          .recover { error =>
            if (!_prom_raw.isCompleted) {
              _prom_raw.failure(error)
            }
          }
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

  def getLimit : Int = sw.root.rootNode.getChild(Limit(-1,"")).lastOption.getOrElse(Limit(-1,"")).value

  def offset( value : Int ) : UnravelQuery = sw.root.addNodeAndRestoreFocus(Offset(value,sw.getUniqueRef())).transaction

  def getOffset : Int = sw.root.rootNode.getChild(Offset(-1,"")).lastOption.getOrElse(Offset(-1,"")).value

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
