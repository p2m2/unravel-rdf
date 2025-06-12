package facade.npm

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("showdown", "Converter")
class ShowdownConverter(options: js.UndefOr[js.Object] = js.undefined) extends js.Object {
  def makeHtml(text: String): String = js.native
}
