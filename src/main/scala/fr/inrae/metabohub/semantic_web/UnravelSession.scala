package fr.inrae.metabohub.semantic_web

import fr.inrae.metabohub.semantic_web.configuration._
import fr.inrae.metabohub.semantic_web.exception._
import fr.inrae.metabohub.semantic_web.node._
import fr.inrae.metabohub.semantic_web.node.pm.{NodeVisitor, RemoveNode}
import fr.inrae.metabohub.semantic_web.rdf._
import fr.inrae.metabohub.semantic_web.sparql.QueryResult
import wvlet.log.Logger
import wvlet.log.Logger.rootLogger._

import java.util.UUID.randomUUID
import scala.concurrent.Future
import io.lemonlabs.uri.{QueryString, Url}

object UnravelSession {

  private val version : String = SWDiscoveryVersionAtBuildTime.version

  implicit val rw: OptionPickler.ReadWriter[UnravelSession] = OptionPickler.macroRW

  info(" --------------------------------------------------" )
  info(" ---- Discovery :"+ UnravelSession.version + "         -----------" )
  info(" --------------------------------------------------" )

}

case class UnravelSession(
                           val config: UnravelConfig=UnravelConfig.init(),
                           val rootNode : Root = Root(),
                           val fn : Option[String] = None)
{
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  val focusNode : String = fn match {
    case Some(v) => v
    case None => rootNode.reference()
  }

  case class FilterIncrement(negation : Boolean = false) {

    def manageFilter(n:Node,forward : Boolean = false) : UnravelSession = focusManagement(n,forward)

    def isLiteral : UnravelSession = manageFilter(
      fr.inrae.metabohub.semantic_web.node.isLiteral(this.negation,getUniqueRef()))
    def isUri : UnravelSession = manageFilter(
      fr.inrae.metabohub.semantic_web.node.isURI(this.negation,getUniqueRef()))
    def isBlank : UnravelSession = manageFilter(
      fr.inrae.metabohub.semantic_web.node.isBlank(this.negation,getUniqueRef()))

    /* strings */
    def regex( pattern : SparqlDefinition, flags : SparqlDefinition="" ) : UnravelSession =
      manageFilter(Regex(pattern,flags,this.negation,getUniqueRef()))
    def contains( string : SparqlDefinition ) : UnravelSession = manageFilter(Contains(string,this.negation,getUniqueRef()))
    def strStarts( string : SparqlDefinition ) : UnravelSession = manageFilter(StrStarts(string,this.negation,getUniqueRef()))
    def strEnds( string : SparqlDefinition ) : UnravelSession = manageFilter(StrEnds(string,this.negation,getUniqueRef()))

    /* numeric */
    def equal( value : SparqlDefinition ) : UnravelSession = manageFilter(Equal(value,this.negation,getUniqueRef()))
    def notEqual( value : SparqlDefinition ) : UnravelSession = manageFilter(NotEqual(value,this.negation,getUniqueRef()))
    def inf( value : SparqlDefinition ) : UnravelSession = manageFilter(Inf(value,this.negation,getUniqueRef()))
    def infEqual( value : SparqlDefinition ) : UnravelSession = manageFilter(InfEqual(value,this.negation,getUniqueRef()))
    def sup( value : SparqlDefinition ) : UnravelSession = manageFilter(Sup(value,this.negation,getUniqueRef()))
    def supEqual( value : SparqlDefinition ) : UnravelSession = manageFilter(SupEqual(value,this.negation,getUniqueRef()))

    def not : FilterIncrement = { FilterIncrement(true) }
  }

  def filter : FilterIncrement = FilterIncrement()

  case class BindIncrement(`var` : String) {
    def manage(n:ExpressionNode,forward : Boolean = true) : UnravelSession =
      // focusManagement(Bind(n,`var`),forward).root.something(`var`).focus(`var`)
      focusManagement(Bind(n,`var`),forward)
    /* primary expression */

    /* String fun */
    def subStr(startingLoc : SparqlDefinition,length : SparqlDefinition ) : UnravelSession = manage(SubStr(startingLoc,length,getUniqueRef()))
    def replace(pattern : SparqlDefinition, replacement : SparqlDefinition, flags : SparqlDefinition="") : UnravelSession =
      manage(Replace(pattern,replacement,flags,getUniqueRef()))

    /* Numeric  fun */
    def abs() : UnravelSession = manage(Abs(getUniqueRef()))
    def round() : UnravelSession = manage(Round(getUniqueRef()))
    def ceil() : UnravelSession = manage(Ceil(getUniqueRef()))
    def floor() : UnravelSession = manage(Floor(getUniqueRef()))
    def rand() : UnravelSession = manage(Rand(getUniqueRef()))

    /* uri fun */
    def datatype() : UnravelSession = manage(Datatype(getUniqueRef()))
    def str() : UnravelSession = manage(Str(QueryVariable(`var`),getUniqueRef()))
  }

  def bind(`var` : String) : BindIncrement = BindIncrement(`var`)

  //private val logger = Logger.of[SWDiscovery]
  // Set the root logger's log level
  Logger.setDefaultLogLevel(config.settings._logLevel)

  /* set focus on root */
  def root: UnravelSession  = UnravelSession(config,rootNode,Some(rootNode.reference()))

  def finder : UnravelSessionHelper = UnravelSessionHelper(this)

  /* configuration */
  def setConfig(newConfig : UnravelConfig) : UnravelSession =
    UnravelSession(newConfig,rootNode,Some(focusNode))

  def getConfig : UnravelConfig = config

  /* get current focus */
  def focus() : String = focusNode

  /* set the current focus on the select node */
  def focus(ref : String) : UnravelSession = {
    if ( ref == focusNode ) {
      UnravelSession(config,rootNode,fn)
    } else if (ref == rootNode.idRef) {
      root
    } else {
      pm.NodeVisitor.getNodeWithRef(ref, rootNode).lastOption match {
        case Some(node) => UnravelSession(config,rootNode,Some(node.reference()))
        case None => throw UnravelException(s"$ref does not exist.")
      }
    }
  }

  def refExist(ref:String) : UnravelSession = {

    pm.NodeVisitor.getNodeWithRef(ref, rootNode).lastOption match {
      case Some(_) => UnravelSession(config,rootNode,Some(focusNode))
      case None => throw UnravelException(s"$ref does not exist.")
    }
  }

  def prefix(short : String, long : IRI ) : UnravelSession = UnravelSession(config,rootNode.addPrefix(short , long ),Some(focusNode))

  def directive(directive : String) : UnravelSession = UnravelSession(config,rootNode.addDirective(directive),Some(focusNode))

  def prefixes( lPrefixes : Map[String,IRI] ) : UnravelSession =
    (lPrefixes map {case (key, value) => prefix(key, value)
    }).toSeq match {
      case l if l.length>0 => l(l.length-1)
      case _ => this
    }

  def getPrefix(short: String) : IRI = rootNode.getPrefix(short)

  def getPrefixes() : Map[String,IRI] = rootNode.getPrefixes

  def graph(graph : IRI) : UnravelSession = UnravelSession(config,rootNode.addDefaultGraph(graph),Some(focusNode))

  def namedGraph(graph : IRI ) : UnravelSession = UnravelSession(config,rootNode.addNamedGraph(graph),Some(focusNode))

  def checkQueryVariable(term : SparqlDefinition): UnravelSession = {
    /* Check if QueryVariable is referenced with Element.
     *  add a Something element otherwise */
    term match {
        case qv : QueryVariable if NodeVisitor.getNodeWithRef(qv.name,rootNode).length == 0  =>
          UnravelSession(config,rootNode.addChildren(rootNode.reference(),Something(qv.name)),Some(focusNode))
        case _ => this
      }
  }

  def focusManagement(n : Node, forward: Boolean = true) : UnravelSession = {
    // get all node
    val current = rootNode.getChild[Node](rootNode.asInstanceOf[Node]).filter( _.idRef == focusNode )

    if ( current.lastOption.exists(_.accept(n))) {
      val newRootNode = rootNode.addChildren(focusNode,n)
      /* current node is the focusNode */
      if (forward) {
        UnravelSession(config,newRootNode,Some(n.reference()))
      }  else {
        UnravelSession(config,newRootNode,Some(focusNode))
      }
    } else {
        throw UnravelException(s"Can not add this node [$n]at the current focus[$current]")
      }
  }

  def getUniqueRef(baseNameVar : String=""): String = {
    baseNameVar + (baseNameVar match {
      case "object" => rootNode.getChild(SubjectOf("",URI(""))).length
      case "subject" => rootNode.getChild(ObjectOf("",URI(""))).length
      case "something" => rootNode.getChild(Something("")).length
      case "linkTo" => rootNode.getChild(LinkTo("",URI(""))).length
      case "linkFrom" => rootNode.getChild(LinkFrom("",URI(""))).length
      case "datatype" => rootNode.getChild(DatatypeNode("",SubjectOf("",URI("")),"")).length
      case _ => randomUUID.toString
    }).toString
  }

  /* start a request with a variable */
  def something( ref : String = getUniqueRef("something") ) : UnravelSession = {
    debug(" -- something -- ")
    focusManagement(Something(ref))
  }

  /* create node which focus is the subject : ?focusId <uri> ?target */
  def isSubjectOf( term : SparqlDefinition , ref : String = getUniqueRef("object")  ) : UnravelSession =
    checkQueryVariable(term).focusManagement(SubjectOf(ref,term))


  /* create node which focus is the subject : ?target <uri> ?focusId */
  def isObjectOf( term : SparqlDefinition , ref : String = getUniqueRef("subject")  ) : UnravelSession =
    checkQueryVariable(term).focusManagement(ObjectOf(ref,term))

  /* create node which focus is the properties :
  ?focusId ?target <uri>|literal
  */
  def isLinkTo(term : SparqlDefinition, ref : String = getUniqueRef("linkTo") ) : UnravelSession =
    checkQueryVariable(term).focusManagement(LinkTo(ref,term))


  /* create node which focus is typed with <uri>:
  ?focusId a <uri>
  */
  def isA( term : SparqlDefinition  ) : UnravelSession =
    checkQueryVariable(term)
    .isSubjectOf(URI("a"))
    .set(term)
    .focus(focusNode)

  /* create node which focus is the properties :
     <uri> ?target ?focusId
  */
  def isLinkFrom( term : SparqlDefinition, ref : String = getUniqueRef("linkFrom")  ) : UnravelSession =
    checkQueryVariable(term).focusManagement(LinkFrom(ref,term))

  /*
  Get attribute value of an object.
  return Sw with the old focus
  Attribute value is optional
  */

  def datatype( uri : URI, ref : String ) : UnravelSession =
    UnravelSession(
      config,
      root.focusManagement(DatatypeNode(focusNode,SubjectOf(ref,uri),ref), false).rootNode,
      Some(focusNode))


  def set( term : SparqlDefinition ) : UnravelSession =
    checkQueryVariable(term).focusManagement(Value(term),forward = false)

  def setList( terms : Seq[SparqlDefinition] ) : UnravelSession = focusManagement(ListValues(terms),forward = false)

  def remove( focus : String ) : UnravelSession =
    UnravelSession(config,RemoveNode.run(rootNode,focus),
      Some(
        NodeVisitor.getAncestorsRef(focusNode,rootNode) match {
          case l if l.length>1 => l(l.length - 2)
          case _ => rootNode.idRef
        }))

  def getSerializedString : String = OptionPickler.write(this)

  def setSerializedString(query : String) : UnravelSession = OptionPickler.read[UnravelSession](query)


  def console : UnravelSession = {
    debug(" -- console -- ")
    println("USER REQUEST\n" +
      pm.SimpleConsole().get(rootNode) + "\n" +
      "FOCUS NODE:"+ focusNode +
      "\nSOURCE:"+config.sources.map(v => println(v.path)).mkString(",") +"\n\n" +  {
      "\n--------------------------------------------------------------------\n -- HTTP GET -- \n\n" +
      sparql_get +
      "\n--------------------------------------------------------------------\n -- HTTP CURL -- \n\n" +
      sparql_curl+
      "\n--------------------------------------------------------------------\n" }
       )
      //"QUERY PLANNER\n"+
      //"todo....")
    this
  }

  def sparql: String = SparqlQueryBuilder.selectQueryString(rootNode).trim

  def sparql_get : String =
    (config.sources.length match {
      case 1 => config.sources(0).path
      case _ => ""
    }) + Url(path="", query=QueryString.fromPairs(
      "query"-> sparql,
      "format"->"json")
    )

  def sparql_curl : String =
    "curl -H \"Accept: application/json\" -G " +  (config.sources.length match {
        case 1 => config.sources(0).path
        case _ => ""
      }) + " --data-urlencode query='" + sparql + "'"

  /**
   * Discovery request
   *
   */
  def transaction = UnravelQuery(this)
  /**
   * Return solutions as Future corresponding with the current Node request.
   * @param lRef : selected variables
   * @param limit : upper bound on the number of solutions returned
   * @param offset : solution are generated after this offset
   * @return
   */
  def select(lRef: Seq[String] = List("*"), limit : Int = 0, offset : Int = 0) : UnravelQuery =
        transaction
        .limit(limit)
        .offset(offset)
        .projection(lRef)

  /**
   * Give an iterable object to browse and obtain all solution performed by a select.
   * @param lRef
   * @return iterable on select function
   */
  def selectByPage(lRef: Seq[String] = List("*"))  : Future[(Int,Seq[UnravelQuery])] = {
    // remove datatype node ref
    val lDatatypeRef = rootNode.lDatatypeNode.map(ldn => ldn.idRef )
    UnravelSessionHelper(this).count(lRef.filter( ! lDatatypeRef.contains(_)) ).map {
      case nSolutions if nSolutions == 0 => (nSolutions, Seq())
      case nSolutions =>        val nit: Int = (nSolutions + config.settings.pageSize - 1) / config.settings.pageSize
        (nSolutions,(0 to nit).map( p =>{
          val limit = config.settings.pageSize
          val offset = p*config.settings.pageSize
          select(lRef,limit,offset)
        }))
      }
  }


  /**
   * Give an iterable object to browse and obtain all solution performed by a select distinct.
   * @param lRef : selected variables
   * @return iterable on select function
   */
  def selectDistinctByPage(lRef: Seq[String] = List("*"))  : Future[(Int,Seq[UnravelQuery])] = {
    val lDatatypeRef = rootNode.lDatatypeNode.map(ldn => ldn.idRef )

    UnravelSessionHelper(this).count(lRef.filter(!lDatatypeRef.contains(_)), true).map {
      case nSolutions if nSolutions == 0 => (nSolutions, Seq())
      case nSolutions =>
        val nit: Int = (nSolutions + config.settings.pageSize - 1) / config.settings.pageSize
        (nSolutions, (0 to nit).map(p => {
          val limit = config.settings.pageSize
          val offset = p * config.settings.pageSize
          select(lRef, limit, offset).distinct
        }))
    }
  }


  def browse[A](visitor : (Node, Integer) => A ) : Seq[A] = NodeVisitor.map(rootNode,0,visitor)

  def setDecoration(key : String, value : String) : UnravelSession = {
      rootNode
        .getChild[Node](rootNode.asInstanceOf[Node])
        .filter( _.idRef == focusNode )
        .lastOption match {
          case Some(n) if n.isInstanceOf[Root]  => {
            UnravelSession(config,rootNode.addDecoratingAttribute(key,value).asInstanceOf[Root],Some(rootNode.reference()))
          }
          case Some(n) => {
            val sw = remove(focusNode)
            sw.focusManagement(n.addDecoratingAttribute(key,value))
          }
          case None => throw UnravelException(s"Can not reach current node -- $focusNode --]")
        }
  }

  def getDecoration(key : String) : String = {
    rootNode
      .getChild[Node](rootNode.asInstanceOf[Node])
      .filter( _.idRef == focusNode )
      .lastOption match {
      case Some(n)  => {
        n.decorations.getOrElse(key,"")
      }
      case None => ""
    }
  }
}
