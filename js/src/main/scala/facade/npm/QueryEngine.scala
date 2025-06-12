/**
 * olivier.filangi@inrae.fr - P2M2 Platform - https://github.com/p2m2
 */
package facade.npm

import facade.npm.SourceType.SourceType

import scala.language.implicitConversions
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.JSImport
import scala.scalajs.js.|

import typings.node.streamMod.{Transform, Readable}


@js.native
@JSImport("@comunica/query-sparql", "QueryEngine")
class QueryEngine extends js.Object {
  def queryBindings( request : String , context : QueryEngineOptions = null) : js.Promise[Transform] = js.native
  def queryQuads( request : String , context : QueryEngineOptions = null) : js.Promise[Transform] = js.native
  def query( request : String , context : QueryEngineOptions = null) : js.Promise[Transform] = js.native
  def resultToString(s :Transform,mediaType: String ) : js.Promise[IActorSparqlSerializeOutput] = js.native
}

@js.native
@JSImport("@comunica/query-sparql", "IActorSparqlSerializeOutput")
class IActorSparqlSerializeOutput extends js.Object {
  val data : Readable = js.native
}
/**
 * https://comunica.dev/docs/query/advanced/context/
 */
trait QueryEngineOptions extends js.Object {
  val sources                : js.UndefOr[js.Array[String | SourceDefinitionNewQueryEngine | N3.Store]] = js.undefined
  val lenient                : js.UndefOr[Boolean] = js.undefined
  val initialBindings        : js.UndefOr[Bindings] = js.undefined
  val baseIRI                : js.UndefOr[String] = js.undefined
  val datetime               : js.UndefOr[js.Date] = js.undefined
  val httpIncludeCredentials : js.UndefOr[Boolean] = js.undefined
  val httpProxyHandler       : js.UndefOr[ProxyHandlerStatic] = js.undefined
  val httpAuth               : js.UndefOr[String] = js.undefined /* 'username:password' */
  val log                    : js.UndefOr[LoggerPretty] = js.undefined
  val queryFormat            : js.UndefOr[QueryFormatType] = js.undefined
  val explain                : js.UndefOr[QueryFormat.Value] = js.undefined
}

object QueryEngineOptions {
  def apply(
             sources                : List[String | SourceDefinitionNewQueryEngine | N3.Store] = List(),
             lenient                : Boolean = false,
             initialBindings        : js.UndefOr[Bindings] = new BindingsFactory().bindings(),
             baseIRI                : js.UndefOr[String] = js.undefined,
             date                   : js.UndefOr[js.Date] = js.undefined,
             httpIncludeCredentials : js.UndefOr[Boolean] = js.undefined,
             httpProxyHandler       : js.UndefOr[ProxyHandlerStatic] = js.undefined,
             httpAuth               : js.UndefOr[String] = js.undefined,
             log                    : js.UndefOr[LoggerPretty] = js.undefined,
             queryFormat            : QueryFormatType = QueryFormatType("sparql","1.1")
           ): QueryEngineOptions = js.Dynamic.literal(
    "sources" -> (sources match {
      case l if l.nonEmpty  => l.toJSArray
      case _ => js.undefined
    }),
    "lenient" ->  lenient,
    "initialBindings" ->  initialBindings,
    "baseIRI" -> baseIRI,
    "date" -> date,
    "httpIncludeCredentials" ->  httpIncludeCredentials,
    "httpProxyHandler" ->  httpProxyHandler,
    "httpAuth" ->  httpAuth,
    "log" ->  log,
    "queryFormat" ->  queryFormat
  ).asInstanceOf[QueryEngineOptions]
}

trait SourceDefinitionNewQueryEngine extends js.Object {
  val `type`           : js.UndefOr[SourceType.Value] = js.undefined
  val value            : js.UndefOr[String] = js.undefined
}

object SourceDefinitionNewQueryEngine {
  def apply(
             `type` : SourceType,
             value : String
           ) : SourceDefinitionNewQueryEngine = js.Dynamic.literal(
    `type` = `type`.toString,
    value = value
  ).asInstanceOf[SourceDefinitionNewQueryEngine]
}


trait QueryFormatType extends js.Object {
  val language               : js.UndefOr[String] = js.undefined
  val version                : js.UndefOr[String] = js.undefined
}

object QueryFormatType {
  def apply(
             language : String,
             version : String
           ) : QueryFormatType = js.Dynamic.literal(
    language = language,
    version = version
  ).asInstanceOf[QueryFormatType]
}

object SourceType extends Enumeration {
  type SourceType = Value
  val hypermedia, file, sparql, rdfjsSource, hdtFile, ostrichFile = Value
}

object QueryFormat extends Enumeration {
  type QueryFormat = Value
  val sparql,graphql = Value

}

object ResultFormat extends Enumeration {
  type ResultFormat = Value
  val `application/json`,simple,`application/sparql-results+json`,`application/sparql-results+xml`,
  `text/csv`,`text/tab-separated-values`,stats,table, tree, `application/trig`, `application/n-quads`, `text/turtle`,
  `application/n-triples`, `text/n3`, `application/ld+json` = Value
}

@js.native
@JSImport("@comunica/bindings-factory", "BindingsFactory")
class BindingsFactory() extends js.Object {
  def bindings() : Bindings = js.native
}

@js.native
@JSImport("@comunica/bindings-factory", "Bindings")
class Bindings(
                val `type` : String = "bindings",
                val dataFactory : BindingsFactory = new BindingsFactory(),
                val entries : js.Object) extends js.Object {
  def has(key:String) : Boolean = js.native
  def get(key:String) : Term = js.native
}

@js.native
@JSImport("@comunica/logger-pretty", "LoggerPretty")
class LoggerPretty(options : js.Object) extends js.Object

@js.native
@JSImport("@comunica/actor-http-proxy", "ProxyHandlerStatic")
class ProxyHandlerStatic(uri : String ) extends js.Object

/* new LoggerPretty({ level: 'debug' }) */
