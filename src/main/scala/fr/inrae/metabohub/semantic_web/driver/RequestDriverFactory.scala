// SPDX-FileCopyrightText: 2020-2026 INRAE
// SPDX-License-Identifier: GPL-3.0-or-later

package fr.inrae.metabohub.semantic_web.driver

import fr.inrae.metabohub.semantic_web.configuration._
import fr.inrae.metabohub.semantic_web.exception.UnravelException
import wvlet.log.Logger.rootLogger.debug

object RequestDriverFactory {
  def build() : RequestDriverFactory = {
    RequestDriverFactory()
  }
}

case class RequestDriverFactory(lCon : Seq[(RequestDriver, Unit)] = Seq())  {

  def addRepositoryConnection( source : Source ) : RequestDriverFactory = {
    val rq : RequestDriver = source.mimetype match {
      case "application/sparql-query" | /* =>
      debug("== AxiosRequestDriver == ")
       AxiosRequestDriver(
          source.id,
          source.method.getOrElse("POST"),
          source.path,
          source.login,
          source.password,
          source.token,
          source.auth)
      case*/
        "application/trig" |
        "application/n-quads" |
        "text/turtle" |
        "application/n-triples" |
        "text/n3" |
        "application/ld+json" | "application/json" |
        "application/rdf+xml" |
        "text/rdf-xml" |
        "text/html" |
        "application/xhtml+xml" |
        "image/svg+xml" |
        "application/xml" =>
        debug("== ComunicaRequestDriver FILE management== ")
          new ComunicaRequestDriver(
            source.id,
            source.resolvedPath,
            "",
            source.sourcePath,
            source.mimetype,
            source.login,
            source.password)
      case "content/rdf-xml" |  "content/turtle" |
           "content/n3" | "content/n-triples" |
           "content/ld+json" =>
        debug("== ComunicaRequestDriver CONTENT management== ")
        new ComunicaRequestDriver(
          source.id,
          "",
          source.content,
          source.sourcePath,
          source.mimetype,
          source.login,
          source.password)
      case _ =>
        throw UnravelException("Bad definition of source configuration :"+source.toString)
    }

    RequestDriverFactory(Seq( (rq,()) ))
  }

}
