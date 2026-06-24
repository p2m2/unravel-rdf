package fr.inrae.metabohub.semantic_web.js

import fr.inrae.metabohub.semantic_web.UnravelQuery
import fr.inrae.metabohub.semantic_web.js.increment.ProjectionExpressionIncrementJs

import scala.scalajs.js
import scala.scalajs.js.JSConverters.JSRichFutureNonThenable
import scala.scalajs.js.Promise
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel(name="UnravelQuery")
case class UnravelQueryJs(transaction : UnravelQuery) {
  implicit val ec: scala.concurrent.ExecutionContext = scala.scalajs.concurrent.JSExecutionContext.queue

  @JSExport
  def progression(  callBack  : js.Function1[Double,Unit]  ): UnravelQueryJs = UnravelQueryJs(transaction.progression(callBack))


  @JSExport
  def requestEvent(callBack  : js.Function1[String,Unit]  ): UnravelQueryJs = UnravelQueryJs(transaction.requestEvent(callBack))

  @JSExport
  def abort(): Unit = transaction.abort()

  @JSExport
  def commit() : UnravelQueryJs = UnravelQueryJs(transaction.commit())

  @JSExport
  def raw() : Promise[Dynamic] = {
    transaction.raw.map(x => scala.scalajs.js.JSON.parse(x.toString())).toJSPromise
  }

  @JSExport
  def aggregate(`var` : String) : ProjectionExpressionIncrementJs = ProjectionExpressionIncrementJs(this,`var`)

  @JSExport
  def distinct()  : UnravelQueryJs = UnravelQueryJs(transaction.distinct)

  @JSExport
  def reduced()  : UnravelQueryJs = UnravelQueryJs(transaction.reduced)

  @JSExport
  def limit( value : Int )  : UnravelQueryJs = UnravelQueryJs(transaction.limit(value))

  @JSExport
  def offset( value : Int )  : UnravelQueryJs = UnravelQueryJs(transaction.offset(value))

  @JSExport
  def orderByAsc( ref: String )  : UnravelQueryJs = UnravelQueryJs(transaction.orderByAsc(ref))

  @JSExport
  def orderByAsc( lRef: js.Array[String] )  : UnravelQueryJs = UnravelQueryJs(transaction.orderByAsc(lRef.toSeq))

  @JSExport
  def orderByDesc( ref: String ) : UnravelQueryJs = UnravelQueryJs(transaction.orderByDesc(ref))

  @JSExport
  def orderByDesc( lRef: js.Array[String] )  : UnravelQueryJs = UnravelQueryJs(transaction.orderByDesc(lRef.toSeq))

  @JSExport
  def getSerializedString(): String = transaction.getSerializedString

  @JSExport
  def setSerializedString(transaction_string : String): UnravelQueryJs =
    UnravelQueryJs(transaction.setSerializedString(transaction_string))

  @JSExport
  def console() : UnravelQueryJs = UnravelQueryJs(transaction.console)
}
