/**
 * olivier.filangi@inrae.fr - P2M2 Platform
 * Project: https://forge.inrae.fr/p2m2/discovery
 */

package facade.npm

import facade.npm.N3FormatOption.N3FormatOption

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.{JSImport, JSName}
import scala.scalajs.js.|

@js.native
@JSImport("n3", JSImport.Namespace)
object N3 extends js.Object {
  val DataFactory: N3DataFactory = js.native
}

object N3FormatOption extends Enumeration {
  type N3FormatOption = Value
  val TriG, `N-Quads`, N3, Turtle, `N-Triples`, `application/trig`, `text/n3`, Notation3 = Value
}

trait N3Options extends js.Object {
  val baseIRI: js.UndefOr[String] = js.undefined
  val format: js.UndefOr[String] = js.undefined
  val end: js.UndefOr[Boolean] = js.undefined
  val prefixes: js.UndefOr[js.Dictionary[String]] = js.undefined
  val blankNodePrefix: js.UndefOr[String] = js.undefined
  val isImpliedBy: js.UndefOr[Boolean] = js.undefined
  val comments: js.UndefOr[Boolean] = js.undefined
}

object N3Options {
  def apply(
             baseIRI: js.UndefOr[String] = js.undefined,
             format: js.UndefOr[N3FormatOption] = js.undefined,
             end: js.UndefOr[Boolean] = js.undefined,
             prefixes: Map[String, String] = Map.empty,
             blankNodePrefix: js.UndefOr[String] = js.undefined,
             isImpliedBy: js.UndefOr[Boolean] = js.undefined,
             comments: js.UndefOr[Boolean] = js.undefined
           ): N3Options =
    js.Dynamic.literal(
      baseIRI = baseIRI,
      format = format.map(_.toString).asInstanceOf[js.Any],
      end = end,
      prefixes = (if (prefixes.nonEmpty) prefixes.toJSDictionary else js.undefined),
      blankNodePrefix = blankNodePrefix,
      isImpliedBy = isImpliedBy,
      comments = comments
    ).asInstanceOf[N3Options]
}

@js.native
trait N3DataFactory extends js.Object {
  def namedNode(value: String): NamedNode = js.native
  def blankNode(value: String = js.native): BlankNode = js.native
  def literal(value: String): Literal = js.native
  def literal(value: String, languageOrDatatype: String | NamedNode): Literal = js.native
  def variable(value: String): Variable = js.native
  def defaultGraph(): DefaultGraph = js.native
  def quad(subject: Term, predicate: Term, `object`: Term, graph: Term = js.native): Quad = js.native
}

@js.native
trait N3ParserCallbackHandlers extends js.Object {
  val onQuad: js.Function2[js.UndefOr[js.Error], Quad, Unit] = js.native
  val onPrefix: js.UndefOr[js.Function2[String, NamedNode, Unit]] = js.native
  val onComment: js.UndefOr[js.Function1[String, Unit]] = js.native
}

object N3ParserCallbackHandlers {
  def apply(
             onQuad: js.Function2[js.UndefOr[js.Error], Quad, Unit],
             onPrefix: js.UndefOr[js.Function2[String, NamedNode, Unit]] = js.undefined,
             onComment: js.UndefOr[js.Function1[String, Unit]] = js.undefined
           ): N3ParserCallbackHandlers =
    js.Dynamic.literal(
      onQuad = onQuad,
      onPrefix = onPrefix,
      onComment = onComment
    ).asInstanceOf[N3ParserCallbackHandlers]
}

@js.native
@JSImport("n3", "Parser")
class N3Parser(options: N3Options = null) extends js.Object {
  def parse(input: String): js.Array[Quad] = js.native
  def parse(
             input: String | JsReadableStream,
             callback: js.Function3[js.UndefOr[js.Error], js.UndefOr[Quad], js.UndefOr[js.Dictionary[String]], Unit]
           ): Unit = js.native
  def parse(input: String | JsReadableStream, handlers: N3ParserCallbackHandlers): Unit = js.native
}

