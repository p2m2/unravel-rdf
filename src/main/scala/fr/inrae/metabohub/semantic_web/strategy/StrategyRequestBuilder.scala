// SPDX-FileCopyrightText: 2020-2026 INRAE
// SPDX-License-Identifier: GPL-3.0-or-later

package fr.inrae.metabohub.semantic_web.strategy

import fr.inrae.metabohub.semantic_web.driver.ComunicaFederatedStrategy
import fr.inrae.metabohub.semantic_web.exception._
import fr.inrae.metabohub.semantic_web.configuration._

/**
 * Build a strategy to request a set of web sem sources (triple store/file/inline turtle)
 * and configuration : proxy/proxyUrl
 */
object StrategyRequestBuilder {

  def build(config: UnravelConfig): StrategyRequest =
    config.sources.length match {
      case 0 => throw UnravelException("No sources specified")
      case 1 => DiscoveryStrategyRequest(config.sources.head)
      case _ => ComunicaFederatedStrategy(config.sources)
    }
}
