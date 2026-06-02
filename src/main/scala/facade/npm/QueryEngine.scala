/**
 * olivier.filangi@inrae.fr - P2M2 Platform
 * Project: https://forge.inrae.fr/p2m2/discovery
 */

package facade.npm

import facade.npm.SourceType.SourceType

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.JSImport
import scala.scalajs.js.|

@js.native
@JSImport("@comunica/query-sparql", "QueryEngine")
class QueryEngine extends js.Object {
  def queryBindings(request: String, context: QueryEngineOptions = null): js.Promise[BindingsStream] = js.native
  def queryQuads(request: String, context: QueryEngineOptions = null): js.Promise[QuadStream] = js.native
  def queryBoolean(request: String, context: QueryEngineOptions = null): js.Promise[BooleanResult] = js.native
  def queryVoid(request: String, context: QueryEngineOptions = null): js.Promise[js.UndefOr[js.Any]] = js.native
  def query(request: String, context: QueryEngineOptions = null): js.Promise[js.Any] = js.native
  def resultToString(result: js.Any, mediaType: String): js.Promise[IActorSparqlSerializeOutput] = js.native
}

@js.native
trait BindingsStream extends JsReadableStream

@js.native
trait QuadStream extends JsReadableStream

@js.native
trait BooleanResult extends js.Object {
  val value: Boolean = js.native
}

@js.native
trait IActorSparqlSerializeOutput extends js.Object {
  val data: JsReadableStream = js.native
}

trait QueryEngineOptions extends js.Object {
  val sources: js.UndefOr[js.Array[String | SourceDefinitionNewQueryEngine | N3Store]] = js.undefined
  val destination: js.UndefOr[String | SourceDefinitionNewQueryEngine | N3Store] = js.undefined
  val lenient: js.UndefOr[Boolean] = js.undefined
  val initialBindings: js.UndefOr[Bindings] = js.undefined
  val baseIRI: js.UndefOr[String] = js.undefined
  val datetime: js.UndefOr[js.Date] = js.undefined
  val httpIncludeCredentials: js.UndefOr[Boolean] = js.undefined
  val httpProxyHandler: js.UndefOr[ProxyHandlerStatic] = js.undefined
  val httpAuth: js.UndefOr[String] = js.undefined
  val httpTimeout: js.UndefOr[Int] = js.undefined
  val httpBodyTimeout: js.UndefOr[Boolean] = js.undefined
  val httpRetryCount: js.UndefOr[Int] = js.undefined
  val httpRetryDelay: js.UndefOr[Int] = js.undefined
  val httpRetryOnServerError: js.UndefOr[Boolean] = js.undefined
  val recoverBrokenLinks: js.UndefOr[Boolean] = js.undefined
  val unionDefaultGraph: js.UndefOr[Boolean] = js.undefined
  val distinctConstruct: js.UndefOr[Boolean] = js.undefined
  val readOnly: js.UndefOr[Boolean] = js.undefined
  val localizeBlankNodes: js.UndefOr[Boolean] = js.undefined
  val log: js.UndefOr[LoggerPretty] = js.undefined
  val queryFormat: js.UndefOr[QueryFormatType] = js.undefined
  val explain: js.UndefOr[String] = js.undefined
}