@js.native
trait PredicateObject extends js.Object {
  val predicate: Term = js.native
  val `object`: Term = js.native
}

object PredicateObject {
  def apply(predicate: Term, `object`: Term): PredicateObject =
    js.Dynamic.literal(
      predicate = predicate,
      `object` = `object`
    ).asInstanceOf[PredicateObject]
}

@js.native
@JSImport("n3", "Writer")
class N3Writer(options: N3Options = null) extends js.Object {
  def this(outputStream: JsWritableStream, options: N3Options) = this(js.native)

  def addQuad(quad: Quad): Unit = js.native
  def addQuad(subject: Term, predicate: Term, `object`: Term, graph: Term = null): Unit = js.native

  def blank(): BlankNode = js.native
  def blank(predicate: Term, `object`: Term): BlankNode = js.native
  def blank(obj: js.Dictionary[Term]): BlankNode = js.native
  def blank(listObj: js.Array[PredicateObject], `object`: Term = null): BlankNode = js.native

  def list[T <: Term](listObj: js.Array[T]): Term = js.native

  def end(done: js.Function2[js.UndefOr[js.Error], String, Unit] = null): Unit = js.native
}

@js.native
trait NodeStream extends JsReadableStream

@js.native
trait DatasetCoreAndReadable extends JsReadableStream

@js.native
@JSImport("n3", "Store")
class N3Store(quads: js.Array[Quad] = js.Array(), options: js.Object = null) extends js.Object {
  val size: Int = js.native

  def add(quad: Quad): this.type = js.native
  def delete(quad: Quad): this.type = js.native
  def has(quad: Quad): Boolean = js.native

  @JSName("match")
  def `match`(s: Term = null, p: Term = null, o: Term = null, g: Term = null): DatasetCoreAndReadable = js.native

  def addQuad(quad: Quad): Unit = js.native
  def addQuad(subject: Term, predicate: Term, `object`: Term, graph: Term = null): Unit = js.native
  def addQuads(quads: Quad*): Unit = js.native

  def removeQuad(quad: Quad): Unit = js.native
  def removeQuads(quads: Quad*): Unit = js.native
  def removeMatches(s: Term = null, p: Term = null, o: Term = null, g: Term = null): Unit = js.native
  def deleteGraph(graph: Term): Unit = js.native
  def createBlankNode(suggestedName: String = ""): BlankNode = js.native

  def getQuads(s: Term = null, p: Term = null, o: Term = null, g: Term = null): js.Array[Quad] = js.native
  def countQuads(s: Term = null, p: Term = null, o: Term = null, g: Term = null): Int = js.native

  def forEach(callback: js.Function1[Quad, Unit], s: Term = null, p: Term = null, o: Term = null, g: Term = null): Unit = js.native
  def every(callback: js.Function1[Quad, Boolean], s: Term = null, p: Term = null, o: Term = null, g: Term = null): Boolean = js.native
  def some(callback: js.Function1[Quad, Boolean], s: Term = null, p: Term = null, o: Term = null, g: Term = null): Boolean = js.native

  def getSubjects(predicate: Term = null, `object`: Term = null, graph: Term = null): js.Array[Term] = js.native
  def getPredicates(subject: Term = null, `object`: Term = null, graph: Term = null): js.Array[Term] = js.native
  def getObjects(subject: Term = null, predicate: Term = null, graph: Term = null): js.Array[Term] = js.native
  def getGraphs(subject: Term = null, predicate: Term = null, `object`: Term = null): js.Array[Term] = js.native
}

@js.native
@JSImport("n3", "StreamParser")
class N3StreamParser(options: N3Options = null) extends NodeStream

@js.native
@JSImport("n3", "StreamWriter")
class N3StreamWriter(options: N3Options = null) extends NodeStream