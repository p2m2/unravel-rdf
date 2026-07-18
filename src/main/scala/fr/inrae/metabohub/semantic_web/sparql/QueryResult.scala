// SPDX-FileCopyrightText: 2020-2026 INRAE
// SPDX-License-Identifier: GPL-3.0-or-later

package fr.inrae.metabohub.semantic_web.sparql

import fr.inrae.metabohub.semantic_web.rdf.{SparqlBuilder, SparqlDefinition}

import scala.util.{Failure, Success, Try}

case class QueryResult(results: String, mimetype : String = "json") {

  lazy val json =
    Try(ujson.read(results)) match {
      case Success(json) =>
        json.objOpt match {
          case Some(_) => json
          // fix when get only results without header (triplydb)
          case None => ujson.Obj(
            "head" -> ujson.Obj(
              "link" -> ujson.Arr(),
              "vars" -> ujson.Arr()
            ),
            "results" -> ujson.Obj(
              "bindings" -> json
            )
          )
        }
      case Failure(_) => ujson.Obj(
        "head" -> ujson.Obj(
          "link" -> ujson.Arr(),
          "vars" -> ujson.Arr()
        ),
        "results" -> ujson.Obj(
          "bindings" -> ujson.Arr()
        )
      )
    }


  /* get column results */
  def getValues( key : String ): Seq[SparqlDefinition] = {
    json("results")("bindings").arr.flatMap {
      case o: ujson.Obj => Some(SparqlBuilder.create(o(key)))
      case _ => None
    }.toSeq
  }

  def setDatatype(
                   key: String,
                   uriValues: Map[String, ujson.Arr]
                 ): Unit = {
    val datatypes =
      json("results").obj.getOrElse("datatypes", ujson.Obj())

    val valuesByUri =
      datatypes.obj.getOrElse(key, ujson.Obj())

    uriValues.foreach {
      case (resourceIri, values) =>
        val existingValues =
          valuesByUri.obj
            .getOrElse(resourceIri, ujson.Arr())
            .arr

        existingValues.appendAll(values.arr)

        valuesByUri.obj.update(
          resourceIri,
          ujson.Arr.from(existingValues)
        )
    }

    datatypes.obj.update(key, valuesByUri)
    json("results").obj.update("datatypes", datatypes)
  }
}
