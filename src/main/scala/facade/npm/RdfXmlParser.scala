/**
 * olivier.filangi@inrae.fr - P2M2 Platform
 * Project: https://forge.inrae.fr/p2m2/discovery
 */

package facade.npm

import scala.scalajs.js
import scala.scalajs.js.annotation._

@js.native
@JSImport("rdfxml-streaming-parser", "RdfXmlParser")
class RdfXmlParser(options: RdfXmlParserOptions = null) extends js.Object {
  def on(event: String, callback: js.Function1[js.Any, Unit]): this.type = js.native
  def write(chunk: String): Boolean = js.native
  def end(): Unit = js.native
  def `import`(stream: JsReadableStream): RdfXmlParser = js.native
}

trait RdfXmlParserOptions extends js.Object {
  val dataFactory: js.UndefOr[js.Object] = js.undefined
  val baseIRI: js.UndefOr[String] = js.undefined
  val defaultGraph: js.UndefOr[NamedNode] = js.undefined
  val strict: js.UndefOr[Boolean] = js.undefined
  val trackPosition: js.UndefOr[Boolean] = js.undefined
  val allowDuplicateRdfIds: js.UndefOr[Boolean] = js.undefined
  val validateUri: js.UndefOr[Boolean] = js.undefined
}

object RdfXmlParserOptions {
  def apply(
             dataFactory: js.UndefOr[js.Object] = js.undefined,
             baseIRI: js.UndefOr[String] = js.undefined,
             defaultGraph: js.UndefOr[NamedNode] = js.undefined,
             strict: js.UndefOr[Boolean] = js.undefined,
             trackPosition: js.UndefOr[Boolean] = js.undefined,
             allowDuplicateRdfIds: js.UndefOr[Boolean] = js.undefined,
             validateUri: js.UndefOr[Boolean] = js.undefined
           ): RdfXmlParserOptions =
    js.Dynamic.literal(
      dataFactory = dataFactory,
      baseIRI = baseIRI,
      defaultGraph = defaultGraph,
      strict = strict,
      trackPosition = trackPosition,
      allowDuplicateRdfIds = allowDuplicateRdfIds,
      validateUri = validateUri
    ).asInstanceOf[RdfXmlParserOptions]
}