package fr.inrae.metabohub.semantic_web.js

import fr.inrae.metabohub.semantic_web.{UnravelSession, UnravelQuery}
import fr.inrae.metabohub.semantic_web.node.Node
import fr.inrae.metabohub.semantic_web.rdf.{IRI, SparqlDefinition, URI}
import fr.inrae.metabohub.semantic_web.configuration._
import fr.inrae.metabohub.semantic_web.exception._
import fr.inrae.metabohub.semantic_web.js.increment._
import fr.inrae.metabohub.semantic_web.view.HtmlView

import scala.scalajs._
import scala.scalajs.js.{Dynamic, JSON}
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel(name="UnravelSession")
case class UnravelSessionJs(
                          config: UnravelConfig=UnravelConfig(),
                          swArg: UnravelSession = null
                        ) {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  val sw: UnravelSession = swArg match {
    case null => UnravelSession(config)
    case v =>v
  }

  def toIRI(any:Any) : IRI = any match {
    case v : IRI => v
    case v : URI => IRI(v.sparql)
    case string : String => string
    case _ => throw UnravelException(any.toString + " can not be cast into IRI.")
  }

  def toURI(any:Any): URI = any match {
    case v : URI => v
    case string : String => string
    case _ => throw UnravelException(any.toString + " can not be cast into IRI.")
  }

  @JSExport
  val filter: FilterIncrementJs = FilterIncrementJs(this)

  @JSExport
  def helper(regex : String = ""): UnravelSessionJs = { HtmlView(sw,regex) ; UnravelSessionJs(config,sw) }

  @JSExport
  def bind(`var` : String) : BindIncrementJs = BindIncrementJs(this,`var`)

  @JSExport
  def finder :UnravelSessionHelperJs = UnravelSessionHelperJs(sw)

  @JSExport
  def setConfig(newConfig : UnravelConfig) : UnravelSessionJs = UnravelSessionJs(newConfig,sw.setConfig(newConfig))

  @JSExport
  def getConfig() : UnravelConfig = sw.getConfig

  @JSExport
  def focus(ref : String) : UnravelSessionJs = UnravelSessionJs(config,sw.focus(ref))

  @JSExport
  def prefix(short : String, long : Any ) : UnravelSessionJs = UnravelSessionJs(config,sw.prefix(short,toIRI(long)))

  @JSExport
  def getPrefix(short : String) : Any = sw.getPrefix(short)

  @JSExport
  def directive(directive : String) : UnravelSessionJs = UnravelSessionJs(config,sw.directive(directive))

  @JSExport
  def graph(graph : Any) : UnravelSessionJs = UnravelSessionJs(config,sw.graph(toIRI(graph)))

  @JSExport
  def root(): UnravelSessionJs = UnravelSessionJs(config,sw.root)

  @JSExport
  def focus() : String = sw.focusNode

  @JSExport
  def namedGraph(graph : Any ) : UnravelSessionJs = UnravelSessionJs(config,sw.namedGraph(toIRI(graph)))
  /* start a request */
  @JSExport
  def something( ref : String = sw.getUniqueRef("something") ) : UnravelSessionJs = UnravelSessionJs(config,sw.something(ref))

  /* create node which focus is the subject : ?focusId <uri> ?target */
  @JSExport
  def isSubjectOf( uri : Any , ref : String = sw.getUniqueRef("object") ) : UnravelSessionJs =
    UnravelSessionJs(config,sw.isSubjectOf(uri,ref))

  /* create node which focus is the subject : ?focusId <uri> ?target */
  @JSExport
  def isObjectOf( uri : Any , ref : String = sw.getUniqueRef("subject") ) : UnravelSessionJs = UnravelSessionJs(config,sw.isObjectOf(uri,ref))

  @JSExport
  def isLinkTo( uri : Any , ref : String = sw.getUniqueRef("linkTo") ) : UnravelSessionJs = UnravelSessionJs(config,sw.isLinkTo(uri,ref))

  @JSExport
  def isA( term : Any ) : UnravelSessionJs = UnravelSessionJs(config,sw.isA(term))

  @JSExport
  def isLinkFrom( uri : String , ref : String = sw.getUniqueRef("linkFrom") ) : UnravelSessionJs = UnravelSessionJs(config,sw.isLinkFrom(uri,ref))
  /* set */
  @JSExport
  def set( term : Any ) : UnravelSessionJs = UnravelSessionJs(config,sw.set(term))

  @JSExport
  def setList( terms : Any* ) : UnravelSessionJs = UnravelSessionJs(config,sw.setList(terms.map(SparqlDefinition.fromAny)))

  @JSExport
  def datatype( uri : Any, ref : String ) : UnravelSessionJs = UnravelSessionJs(config,sw.datatype(toURI(uri),ref))

  @JSExport
  def remove( focus : String ) : UnravelSessionJs = UnravelSessionJs(config,sw.remove(focus))

  @JSExport
  def console() : UnravelSessionJs = UnravelSessionJs(config,sw.console)

  @JSExport
  def sparql() : String = sw.sparql

  @JSExport
  def sparql_get() : String = sw.sparql_get

  @JSExport
  def sparql_curl() : String = sw.sparql_curl

  @JSExport
  def getSerializedString(): String = sw.getSerializedString

  @JSExport
  def setSerializedString(query : String): UnravelSessionJs = UnravelSessionJs(config,sw.setSerializedString(query))

  @JSExport
  def browse[A](visitor : js.Function2[Dynamic, Integer,A] ) : js.Array[A] = {
    val visitor2 : (Node, Integer) => A = (n, p) => {
      visitor(JSON.parse(OptionPickler.write(n)),p)
    }
    sw.browse(visitor2).toJSArray
  }
  
  @JSExport
  def setDecoration(key : String, value : String) : UnravelSessionJs = UnravelSessionJs(config,sw.setDecoration(key,value))

  @JSExport
  def getDecoration(key : String) : String = sw.getDecoration(key)

  @JSExport
  def select(lRef: String*): UnravelQueryJs = UnravelQueryJs(sw.select(lRef))

  @JSExport
  def select(lRef: js.Array[String], limit : Int = 0, offset : Int = 0): UnravelQueryJs =
    UnravelQueryJs(sw.select(lRef.toSeq,limit,offset))

  @JSExport
  def selectByPage(lRef: js.Array[String])  : js.Promise[(Int,js.Array[UnravelQueryJs])] =
    sw.selectByPage(lRef.toSeq).map( res => {
      val n : Int = res._1
      val l : Seq[UnravelQuery] = res._2
      (n, l.map(swt => UnravelQueryJs(swt) ).toJSArray) }).toJSPromise

  @JSExport
  def selectByPage(lRef: String*)  : js.Promise[(Int,js.Array[UnravelQueryJs])] =
    selectByPage(lRef.toJSArray)

  @JSExport
  def selectDistinctByPage(lRef: js.Array[String])  : js.Promise[(Int,js.Array[UnravelQueryJs])] =
    sw.selectDistinctByPage(lRef.toSeq).map( res => {
    val n : Int = res._1
    val l : Seq[UnravelQuery] = res._2
    (n, l.map(swt => UnravelQueryJs(swt) ).toJSArray) }).toJSPromise

  @JSExport
  def selectDistinctByPage(lRef: String*)  : js.Promise[(Int,js.Array[UnravelQueryJs])] =
    selectDistinctByPage(lRef.toJSArray)
}
