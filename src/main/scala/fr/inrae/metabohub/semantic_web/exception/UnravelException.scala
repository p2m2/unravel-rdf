package fr.inrae.metabohub.semantic_web.exception

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel(name="UnravelException")
final case class UnravelException(private val messageInt: String = "",
                                  private val cause: Throwable = None.orNull) extends Exception(messageInt,cause) {
  @JSExport
  def message : String =  messageInt

}