package fr.inrae.metabohub.semantic_web.driver

import fr.inrae.metabohub.semantic_web.event.{UnravelRequestEvent, UnravelStateRequestEvent, Publisher}
import fr.inrae.metabohub.semantic_web.sparql.QueryResult
import wvlet.log.Logger.rootLogger.debug

import scala.concurrent.Future

trait RequestDriver extends Publisher[UnravelRequestEvent] {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  def request(query: String): Future[QueryResult] = {

    publish(UnravelRequestEvent(UnravelStateRequestEvent.START_HTTP_REQUEST))
    val dateStart = System.nanoTime
    val t1 = System.nanoTime
    debug("RequestDriver Send request "+dateStart+","+t1)
    
    requestOnSWDB(query).map(resultsQR => {

      publish(UnravelRequestEvent(UnravelStateRequestEvent.RESULTS_BUILD))
      val duration = (System.nanoTime - t1) / 1e9d
      debug(s"RequestDriver Receive results  -- Elapsed Time : ${duration}")
      debug("RequestDriver Memorize (Mb) =>"+(resultsQR.results.length.toDouble/(1024*1024)))
      publish(UnravelRequestEvent(UnravelStateRequestEvent.RESULTS_DONE))
      resultsQR
    })
  }

  protected def requestOnSWDB(query: String): Future[QueryResult]
}
