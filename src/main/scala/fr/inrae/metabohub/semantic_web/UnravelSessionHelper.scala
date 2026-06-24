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
      case uri : URI if uri == URI("")  => sw.out(URI("a"),"?_esp___type")
      case _ : URI =>  sw.out(URI("a"),"?_esp___type",
          _.out(URI("a"),motherClass))
    }).from("_esp___type")
      .filter.not.regex(regex_avoid_prefix)

    (if ( regex.trim != "")
          query.from("_esp___type").filter.regex(regex)
      else
        query)
      .selectByPage(List("_esp___type"))
      .flatMap(  v => {
        val futurePages : Seq[UnravelQuery] = v._2

        if ( futurePages.length > page ) {
          futurePages(page)
            .commit()
            .raw
            .map( json => {
              json("results")("bindings").arr.map(
                row => SparqlBuilder.createUri(row("_esp___type"))
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
        .something("_esp___type",
          _.out(Var("_esp___property"),Var("_esp___type"),
            _.out(URI("a"),motherClassProperties)))
    } else {
      sw.root
        .something("_esp___type",_.out(Var("_esp___property"),Var("_esp___type")))
    }

    /* object or datatype properties owl def. */
    val query = ( kind  match {
      case "objectProperty" => state.from("_esp___type",_.filter.isUri)
      case "datatypeProperty" => state.from("_esp___type",_.filter.isLiteral)
      case _ => state
    }).from("_esp___property",_.filter.not.regex(regex_avoid_prefix))

    (if ( regex.trim.nonEmpty)
      query.from("_esp___property",_.filter.regex(regex))
    else
      query)
      .selectByPage(List("_esp___property"))
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
                  SparqlBuilder.createUri(row("_esp___property")) }
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
        .something("_esp___type",
          _.in(Var("_esp___property"),Var(sw.focusNode))
           .out(URI("a"), motherClassProperties)
          )

    } else {
      sw.in(Var("_esp___property"))
    })//.something("_esp___property",_.filter.not.regex(regex_avoid_prefix))

    (if ( regex.trim != "")
      query.something("_esp___property",_.filter.regex(regex))
    else
      query)

      .selectByPage(List("_esp___property"))
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
                  SparqlBuilder.createUri(row("_esp___property")) }
              ).toSeq.distinct
            })
        } else {
          Future { Seq[URI]() }
        }
      })
  }

}
