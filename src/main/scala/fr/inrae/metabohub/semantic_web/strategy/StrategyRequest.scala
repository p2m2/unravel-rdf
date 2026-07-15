// SPDX-FileCopyrightText: 2020-2026 INRAE
// SPDX-License-Identifier: GPL-3.0-or-later

package fr.inrae.metabohub.semantic_web.strategy

import fr.inrae.metabohub.semantic_web.UnravelQuery
import fr.inrae.metabohub.semantic_web.driver.RequestDriver
import fr.inrae.metabohub.semantic_web.event.{UnravelRequestEvent, Publisher, Subscriber}
import fr.inrae.metabohub.semantic_web.sparql.QueryResult

import scala.concurrent.Future


/** Management of discovery requests
 *
 *  This is further documentation of what we're documenting.
 *  Here are more details about how it works and what it does.
 */
abstract class StrategyRequest()
  extends Publisher[UnravelRequestEvent]
    with Subscriber[UnravelRequestEvent,RequestDriver] {

  def execute(swt : UnravelQuery) : Future[QueryResult]

  def request(query: String): Future[QueryResult]

  def notify(pub: RequestDriver, event: UnravelRequestEvent): Unit = {
    publish(UnravelRequestEvent(event.state))
  }

}
