// SPDX-FileCopyrightText: 2020-2026 INRAE
// SPDX-License-Identifier: GPL-3.0-or-later

package fr.inrae.metabohub.semantic_web

import fr.inrae.metabohub.semantic_web.rdf.{Literal, Var, SparqlBuilder, URI}
import wvlet.log.Logger.rootLogger.debug

import scala.concurrent.Future

case class UnravelSessionHelper(sw : UnravelSession) {
  implicit val ec: scala.concurrent.ExecutionContext = scala.scalajs.concurrent.JSExecutionContext.queue

  private val regex_avoid_prefix: Literal[String] = Literal("^(" + List(
    "http://www.openlinksw.com/schemas/virtrdf#",
    "http://www.w3.org/2002/07/owl#",
    "http://www.w3.org/2000/01/rdf-schema#",
    "http://www.w3.org/1999/02/22-rdf-syntax-ns"
  ).mkString("|") + ")")

  def count(lRef : Seq[String],distinct : Boolean = false) : Future[Int] = {
    sw
      .transaction
      .projection
      .aggregate("count")
      .count(lRef,distinct)
      .commit()
      .raw
      .map( json => {
        SparqlBuilder.createLiteral(json("results")("bindings")(0)("count")).toInt
      })
  }

  /**
   * Discovery search functionalities
   *
   */

  def classes(regex : String="", motherClass: URI = URI(""), page : Int =0) : Future[Seq[URI]] = {
    debug(" -- findClasses -- ")
    val query = (motherClass match {
      case uri : URI if uri == URI("")  => sw.out(URI("a"),"?espTypeInternal")
      case _ : URI =>  sw.out(URI("a"),"?espTypeInternal",
          _.out(URI("a"),motherClass))
    }).from("espTypeInternal")
      .filter.not.regex(regex_avoid_prefix)

    (if ( regex.trim != "")
          query.from("espTypeInternal").filter.regex(regex)
      else
        query)
      .selectByPage(List("espTypeInternal"))
      .flatMap(  v => {
        val futurePages : Seq[UnravelQuery] = v._2

        if ( futurePages.length > page ) {
          futurePages(page)
            .commit()
            .raw
            .map( json => {
              json("results")("bindings").arr.map(
                row => SparqlBuilder.createUri(row("espTypeInternal"))
              ).toSeq.distinct
            })
        } else {
          Future { Seq[URI]() }
        }
      })
  }

  private def properties(regex : String="", motherClassProperties: URI = URI(""), kind : String, page : Int) : Future[Seq[URI]] = {
    debug(" -- findProperties -- ")

    /* inherited from something ??? */
    val state = if (motherClassProperties != URI("")) {
      sw.root
        .something("espTypeInternal",
          _.out(Var("espPropertyInternal"),Var("espTypeInternal"),
            _.out(URI("a"),motherClassProperties)))
    } else {
      sw.root
        .something("espTypeInternal",_.out(Var("espPropertyInternal"),Var("espTypeInternal")))
    }

    /* object or datatype properties owl def. */
    val query = ( kind  match {
      case "objectProperty" => state.from("espTypeInternal",_.filter.isUri)
      case "datatypeProperty" => state.from("espTypeInternal",_.filter.isLiteral)
      case _ => state
    }).from("espPropertyInternal",_.filter.not.regex(regex_avoid_prefix))

    (if ( regex.trim.nonEmpty)
      query.from("espPropertyInternal",_.filter.regex(regex))
    else
      query)
      .selectByPage(List("espPropertyInternal"))
      .flatMap(  v => {
        val futurePages : Seq[UnravelQuery] = v._2
        if ( futurePages.length > page ) {
          futurePages(page)
            .distinct
            .commit()
            .raw
            .map( json => {
              json("results")("bindings").arr.map(
                row => {
                  SparqlBuilder.createUri(row("espPropertyInternal")) }
              ).toSeq.distinct
            })
        } else {
          Future { Seq[URI]() }
        }
      })


  }

  def objectProperties(regex : String="", motherClassProperties: URI = URI(""), page : Int = 0 ) : Future[Seq[URI]] = {
    debug(" -- findObjectProperties -- ")
    properties(regex,motherClassProperties,"objectProperty",page)
  }

  def datatypeProperties(regex : String="", motherClassProperties: URI = URI(""), page : Int = 0 ) : Future[Seq[URI]] = {
    debug(" -- findDatatypeProperties -- ")
    properties(regex,motherClassProperties,"datatypeProperty",page)
  }

  /* backward */
  def subjectProperties(regex : String="", motherClassProperties: URI = URI(""), page : Int = 0 ) : Future[Seq[URI]] = {
    debug(" -- findSubjectProperties -- ")
    //println(s"focus node:${sw.focusNode}")
    val query = (if (motherClassProperties != URI("")) {
      sw.root
        .something("espTypeInternal",
          _.in(Var("espPropertyInternal"),Var(sw.focusNode))
           .out(URI("rdf:type"), motherClassProperties)
          )

    } else {
      sw.in(Var("espPropertyInternal"))
    })//.something("espPropertyInternal",_.filter.not.regex(regex_avoid_prefix))

    (if ( regex.trim != "")
      query.something("espPropertyInternal",_.filter.regex(regex))
    else
      query)

      .selectByPage(List("espPropertyInternal"))
      .flatMap(  v => {
        val futurePages : Seq[UnravelQuery] = v._2
        if ( futurePages.length > page ) {
          futurePages(page)
            .distinct
            .commit()
            .raw
            .map( json => {
              json("results")("bindings").arr.map(
                row => {
                  SparqlBuilder.createUri(row("espPropertyInternal")) }
              ).toSeq.distinct
            })
        } else {
          Future { Seq[URI]() }
        }
      })
  }

}
