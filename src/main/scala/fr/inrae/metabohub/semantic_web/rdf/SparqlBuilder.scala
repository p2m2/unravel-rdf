package fr.inrae.metabohub.semantic_web.rdf

import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel(name = "SparqlBuilder")
object SparqlBuilder {

  private def normalizedIri(value: String): String =
    value.replace("\\/", "/")

  def create(value: ujson.Value): SparqlDefinition = {
    value("type").str match {
      case "uri" =>
        createUri(value)

      case "literal" | "typed-literal" =>
        createLiteral(value)

      case "bnode" =>
        Anonymous(value("value").str)

      case termType =>
        throw new Error(s"Unknown SPARQL JSON term type: $termType")
    }
  }

  def createUri(value: ujson.Value): URI =
    URI(normalizedIri(value("value").str))

  def createLiteral(value: ujson.Value): Literal[String] = {
    val datatype =
      value.obj
        .get("datatype")
        .map(datatypeValue => URI(datatypeValue.str))
        .getOrElse(URI.empty)

    val languageTag =
      value.obj
        .get("xml:lang")
        .orElse(value.obj.get("lang"))
        .orElse(value.obj.get("tag"))
        .map(_.str)
        .getOrElse("")

    Literal(
      value = value("value").str,
      datatype = datatype,
      ta = languageTag
    )
  }
}

