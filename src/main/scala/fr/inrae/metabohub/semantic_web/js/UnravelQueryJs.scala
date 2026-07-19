// SPDX-FileCopyrightText: 2020-2026 INRAE
// SPDX-License-Identifier: GPL-3.0-or-later

package fr.inrae.metabohub.semantic_web.js

import fr.inrae.metabohub.semantic_web.UnravelQuery
import fr.inrae.metabohub.semantic_web.js.increment.ProjectionExpressionIncrementJs

import scala.scalajs.js
import scala.scalajs.js.Dynamic
import scala.scalajs.js.JSConverters.JSRichFutureNonThenable
import scala.scalajs.js.Promise
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

/**
 * Query object returned by session selection methods.
 *
 * This object is used to configure, execute, serialize, and inspect a query.
 * Most methods return a new query object so calls can be chained fluently.
 *
 * ==Executing query==
 *
 * ===Methods===
 *
 *  - `select(var1,var2,var3,..)` builds a query from a session
 *  - `commit()` prepares the query execution
 *  - `raw()` gets results as a JavaScript promise
 *  - `selectByPage(var1,var2,var3,..)` gets paginated results as a promise
 *
 * ===Pattern: getting results===
 *
 * {{{
 * UnravelSession()
 *   .something("some")
 *   .select("some")
 *   .commit()
 *   .raw()
 *   .then((response: scala.scalajs.js.Dynamic) => {
 *     val bindings = response.results.bindings
 *   })
 * }}}
 *
 * ===Pattern: paginated results===
 *
 * {{{
 * UnravelSession()
 *   .something("some")
 *   .selectByPage("some")
 *   .`then` { args =>
 *     val numberOfPages = args._1
 *     val lazyPages = args._2
 *   }
 * }}}
 *
 * ===Transport===
 *
 *  - `getSerializedString()`
 *  - `setSerializedString(...)`
 */