object QueryEngineOptions {
  def apply(
             sources: List[String | SourceDefinitionNewQueryEngine | N3Store] = Nil,
             destination: js.UndefOr[String | SourceDefinitionNewQueryEngine | N3Store] = js.undefined,
             lenient: js.UndefOr[Boolean] = js.undefined,
             initialBindings: js.UndefOr[Bindings] = js.undefined,
             baseIRI: js.UndefOr[String] = js.undefined,
             datetime: js.UndefOr[js.Date] = js.undefined,
             httpIncludeCredentials: js.UndefOr[Boolean] = js.undefined,
             httpProxyHandler: js.UndefOr[ProxyHandlerStatic] = js.undefined,
             httpAuth: js.UndefOr[String] = js.undefined,
             httpTimeout: js.UndefOr[Int] = js.undefined,
             httpBodyTimeout: js.UndefOr[Boolean] = js.undefined,
             httpRetryCount: js.UndefOr[Int] = js.undefined,
             httpRetryDelay: js.UndefOr[Int] = js.undefined,
             httpRetryOnServerError: js.UndefOr[Boolean] = js.undefined,
             recoverBrokenLinks: js.UndefOr[Boolean] = js.undefined,
             unionDefaultGraph: js.UndefOr[Boolean] = js.undefined,
             distinctConstruct: js.UndefOr[Boolean] = js.undefined,
             readOnly: js.UndefOr[Boolean] = js.undefined,
             localizeBlankNodes: js.UndefOr[Boolean] = js.undefined,
             log: js.UndefOr[LoggerPretty] = js.undefined,
             queryFormat: js.UndefOr[QueryFormatType] = js.undefined,
             explain: js.UndefOr[QueryFormat.Value] = js.undefined
           ): QueryEngineOptions = {
    val obj = js.Dynamic.literal()

    if (sources.nonEmpty) obj.updateDynamic("sources")(sources.toJSArray)
    destination.foreach(v => obj.updateDynamic("destination")(v.asInstanceOf[js.Any]))
    lenient.foreach(v => obj.updateDynamic("lenient")(v))
    initialBindings.foreach(v => obj.updateDynamic("initialBindings")(v))
    baseIRI.foreach(v => obj.updateDynamic("baseIRI")(v))
    datetime.foreach(v => obj.updateDynamic("datetime")(v))
    httpIncludeCredentials.foreach(v => obj.updateDynamic("httpIncludeCredentials")(v))
    httpProxyHandler.foreach(v => obj.updateDynamic("httpProxyHandler")(v))
    httpAuth.foreach(v => obj.updateDynamic("httpAuth")(v))
    httpTimeout.foreach(v => obj.updateDynamic("httpTimeout")(v))
    httpBodyTimeout.foreach(v => obj.updateDynamic("httpBodyTimeout")(v))
    httpRetryCount.foreach(v => obj.updateDynamic("httpRetryCount")(v))
    httpRetryDelay.foreach(v => obj.updateDynamic("httpRetryDelay")(v))
    httpRetryOnServerError.foreach(v => obj.updateDynamic("httpRetryOnServerError")(v))
    recoverBrokenLinks.foreach(v => obj.updateDynamic("recoverBrokenLinks")(v))
    unionDefaultGraph.foreach(v => obj.updateDynamic("unionDefaultGraph")(v))
    distinctConstruct.foreach(v => obj.updateDynamic("distinctConstruct")(v))
    readOnly.foreach(v => obj.updateDynamic("readOnly")(v))
    localizeBlankNodes.foreach(v => obj.updateDynamic("localizeBlankNodes")(v))
    log.foreach(v => obj.updateDynamic("log")(v))
    queryFormat.foreach(v => obj.updateDynamic("queryFormat")(v))
    explain.foreach(v => obj.updateDynamic("explain")(v.toString))

    obj.asInstanceOf[QueryEngineOptions]
  }
}

trait SourceDefinitionNewQueryEngine extends js.Object {
  val `type`: js.UndefOr[String] = js.undefined
  val value: js.UndefOr[String] = js.undefined
  val context: js.UndefOr[QueryEngineOptions] = js.undefined
}

object SourceDefinitionNewQueryEngine {
  def apply(
             `type`: SourceType,
             value: String,
             context: js.UndefOr[QueryEngineOptions] = js.undefined
           ): SourceDefinitionNewQueryEngine =
    js.Dynamic.literal(
      `type` = `type`.toString,
      value = value,
      context = context
    ).asInstanceOf[SourceDefinitionNewQueryEngine]
}

trait QueryFormatType extends js.Object {
  val language: js.UndefOr[String] = js.undefined
  val version: js.UndefOr[String] = js.undefined
}

object QueryFormatType {
  def apply(language: String, version: String): QueryFormatType =
    js.Dynamic.literal(
      language = language,
      version = version
    ).asInstanceOf[QueryFormatType]
}

object SourceType extends Enumeration {
  type SourceType = Value
  val hypermedia, file, sparql, rdfjs, hdt, ostrichFile = Value
}

object QueryFormat extends Enumeration {
  type QueryFormat = Value
  val sparql, graphql = Value
}

object ResultFormat extends Enumeration {
  type ResultFormat = Value
  val `application/json`, simple, `application/sparql-results+json`, `application/sparql-results+xml`,
  `text/csv`, `text/tab-separated-values`, stats, table, tree, `application/trig`, `application/n-quads`,
  `text/turtle`, `application/n-triples`, `text/n3`, `application/ld+json` = Value
}

@js.native
@JSImport("@comunica/utils-bindings-factory", "BindingsFactory")
class BindingsFactory() extends js.Object {
  def bindings(): Bindings = js.native
  def fromRecord(record: js.Dictionary[Term]): Bindings = js.native
}

@js.native
trait Bindings extends js.Object with js.Iterable[js.Array[js.Any]] {
  def has(key: String): Boolean = js.native
  def get(key: String): Term = js.native
  override def toString(): String = js.native
}

@js.native
@JSImport("@comunica/logger-pretty", "LoggerPretty")
class LoggerPretty(options: js.Object = js.Dynamic.literal()) extends js.Object

@js.native
@JSImport("@comunica/actor-http-proxy", "ProxyHandlerStatic")
class ProxyHandlerStatic(uri: String) extends js.Object