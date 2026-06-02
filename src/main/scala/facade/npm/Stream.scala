/**
 * olivier.filangi@inrae.fr - P2M2 Platform
 * Project: https://forge.inrae.fr/p2m2/discovery
 */

package facade.npm

import scala.scalajs.js
import scala.scalajs.js.annotation._

@js.native
trait JsEventEmitter extends js.Object {
  def on(event: String, listener: js.Function): this.type = js.native
}

@js.native
trait JsReadableStream extends JsEventEmitter {
  def pipe(dest: js.Any): js.Any = js.native
}

@js.native
trait JsWritableStream extends JsEventEmitter