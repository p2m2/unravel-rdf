/**
 * olivier.filangi@inrae.fr - P2M2 Platform
 * Project: https://forge.inrae.fr/p2m2/discovery
 */

package facade.npm

import scala.scalajs.js
import scala.scalajs.js.annotation._

@js.native
trait AxiosInstance extends js.Object {
  def request(config: AxiosConfig): js.Promise[AxiosResponse] = js.native
  def get(url: String, config: AxiosConfig = null): js.Promise[AxiosResponse] = js.native
  def delete(url: String, config: AxiosConfig = null): js.Promise[AxiosResponse] = js.native
  def head(url: String, config: AxiosConfig = null): js.Promise[AxiosResponse] = js.native
  def options(url: String, config: AxiosConfig = null): js.Promise[AxiosResponse] = js.native
  def post(url: String, data: js.Any = null, config: AxiosConfig = null): js.Promise[AxiosResponse] = js.native
  def put(url: String, data: js.Any = null, config: AxiosConfig = null): js.Promise[AxiosResponse] = js.native
  def patch(url: String, data: js.Any = null, config: AxiosConfig = null): js.Promise[AxiosResponse] = js.native
}

@js.native
@JSImport("axios", JSImport.Default)
object Axios extends AxiosInstance {
  var defaults: AxiosConfig = js.native
  def create(config: AxiosConfig = null): AxiosInstance = js.native
}

@js.native
trait AxiosConfig extends js.Object {
  var url: String = js.native
  var method: String = js.native
  var baseURL: String = js.native
  var headers: js.Dictionary[String] = js.native
  var params: js.Dictionary[js.Any] = js.native
  var data: js.Any = js.native
  var timeout: Int = js.native
  var withCredentials: Boolean = js.native
  var responseType: String = js.native
  var xsrfCookieName: String = js.native
  var xsrfHeaderName: String = js.native
  var maxContentLength: Int = js.native
  var maxRedirects: Int = js.native
  var auth: AxiosAuth = js.native
  var proxy: AxiosProxy = js.native
  var httpAgent: js.Any = js.native
  var httpsAgent: js.Any = js.native
  var cancelToken: js.Any = js.native
  var signal: js.Any = js.native
}

object AxiosConfig {
  def apply(
             url: String = null,
             method: String = null,
             baseURL: String = null,
             headers: js.Dictionary[String] = null,
             params: js.Dictionary[js.Any] = null,
             data: js.Any = null,
             timeout: Int = 0,
             withCredentials: Boolean = false,
             responseType: String = null
           ): AxiosConfig =
    js.Dynamic.literal(
      url = url,
      method = method,
      baseURL = baseURL,
      headers = headers,
      params = params,
      data = data,
      timeout = timeout,
      withCredentials = withCredentials,
      responseType = responseType
    ).asInstanceOf[AxiosConfig]
}

@js.native
trait AxiosResponse extends js.Object {
  val data: js.Any = js.native
  val status: Int = js.native
  val statusText: String = js.native
  val headers: js.Dictionary[String] = js.native
  val config: AxiosConfig = js.native
  val request: js.Any = js.native
}

@js.native
trait AxiosAuth extends js.Object {
  var username: String = js.native
  var password: String = js.native
}

object AxiosAuth {
  def apply(username: String, password: String): AxiosAuth =
    js.Dynamic.literal(
      username = username,
      password = password
    ).asInstanceOf[AxiosAuth]
}

@js.native
trait AxiosProxy extends js.Object {
  var host: String = js.native
  var port: Int = js.native
  var auth: AxiosAuth = js.native
}

object AxiosProxy {
  def apply(host: String, port: Int, auth: AxiosAuth = null): AxiosProxy =
    js.Dynamic.literal(
      host = host,
      port = port,
      auth = auth
    ).asInstanceOf[AxiosProxy]
}

sealed abstract class ResponseType(val value: String) {
  override def toString: String = value
}

case object ArrayBufferResponseType extends ResponseType("arraybuffer")
case object BlobResponseType extends ResponseType("blob")
case object DocumentResponseType extends ResponseType("document")
case object JsonResponseType extends ResponseType("json")
case object TextResponseType extends ResponseType("text")
case object StreamResponseType extends ResponseType("stream")