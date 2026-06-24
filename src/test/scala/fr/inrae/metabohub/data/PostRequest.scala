package fr.inrae.metabohub.data

import facade.npm.{Axios, AxiosConfig, qs}
import fr.inrae.metabohub.semantic_web.exception.UnravelException

import scala.scalajs.js

case object PostRequest {
  implicit val ec: scala.concurrent.ExecutionContext = scala.scalajs.concurrent.JSExecutionContext.queue

  def put(stringQuery: String, url_endpoint: String) = {
    val configAxios: AxiosConfig = js.Dynamic.literal(
      url = url_endpoint,
      method = "POST",
      headers = js.Dictionary(
        "Accept" -> "application/json",
        "Content-Type" -> "application/x-www-form-urlencoded"
      ),
      data = qs.stringify(js.Dictionary[String](
        "query" -> stringQuery
      ))
    ).asInstanceOf[AxiosConfig]

    Axios
      .request(configAxios)
      .toFuture
      .map { response =>
        response
      }
      .recover { case e: Throwable =>
        throw UnravelException(Option(e.getMessage).getOrElse(e.toString))
      }
  }
}