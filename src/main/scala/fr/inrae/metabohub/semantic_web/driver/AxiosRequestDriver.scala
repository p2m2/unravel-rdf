package fr.inrae.metabohub.semantic_web.driver

import facade.npm.{Axios, AxiosConfig, qs}

import fr.inrae.metabohub.semantic_web.event.{UnravelRequestEvent, UnravelStateRequestEvent}
import fr.inrae.metabohub.semantic_web.exception.UnravelException
import fr.inrae.metabohub.semantic_web.sparql.QueryResult
import wvlet.log.Logger.rootLogger.debug

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.{JSON, URIUtils}

case class AxiosRequestDriver(
                               idName: String,
                               method: String,
                               url: String,
                               login: Option[String] = None,
                               password: Option[String] = None,
                               token: Option[String] = None,
                               auth: Option[String] = None
                             ) extends HttpRequestDriver {

  def requestOnSWDB(query: String): Future[QueryResult] = {
    debug(s" -- HttpRequestDriver > ${this.getClass.getName}")
    publish(UnravelRequestEvent(UnravelStateRequestEvent.START_HTTP_REQUEST))

    method.toLowerCase match {
      case "post" => post(query)
      case "get"  => get(query)
      case _      => throw HttpRequestDriverException(s"Unknown http type request: $method")
    }
  }

  private def handleError(e: Throwable): Nothing = {
    publish(UnravelRequestEvent(UnravelStateRequestEvent.ERROR_HTTP_REQUEST))

    val raw: js.Dynamic = e match {
      case jse: js.JavaScriptException =>
        jse.exception.asInstanceOf[js.Dynamic]
      case _ =>
        e.asInstanceOf[js.Dynamic]
    }

    val axiosMessage =
      if (!js.isUndefined(raw.message) && raw.message != null)
        raw.message.toString.replaceFirst("^AxiosError:\\s*", "").trim
      else
        Option(e.getMessage).getOrElse(e.toString).replaceFirst("^AxiosError:\\s*", "").trim

    val response =
      if (js.isUndefined(raw.response) || raw.response == null) null
      else raw.response

    val serverMessage =
      if (response != null && !js.isUndefined(response.data) && response.data != null) {
        val d = response.data
        if (js.typeOf(d) == "string") d.toString.trim
        else JSON.stringify(d)
      } else ""

    val finalMessage =
      if (serverMessage.nonEmpty && serverMessage != axiosMessage)
        s"$axiosMessage\n$serverMessage"
      else
        axiosMessage

    throw UnravelException(finalMessage)
  }

  def get(query: String): Future[QueryResult] = {
    publish(UnravelRequestEvent(UnravelStateRequestEvent.PROCESS_HTTP_REQUEST))

    val configAxios: AxiosConfig = js.Dynamic.literal(
      headers = js.Dictionary(
        "Accept" -> "application/json"
      )
    ).asInstanceOf[AxiosConfig]

    Axios
      .get(s"$url?query=${URIUtils.encodeURIComponent(query)}", configAxios)
      .toFuture
      .map { response =>
        publish(UnravelRequestEvent(UnravelStateRequestEvent.FINISHED_HTTP_REQUEST))
        QueryResult(JSON.stringify(response.data))
      }
      .recover { case e: Throwable => handleError(e) }
  }

  def post(query: String): Future[QueryResult] = {
    publish(UnravelRequestEvent(UnravelStateRequestEvent.PROCESS_HTTP_REQUEST))

    println("====================================")
    println("SPARQL QUERY")
    println("====================================")
    println(query)

    val configAxios: AxiosConfig = js.Dynamic.literal(
      url = url,
      method = "POST",
      headers = js.Dictionary(
        "Accept" -> "application/json",
        "Content-Type" -> "application/x-www-form-urlencoded"
      ),
      data = qs.stringify(js.Dictionary[String](
        "query" -> query
      ))
    ).asInstanceOf[AxiosConfig]

    Axios
      .request(configAxios)
      .toFuture
      .map { response =>
        publish(UnravelRequestEvent(UnravelStateRequestEvent.FINISHED_HTTP_REQUEST))
        QueryResult(JSON.stringify(response.data))
      }
      .recover { case e: Throwable => handleError(e) }
  }
}