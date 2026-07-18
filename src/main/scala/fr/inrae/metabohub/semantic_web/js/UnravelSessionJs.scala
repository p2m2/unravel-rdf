// SPDX-FileCopyrightText: 2020-2026 INRAE
// SPDX-License-Identifier: GPL-3.0-or-later

package fr.inrae.metabohub.semantic_web.js

import fr.inrae.metabohub.semantic_web.{UnravelQuery, UnravelSession}
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
 * This class exposes the fluent Unravel traversal API to JavaScript and Scala.js.
 * Each method returns a new session wrapper unless stated otherwise.
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
 * @note Traversal methods accept either string shortcuts or explicit RDF values.
 *
 * @example
 * {{{
 * val session = UnravelSession()
 *   .prefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#")
 *   .out("rdf:type", "owl:Class")
 *   .out("rdfs:label")
 * }}}
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

  /**
   * Converts a JavaScript-facing value into an RDF IRI.
   *
   * Accepted values are:
   *  - `IRI`
   *  - `URI`
   *  - `String`
   *
   * @throws UnravelException if the value cannot be interpreted as an IRI.
   */
  def toIRI(any: Any): IRI = any match {
    case v: IRI    => v
    case v: URI    => IRI(v.sparql)
    case s: String => s
    case _         =>
      throw UnravelException(
        s"Unsupported IRI value: $any (expected IRI, URI, or String)"
      )
  }

  /**
   * Adapts a JavaScript closure on `UnravelSessionJs`
   * into a Scala closure on `UnravelSession`.
   */
  private def wrap(
                    f: js.Function1[UnravelSessionJs, UnravelSessionJs]
                  ): UnravelSession => UnravelSession =
    inner => f(UnravelSessionJs(config, inner)).sw

  // ------------------------------------------------------------------ //
  //  Accessors / configuration                                         //
  // ------------------------------------------------------------------ //

  /**
   * Exposes incremental filter helpers for the current session.
   *
   * @example
   * {{{
   * session.filter
   * }}}
   */
  @JSExport
  val filter: FilterIncrementJs = FilterIncrementJs(this)

  /**
   * Opens an HTML helper view for the current session.
   *
   * When a regular expression is provided, the helper view can use it
   * to highlight or restrict displayed content.
   *
   * @example
   * {{{
   * session.helper()
   * session.helper("label|name")
   * }}}
   */
  @JSExport
  def helper(regex: String = ""): UnravelSessionJs = {
    HtmlView(sw, regex)
    UnravelSessionJs(config, sw)
  }

  /**
   * Starts a bind helper for the given variable name.
   *
   * @example
   * {{{
   * session.bind("?x")
   * }}}
   */
  @JSExport
  def bind(`var`: String): BindIncrementJs =
    BindIncrementJs(this, `var`)

  /**
   * Returns helper utilities around the current session.
   *
   * @example
   * {{{
   * val h = session.finder
   * }}}
   */
  @JSExport
  def finder: UnravelSessionHelperJs =
    UnravelSessionHelperJs(sw)

  /**
   * Returns a new session with an updated configuration.
   *
   * This lets you change how Unravel behaves while keeping the session fluent.
   *
   * @example
   * {{{
   * val cfg2 = session.getConfig().copy(pageSize = 500)
   * val session2 = session.setConfig(cfg2)
   * }}}
   */
  @JSExport
  def setConfig(newConfig: UnravelConfig): UnravelSessionJs =
    UnravelSessionJs(newConfig, sw.setConfig(newConfig))

  /**
   * Returns the current configuration of this session.
   *
   * @example
   * {{{
   * val cfg = session.getConfig()
   * }}}
   */
  @JSExport
  def getConfig(): UnravelConfig = sw.getConfig

  /**
   * Registers or overrides a namespace prefix in the current session.
   *
   * This allows compact names such as `ex:Resource` to be used in later calls.
   *
   * @example
   * {{{
   * val s2 = session.prefix("ex", "http://example.org/")
   * }}}
   */
  @JSExport
  def prefix(short: String, long: Any): UnravelSessionJs =
    UnravelSessionJs(config, sw.prefix(short, toIRI(long)))

  /**
   * Resolves a previously registered namespace prefix.
   *
   * @example
   * {{{
   * val base = session.getPrefix("ex")
   * }}}
   */
  @JSExport
  def getPrefix(short: String): Any =
    sw.getPrefix(short)

  /**
   * Applies a raw directive to the underlying session.
   *
   * Use this when you want to pass a low-level directive string directly.
   *
   * @example
   * {{{
   * val s2 = session.directive("distinct")
   * }}}
   */
  @JSExport
  def directive(directive: String): UnravelSessionJs =
    UnravelSessionJs(config, sw.directive(directive))

  /**
   * Sets the active graph for subsequent traversal operations.
   *
   * @example
   * {{{
   * val s2 = session.graph("http://example.org/graph")
   * }}}
   */
  @JSExport
  def graph(graph: Any): UnravelSessionJs =
    UnravelSessionJs(config, sw.graph(toIRI(graph)))

  /**
   * Returns a session focused on the traversal root.
   *
   * @example
   * {{{
   * val rootSession = session.root()
   * }}}
   */
  @JSExport
  def root(): UnravelSessionJs =
    UnravelSessionJs(config, sw.root)

  /**
   * Returns the current focus node reference.
   *
   * @example
   * {{{
   * val ref = session.current()
   * }}}
   */
  @JSExport
  def current(): String = sw.focusNode

  /**
   * Sets the active named graph for subsequent operations.
   *
   * @example
   * {{{
   * val s2 = session.namedGraph("http://example.org/graph")
   * }}}
   */
  @JSExport
  def namedGraph(graph: Any): UnravelSessionJs =
    UnravelSessionJs(config, sw.namedGraph(toIRI(graph)))

  /**
   * Positions the cursor on the given reference.
   *
   * @example
   * {{{
   * val s2 = session.from("?x")
   * }}}
   */
  @JSExport
  def from(ref: String): UnravelSessionJs =
    UnravelSessionJs(config, sw.from(ref))

  /**
   * Positions the cursor on the given reference, applies a transformation,
   * then restores the parent focus.
   *
   * @example
   * {{{
   * session.from("?x", s => s.out("rdfs:label"))
   * }}}
   */
  @JSExport
  def from(
            ref: String,
            f: js.Function1[UnravelSessionJs, UnravelSessionJs]
          ): UnravelSessionJs =
    UnravelSessionJs(config, sw.from(ref, wrap(f)))

  /**
   * Focuses the session on a non-null / existing reference.
   *
   * @example
   * {{{
   * val s2 = session.something("?x")
   * }}}
   */
  @JSExport
  def something(ref: String): UnravelSessionJs =
    UnravelSessionJs(config, sw.something(ref))

  /**
   * Focuses the session on a non-null / existing reference,
   * then applies a transformation.
   *
   * @example
   * {{{
   * session.something("?x", s => s.out("rdfs:label"))
   * }}}
   */
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
   * Follows an RDF property from the current subject to its object or objects.
   *
   * The property may be a fixed IRI or a variable.
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
   * Follows an RDF property from the current subject to the given object.
   *
   * The property and object may be fixed RDF terms or variables.
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
   * Follows an RDF property from the current subject to the given object,
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
   * Follows an RDF property backward from the current object to its subject or subjects.
   *
   * The property may be a fixed IRI or a variable.
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
   * Follows an RDF property backward from the current object to the given subject.
   *
   * The property and subject may be fixed RDF terms or variables.
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
   * Follows an RDF property backward from the current object to the given subject,
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
   * Traverses an RDF property in either direction from the current focus.
   *
   * This combines forward and backward traversal semantics.
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
   * Traverses an RDF property in either direction,
   * then applies a transformation to the resulting session.
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

  /**
   * Restricts the current focus to nodes having the given RDF type.
   *
   * @example
   * {{{
   * val s2 = session.isA("owl:Class")
   * }}}
   */
  @JSExport
  def isA(term: Any): UnravelSessionJs =
    UnravelSessionJs(config, sw.isA(term))

  // ------------------------------------------------------------------ //
  //  Values                                                            //
  // ------------------------------------------------------------------ //

  /**
   * Sets the current focus to the given term.
   *
   * @example
   * {{{
   * val s2 = session.set("ex:Metabolite")
   * }}}
   */
  @JSExport
  def set(term: Any): UnravelSessionJs =
    UnravelSessionJs(config, sw.set(term))

  /**
   * Sets the current focus to the given list of terms.
   *
   * @example
   * {{{
   * val s2 = session.setList("ex:A", "ex:B", "ex:C")
   * }}}
   */
  @JSExport
  def setList(terms: Any*): UnravelSessionJs =
    UnravelSessionJs(config, sw.setList(terms.map(SparqlDefinition.fromAny)))

  /**
   * Declares or constrains a datatype property using the given reference.
   *
   * @example
   * {{{
   * val s2 = session.datatype("xsd:string", "?value")
   * }}}
   */
  @JSExport
  def datatype(datatypeProperty: String, ref: String): UnravelSessionJs =
    UnravelSessionJs(config, sw.datatype(datatypeProperty, ref))

  /**
   * Removes the given focus reference from the current session.
   *
   * @example
   * {{{
   * val s2 = session.remove("?x")
   * }}}
   */
  @JSExport
  def remove(focus: String): UnravelSessionJs =
    UnravelSessionJs(config, sw.remove(focus))

  // ------------------------------------------------------------------ //
  //  Decoration                                                        //
  // ------------------------------------------------------------------ //

  /**
   * Associates a decoration value with the given key.
   *
   * Decorations can be used by views or helper layers to store display metadata.
   *
   * @example
   * {{{
   * val s2 = session.setDecoration("color", "green")
   * }}}
   */
  @JSExport
  def setDecoration(key: String, value: String): UnravelSessionJs =
    UnravelSessionJs(config, sw.setDecoration(key, value))

  /**
   * Returns the decoration value associated with the given key.
   *
   * @example
   * {{{
   * val color = session.getDecoration("color")
   * }}}
   */
  @JSExport
  def getDecoration(key: String): String =
    sw.getDecoration(key)

  // ------------------------------------------------------------------ //
  //  Debug / SPARQL                                                    //
  // ------------------------------------------------------------------ //

  /**
   * Renders the current session as a plain-text debug report.
   *
   * This method is useful for local debugging, console-based tests,
   * and non-HTML environments such as Node.js.
   *
   * @example
   * {{{
   * session.console()
   * }}}
   */
  @JSExport
  def console(): UnravelSessionJs =
    UnravelSessionJs(config, sw.console)

  /**
   * Renders the current session in the browser as an HTML debug screen.
   *
   * This view is intended for interactive debugging in a web page and
   * provides a richer developer experience than the plain console output.
   *
   * @example
   * {{{
   * session.renderConsole()
   * }}}
   */
  @JSExport
  def renderConsole(): UnravelSessionJs =
    UnravelSessionJs(config, sw.renderConsole)
  /**
   * Returns the SPARQL query corresponding to the current session.
   *
   * @example
   * {{{
   * val q = session.sparql()
   * }}}
   */
  @JSExport
  def sparql(): String = sw.sparql

  /**
   * Returns the SPARQL query encoded as a GET request payload.
   *
   * @example
   * {{{
   * val q = session.sparql_get()
   * }}}
   */
  @JSExport
  def sparql_get(): String = sw.sparql_get

  /**
   * Returns the SPARQL query as a curl command.
   *
   * @example
   * {{{
   * val c = session.sparql_curl()
   * }}}
   */
  @JSExport
  def sparql_curl(): String = sw.sparql_curl

  /**
   * Serializes the current session to a string form.
   *
   * @example
   * {{{
   * val serialized = session.getSerializedString()
   * }}}
   */
  @JSExport
  def getSerializedString(): String =
    sw.getSerializedString

  /**
   * Restores a session from its serialized string form.
   *
   * @example
   * {{{
   * val restored = session.setSerializedString(serialized)
   * }}}
   */
  @JSExport
  def setSerializedString(query: String): UnravelSessionJs =
    UnravelSessionJs(config, sw.setSerializedString(query))

  /**
   * Browses the current session tree and applies the given visitor
   * to each visited node.
   *
   * The visitor receives:
   *  - the serialized node as a JavaScript object;
   *  - the depth or position integer provided by the underlying traversal.
   *
   * @example
   * {{{
   * val labels = session.browse { (node, depth) =>
   *   s"$depth -> " + node.toString()
   * }
   * }}}
   */
  @JSExport
  def browse[A](visitor: js.Function2[Dynamic, Integer, A]): js.Array[A] = {
    val visitor2: (Node, Integer) => A =
      (n, p) => visitor(JSON.parse(OptionPickler.write(n)), p)
    sw.browse(visitor2).toJSArray
  }

  // ------------------------------------------------------------------ //
  //  Queries                                                           //
  // ------------------------------------------------------------------ //

  /**
   * Builds a select query from the given references.
   *
   * @example
   * {{{
   * val q = session.select("?s", "?p", "?o")
   * }}}
   */
  @JSExport
  def select(lRef: String*): UnravelQueryJs =
    UnravelQueryJs(sw.select(lRef))

  /**
   * Builds a select query from the given references, with optional limit and offset.
   *
   * @example
   * {{{
   * val q = session.select(js.Array("?s", "?p"), limit = 100, offset = 0)
   * }}}
   */
  @JSExport
  def select(
              lRef: js.Array[String],
              limit: Int = 0,
              offset: Int = 0
            ): UnravelQueryJs =
    UnravelQueryJs(sw.select(lRef.toSeq, limit, offset))

  class PagedResult(
                    val totalCount: Int,
                    val pageSize: Int,
                    val pageQueries: js.Array[UnravelQueryJs]
                  ) extends js.Object

  /**
   * Executes a paged select query on the given references.
   *
   * The returned promise contains:
   *  - the total number of matching results;
   *  - the number of available pages;
   *  - one lazy query per page.
   *
   * @example
   * {{{
   * session.selectByPage(js.Array("?s", "?label"))
   * }}}
   */
  @JSExport
  def selectByPage(
                    lRef: js.Array[String]
                  ): js.Promise[PagedResult] =
    sw.selectByPage(lRef.toSeq).map { res =>
      val totalCount = res._1
      val pages = res._2.map(UnravelQueryJs(_)).toJSArray

      new PagedResult(
        totalCount = totalCount,
        pageSize = pages.length,
        pageQueries = pages
      )
    }.toJSPromise

  /**
   * Executes a paged select query on the given references.
   *
   * This overload accepts varargs instead of a JavaScript array.
   *
   * @example
   * {{{
   * session.selectByPage("?s", "?label")
   * }}}
   */
  @JSExport
  def selectByPage(lRef: String*): js.Promise[PagedResult] =
    selectByPage(lRef.toJSArray)

  /**
   * Executes a paged distinct select query on the given references.
   *
   * @example
   * {{{
   * session.selectDistinctByPage(js.Array("?s", "?label"))
   * }}}
   */
  @JSExport
  def selectDistinctByPage(
                            lRef: js.Array[String]
                          ): js.Promise[PagedResult] =
    sw.selectDistinctByPage(lRef.toSeq).map { res =>
      val totalCount = res._1
      val pages = res._2.map(UnravelQueryJs(_)).toJSArray

      new PagedResult(
        totalCount = totalCount,
        pageSize = pages.length,
        pageQueries = pages
      )
    }.toJSPromise


  /**
   * Executes a paged distinct select query on the given references.
   *
   * This overload accepts varargs instead of a JavaScript array.
   *
   * @example
   * {{{
   * session.selectDistinctByPage("?s", "?label")
   * }}}
   */
  @JSExport
  def selectDistinctByPage(
                            lRef: String*
                          ): js.Promise[PagedResult] =
    selectDistinctByPage(lRef.toJSArray)
}