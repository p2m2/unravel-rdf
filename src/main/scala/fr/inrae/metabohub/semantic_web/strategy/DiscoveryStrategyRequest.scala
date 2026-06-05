package fr.inrae.metabohub.semantic_web.strategy
import fr.inrae.metabohub.semantic_web.configuration._
import fr.inrae.metabohub.semantic_web.driver._
import fr.inrae.metabohub.semantic_web.event.{UnravelRequestEvent, UnravelStateRequestEvent, Publisher, Subscriber}
import fr.inrae.metabohub.semantic_web.{UnravelQuery, SparqlQueryBuilder}
import fr.inrae.metabohub.semantic_web.sparql.QueryResult

import scala.concurrent.Future

case class DiscoveryStrategyRequest(source : Source) extends StrategyRequest {

  val driver : RequestDriver = RequestDriverFactory.build().addRepositoryConnection(source).lCon.map(_._1).last

  driver.subscribe(this.asInstanceOf[Subscriber[UnravelRequestEvent,Publisher[UnravelRequestEvent]]])

  def execute(swt : UnravelQuery) : Future[QueryResult] = {

    publish(UnravelRequestEvent(UnravelStateRequestEvent.QUERY_BUILD))
    val query: String = SparqlQueryBuilder.selectQueryString(swt.sw.rootNode)
    driver.request(query)
  }

  def request(query: String): Future[QueryResult] = driver.request(query)

}
