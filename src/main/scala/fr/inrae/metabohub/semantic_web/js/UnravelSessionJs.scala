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

/**
 * JavaScript wrapper around an RDF traversal session.
 *
 * String values are interpreted as follows:
 *  - a string starting with `?` is treated as a variable;
 *  - a string starting with `http` or using a prefixed name such as `rdf:type`
 *    is treated as an RDF IRI.
 *
 * Typed values can also be used directly:
 *  - `Var("metabolite")` is equivalent to `"?metabolite"`;
 *  - `URI("http://some/some2")` is equivalent to `"http://some/some2"`.
 *
 * @note Methods such as [[out]], [[in]], and [[traverse]] accept either
 *       string shortcuts or explicit RDF values.
 */
@JSExportTopLevel(name = "UnravelSession")
case class UnravelSessionJs(
                             config: UnravelConfig = UnravelConfig(),
                             swArg: UnravelSession = null
                           ) {
  implicit val ec: scala.concurrent.ExecutionContext =
    scala.scalajs.concurrent.JSExecutionContext.queue

  val sw: UnravelSession = swArg match {
    case null => UnravelSession(config)
    case v    => v
  }

  def toIRI(any: Any): IRI = any match {
    case v: IRI    => v
    case v: URI    => IRI(v.sparql)
    case s: String => s
    case _         =>
      throw UnravelException(
        s"Unsupported IRI value: $any (expected IRI, URI, or String)"
      )
  }

  /** Adapts a JS closure (UnravelSessionJs => UnravelSessionJs)
   *  to a Scala closure (UnravelSession => UnravelSession). */
  private def wrap(
                    f: js.Function1[UnravelSessionJs, UnravelSessionJs]
                  ): UnravelSession => UnravelSession =
    inner => f(UnravelSessionJs(config, inner)).sw

  // ------------------------------------------------------------------ //
  //  Accessors / configuration                                         //
  // ------------------------------------------------------------------ //

  @JSExport
  val filter: FilterIncrementJs = FilterIncrementJs(this)

  @JSExport
  def helper(regex: String = ""): UnravelSessionJs = {
    HtmlView(sw, regex)
    UnravelSessionJs(config, sw)
  }

  @JSExport
  def bind(`var`: String): BindIncrementJs =
    BindIncrementJs(this, `var`)

  @JSExport
  def finder: UnravelSessionHelperJs =
    UnravelSessionHelperJs(sw)

  @JSExport
  def setConfig(newConfig: UnravelConfig): UnravelSessionJs =
    UnravelSessionJs(newConfig, sw.setConfig(newConfig))

  @JSExport
  def getConfig(): UnravelConfig = sw.getConfig

  @JSExport
  def prefix(short: String, long: Any): UnravelSessionJs =
    UnravelSessionJs(config, sw.prefix(short, toIRI(long)))

  @JSExport
  def getPrefix(short: String): Any =
    sw.getPrefix(short)

  @JSExport
  def directive(directive: String): UnravelSessionJs =
    UnravelSessionJs(config, sw.directive(directive))

  @JSExport
  def graph(graph: Any): UnravelSessionJs =
    UnravelSessionJs(config, sw.graph(toIRI(graph)))

  @JSExport
  def root(): UnravelSessionJs =
    UnravelSessionJs(config, sw.root)

  @JSExport
  def current(): String = sw.focusNode

  @JSExport
  def namedGraph(graph: Any): UnravelSessionJs =
    UnravelSessionJs(config, sw.namedGraph(toIRI(graph)))

  /** Positions the cursor on `ref` (no closure). */
  @JSExport
  def from(ref: String): UnravelSessionJs =
    UnravelSessionJs(config, sw.from(ref))

  /** Positions the cursor on `ref`, applies the closure, then restores the parent focus. */
  @JSExport
  def from(
            ref: String,
            f: js.Function1[UnravelSessionJs, UnravelSessionJs]
          ): UnravelSessionJs =
    UnravelSessionJs(config, sw.from(ref, wrap(f)))

  @JSExport
  def something(ref: String): UnravelSessionJs =
    UnravelSessionJs(config, sw.something(ref))

  @JSExport
  def something(
                 ref: String,
                 f: js.Function1[UnravelSessionJs, UnravelSessionJs]
               ): UnravelSessionJs =
    UnravelSessionJs(config, sw.something(ref, wrap(f)))

  // ------------------------------------------------------------------ //
  //  Traversal                                                         //
  // ------------------------------------------------------------------ //

  /**
   * Follows an RDF property from the current subject to its object(s).
   *
   * The property and object can be fixed IRIs or variables.
   *
   * @example
   * {{{
   * session.out("rdf:type")
   * session.out("?p")
   * session.out(Var("p"))
   * }}}
   */
  @JSExport
  def out(property: Any): UnravelSessionJs =
    UnravelSessionJs(config, sw.out(property, null, identity))

  /**
   * Follows an RDF property from the current subject to a given object.
   *
   * Both property and object can be fixed IRIs or variables.
   *
   * @example
   * {{{
   * session.out("rdf:type", "owl:Class")
   * session.out("http://example.org/p", "?o")
   * session.out(URI("http://example.org/p"), Var("o"))
   * }}}
   */
  @JSExport
  def out(property: Any, `object`: Any): UnravelSessionJs =
    UnravelSessionJs(config, sw.out(property, `object`, identity))

  /**
   * Follows an RDF property from the current subject to a given object,
   * then applies a transformation to the resulting session.
   *
   * @example
   * {{{
   * session.out("rdf:type", "owl:Class", s => s.out("rdfs:label"))
   * }}}
   */
  @JSExport
  def out(
           property: Any,
           `object`: Any,
           f: js.Function1[UnravelSessionJs, UnravelSessionJs]
         ): UnravelSessionJs =
    UnravelSessionJs(config, sw.out(property, `object`, wrap(f)))

  /**
   * Follows an RDF property from an object back to its subject(s).
   *
   * The property and subject can be fixed IRIs or variables.
   *
   * @example
   * {{{
   * session.in("rdf:type")
   * session.in("?p")
   * session.in(Var("p"))
   * }}}
   */
  @JSExport
  def in(property: Any): UnravelSessionJs =
    UnravelSessionJs(config, sw.in(property, null, identity))

  /**
   * Follows an RDF property from a given object back to a given subject.
   *
   * Both property and subject can be fixed IRIs or variables.
   *
   * @example
   * {{{
   * session.in("rdf:type", "?s")
   * session.in("rdfs:subClassOf", "ex:Metabolite")
   * session.in(URI("http://example.org/p"), Var("s"))
   * }}}
   */
  @JSExport
  def in(property: Any, subject: Any): UnravelSessionJs =
    UnravelSessionJs(config, sw.in(property, subject, identity))

  /**
   * Follows an RDF property from a given object back to a given subject,
   * then applies a transformation to the resulting session.
   *
   * @example
   * {{{
   * session.in("rdf:type", "?s", s => s.out("rdfs:label"))
   * }}}
   */
  @JSExport
  def in(
          property: Any,
          subject: Any,
          f: js.Function1[UnravelSessionJs, UnravelSessionJs]
        ): UnravelSessionJs =
    UnravelSessionJs(config, sw.in(property, subject, wrap(f)))

  /**
   * Traverses an RDF property in either direction, combining [[out]] and [[in]].
   *
   * The starting node may be a subject or an object. Property and node
   * can be fixed IRIs or variables.
   *
   * @example
   * {{{
   * session.traverse("rdf:type")
   * session.traverse("?p")
   * session.traverse(Var("p"))
   * }}}
   */
  @JSExport
  def traverse(property: Any): UnravelSessionJs =
    UnravelSessionJs(config, sw.traverse(property, null, identity))

  /**
   * Traverses an RDF property in either direction from the given node.
   *
   * @example
   * {{{
   * session.traverse("rdf:type", "?x")
   * session.traverse("http://example.org/p", "ex:Metabolite")
   * session.traverse(URI("http://example.org/p"), Var("x"))
   * }}}
   */
  @JSExport
  def traverse(property: Any, subject: Any): UnravelSessionJs =
    UnravelSessionJs(config, sw.traverse(property, subject, identity))

  /**
   * Traverses an RDF property in either direction, then applies a transformation
   * to the resulting session.
   *
   * @example
   * {{{
   * session.traverse("rdf:type", "?x", s => s.out("rdfs:label"))
   * }}}
   */
  @JSExport
  def traverse(
                property: Any,
                subject: Any,
                f: js.Function1[UnravelSessionJs, UnravelSessionJs]
              ): UnravelSessionJs =
    UnravelSessionJs(config, sw.traverse(property, subject, wrap(f)))

  @JSExport
  def isA(term: Any): UnravelSessionJs =
    UnravelSessionJs(config, sw.isA(term))

  // ------------------------------------------------------------------ //
  //  Values                                                            //
  // ------------------------------------------------------------------ //

  @JSExport
  def set(term: Any): UnravelSessionJs =
    UnravelSessionJs(config, sw.set(term))

  @JSExport
  def setList(terms: Any*): UnravelSessionJs =
    UnravelSessionJs(config, sw.setList(terms.map(SparqlDefinition.fromAny)))

  @JSExport
  def datatype(datatypeProperty: String, ref: String): UnravelSessionJs =
    UnravelSessionJs(config, sw.datatype(datatypeProperty, ref))

  @JSExport
  def remove(focus: String): UnravelSessionJs =
    UnravelSessionJs(config, sw.remove(focus))

  // ------------------------------------------------------------------ //
  //  Decoration                                                        //
  // ------------------------------------------------------------------ //

  @JSExport
  def setDecoration(key: String, value: String): UnravelSessionJs =
    UnravelSessionJs(config, sw.setDecoration(key, value))

  @JSExport
  def getDecoration(key: String): String =
    sw.getDecoration(key)

  // ------------------------------------------------------------------ //
  //  Debug / SPARQL                                                    //
  // ------------------------------------------------------------------ //

  @JSExport
  def console(): UnravelSessionJs =
    UnravelSessionJs(config, sw.console)

  @JSExport
  def sparql(): String = sw.sparql

  @JSExport
  def sparql_get(): String = sw.sparql_get

  @JSExport
  def sparql_curl(): String = sw.sparql_curl

  @JSExport
  def getSerializedString(): String =
    sw.getSerializedString

  @JSExport
  def setSerializedString(query: String): UnravelSessionJs =
    UnravelSessionJs(config, sw.setSerializedString(query))

  @JSExport
  def browse[A](visitor: js.Function2[Dynamic, Integer, A]): js.Array[A] = {
    val visitor2: (Node, Integer) => A =
      (n, p) => visitor(JSON.parse(OptionPickler.write(n)), p)
    sw.browse(visitor2).toJSArray
  }

  // ------------------------------------------------------------------ //
  //  Queries                                                           //
  // ------------------------------------------------------------------ //

  @JSExport
  def select(lRef: String*): UnravelQueryJs =
    UnravelQueryJs(sw.select(lRef))

  @JSExport
  def select(
              lRef: js.Array[String],
              limit: Int = 0,
              offset: Int = 0
            ): UnravelQueryJs =
    UnravelQueryJs(sw.select(lRef.toSeq, limit, offset))

  @JSExport
  def selectByPage(
                    lRef: js.Array[String]
                  ): js.Promise[(Int, js.Array[UnravelQueryJs])] =
    sw.selectByPage(lRef.toSeq).map { res =>
      val n: Int            = res._1
      val l: Seq[UnravelQuery] = res._2
      (n, l.map(UnravelQueryJs(_)).toJSArray)
    }.toJSPromise

  @JSExport
  def selectByPage(lRef: String*): js.Promise[(Int, js.Array[UnravelQueryJs])] =
    selectByPage(lRef.toJSArray)

  @JSExport
  def selectDistinctByPage(
                            lRef: js.Array[String]
                          ): js.Promise[(Int, js.Array[UnravelQueryJs])] =
    sw.selectDistinctByPage(lRef.toSeq).map { res =>
      val n: Int            = res._1
      val l: Seq[UnravelQuery] = res._2
      (n, l.map(UnravelQueryJs(_)).toJSArray)
    }.toJSPromise

  @JSExport
  def selectDistinctByPage(
                            lRef: String*
                          ): js.Promise[(Int, js.Array[UnravelQueryJs])] =
    selectDistinctByPage(lRef.toJSArray)
}