// SPDX-FileCopyrightText: 2020-2026 INRAE
// SPDX-License-Identifier: GPL-3.0-or-later

package fr.inrae.metabohub.semantic_web.driver

import fr.inrae.metabohub.semantic_web.sparql.QueryResult

import scala.concurrent.Future

abstract class HttpRequestDriver  extends RequestDriver {
  def post(query: String): Future[QueryResult]
  def get(query: String): Future[QueryResult]
}