@JSExportTopLevel(name = "UnravelQuery")
case class UnravelQueryJs(transaction: UnravelQuery) {
  implicit val ec: scala.concurrent.ExecutionContext =
    scala.scalajs.concurrent.JSExecutionContext.queue

  /**
   * Adds a progression callback to the query.
   *
   * The callback receives a numeric progress indicator during execution.
   *
   * @example
   * {{{
   * query.progression(p => println("progress = " + p))
   * }}}
   */
  @JSExport
  def progression(callBack: js.Function1[Double, Unit]): UnravelQueryJs =
    UnravelQueryJs(transaction.progression(callBack))

  /**
   * Adds a request event callback to the query.
   *
   * This can be used to observe request lifecycle messages.
   *
   * @example
   * {{{
   * query.requestEvent(evt => println(evt))
   * }}}
   */
  @JSExport
  def requestEvent(callBack: js.Function1[String, Unit]): UnravelQueryJs =
    UnravelQueryJs(transaction.requestEvent(callBack))

  /**
   * Aborts the current query execution.
   *
   * @example
   * {{{
   * query.abort()
   * }}}
   */
  @JSExport
  def abort(): Unit = transaction.abort()

  /**
   * Commits the query before execution.
   *
   * This method is usually called before [[raw]].
   *
   * @example
   * {{{
   * query.commit().raw()
   * }}}
   */
  @JSExport
  def commit(): UnravelQueryJs =
    UnravelQueryJs(transaction.commit())

  /**
   * Executes the query and returns the raw JSON result as a JavaScript promise.
   *
   * This is the main way to retrieve SPARQL results from JavaScript.
   *
   * ===Pattern: unit block results===
   *
   * {{{
   * query
   *   .commit()
   *   .raw()
   *   .then((response: scala.scalajs.js.Dynamic) => {
   *     for (i <- 0 until response.results.bindings.length.asInstanceOf[Int]) {
   *       println(response.results.bindings(i).selectDynamic("some").selectDynamic("value"))
   *     }
   *   })
   * }}}
   *
   * ===Pattern: datatype results===
   *
   * {{{
   * session
   *   .datatype("datatypeSome", "?some")
   *   .select("some")
   *   .commit()
   *   .raw()
   *   .then((response: scala.scalajs.js.Dynamic) => {
   *     val uri = response.results.bindings(0).selectDynamic("some").selectDynamic("value")
   *     val datatypes = response.results.datatypes.selectDynamic("datatypeSome").selectDynamic(uri.toString)
   *   })
   * }}}
   */
  @JSExport
  def raw(): Promise[Dynamic] = {
    transaction.raw.map(x => scala.scalajs.js.JSON.parse(x.toString())).toJSPromise
  }

  /**
   * Starts building an aggregate expression on the given variable.
   *
   * @example
   * {{{
   * query.aggregate("?count")
   * }}}
   */
  @JSExport
  def aggregate(`var`: String): ProjectionExpressionIncrementJs =
    ProjectionExpressionIncrementJs(this, `var`)

  /**
   * Marks the query as `DISTINCT`.
   *
   * @example
   * {{{
   * query.distinct()
   * }}}
   */
  @JSExport
  def distinct(): UnravelQueryJs =
    UnravelQueryJs(transaction.distinct)

  /**
   * Marks the query as `REDUCED`.
   *
   * @example
   * {{{
   * query.reduced()
   * }}}
   */
  @JSExport
  def reduced(): UnravelQueryJs =
    UnravelQueryJs(transaction.reduced)

  /**
   * Limits the number of returned results.
   *
   * @example
   * {{{
   * query.limit(10)
   * }}}
   */
  @JSExport
  def limit(value: Int): UnravelQueryJs =
    UnravelQueryJs(transaction.limit(value))

  /**
   * Sets the offset of returned results.
   *
   * @example
   * {{{
   * query.offset(20)
   * }}}
   */
  @JSExport
  def offset(value: Int): UnravelQueryJs =
    UnravelQueryJs(transaction.offset(value))

  /**
   * Orders results ascendingly by one variable.
   *
   * @example
   * {{{
   * query.orderByAsc("?label")
   * }}}
   */
  @JSExport
  def orderByAsc(ref: String): UnravelQueryJs =
    UnravelQueryJs(transaction.orderByAsc(ref))

  /**
   * Orders results ascendingly by several variables.
   *
   * @example
   * {{{
   * query.orderByAsc(js.Array("?type", "?label"))
   * }}}
   */
  @JSExport
  def orderByAsc(lRef: js.Array[String]): UnravelQueryJs =
    UnravelQueryJs(transaction.orderByAsc(lRef.toSeq))

  /**
   * Orders results descendingly by one variable.
   *
   * @example
   * {{{
   * query.orderByDesc("?label")
   * }}}
   */
  @JSExport
  def orderByDesc(ref: String): UnravelQueryJs =
    UnravelQueryJs(transaction.orderByDesc(ref))

  /**
   * Orders results descendingly by several variables.
   *
   * @example
   * {{{
   * query.orderByDesc(js.Array("?type", "?label"))
   * }}}
   */
  @JSExport
  def orderByDesc(lRef: js.Array[String]): UnravelQueryJs =
    UnravelQueryJs(transaction.orderByDesc(lRef.toSeq))

  /**
   * Serializes the current query as a string.
   *
   * This can be used to transport or persist a query.
   *
   * @example
   * {{{
   * val s = query.getSerializedString()
   * }}}
   */
  @JSExport
  def getSerializedString(): String = transaction.getSerializedString

  /**
   * Restores a query from a serialized string.
   *
   * @example
   * {{{
   * val q2 = query.setSerializedString(serialized)
   * }}}
   */
  @JSExport
  def setSerializedString(transaction_string: String): UnravelQueryJs =
    UnravelQueryJs(transaction.setSerializedString(transaction_string))

  /**
   * Displays the current query as text in the console for debugging.
   *
   * @example
   * {{{
   * query.console()
   * }}}
   */
  @JSExport
  def console(): UnravelQueryJs =
    UnravelQueryJs(transaction.console)

  /**
   * Displays the current query in the interactive debug screen.
   *
   * @example
   * {{{
   * query.showDebugScreen()
   * }}}
   */
  @JSExport
  def showDebugScreen(): UnravelQueryJs =
    UnravelQueryJs(transaction.showDebugScreen)


}