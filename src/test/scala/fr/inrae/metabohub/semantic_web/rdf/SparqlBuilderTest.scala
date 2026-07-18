// SPDX-FileCopyrightText: 2020-2026 INRAE
// SPDX-License-Identifier: GPL-3.0-or-later

package fr.inrae.metabohub.semantic_web.rdf

import utest.{TestSuite, Tests, test}

object SparqlBuilderTest extends TestSuite {

  private val xsdString =
    "http://www.w3.org/2001/XMLSchema#string"

  private val xsdInteger =
    "http://www.w3.org/2001/XMLSchema#integer"

  def tests: Tests = Tests {

    test("create URI from a SPARQL JSON result term") {
      val term = ujson.Obj(
        "type" -> "uri",
        "value" -> "http://example.org/resource/1"
      )

      val result = SparqlBuilder.create(term)

      assert(result.isInstanceOf[URI])
      assert(result.sparql == "<http://example.org/resource/1>")
      assert(result.naiveLabel == "1")
    }

    test("create URI with an escaped slash from a SPARQL JSON result term") {
      val term = ujson.read(
        """{
          |  "type": "uri",
          |  "value": "http://mb-wiki.nig.ac.jp/resource/C00002906\\/names"
          |}""".stripMargin
      )

      val result = SparqlBuilder.create(term)

      assert(result.isInstanceOf[URI])
      assert(
        result.sparql ==
          "<http://mb-wiki.nig.ac.jp/resource/C00002906/names>"
      )
      assert(result.naiveLabel == "names")
    }

    test("create URI with a slash from a SPARQL JSON result term") {
      val term = ujson.Obj(
        "type" -> "uri",
        "value" -> "http://mb-wiki.nig.ac.jp/resource/C00002906/names"
      )

      val result = SparqlBuilder.create(term)

      assert(
        result.sparql ==
          "<http://mb-wiki.nig.ac.jp/resource/C00002906/names>"
      )
    }

    test("create plain literal from a SPARQL JSON result term") {
      val term = ujson.Obj(
        "type" -> "literal",
        "value" -> "Example label"
      )

      val result = SparqlBuilder.create(term)

      assert(result.isInstanceOf[Literal[_]])
      assert(result.sparql == "\"Example label\"")
      assert(result.naiveLabel == "Example label")
    }

    test("create typed literal from a SPARQL JSON result term") {
      val term = ujson.Obj(
        "type" -> "literal",
        "value" -> "42",
        "datatype" -> xsdInteger
      )

      val result = SparqlBuilder.create(term)

      assert(result.isInstanceOf[Literal[_]])
      assert(
        result.sparql ==
          s"\"42\"^^<$xsdInteger>"
      )
      assert(result.naiveLabel == "42")
    }

    test("create xsd:string literal from a SPARQL JSON result term") {
      val term = ujson.Obj(
        "type" -> "literal",
        "value" -> "Book",
        "datatype" -> xsdString
      )

      val result = SparqlBuilder.create(term)

      assert(result.isInstanceOf[Literal[_]])
      assert(
        result.sparql ==
          s"\"Book\"^^<$xsdString>"
      )
    }

    test("create language-tagged literal using xml:lang") {
      val term = ujson.Obj(
        "type" -> "literal",
        "value" -> "Example resource",
        "xml:lang" -> "en"
      )

      val result = SparqlBuilder.create(term)

      assert(result.isInstanceOf[Literal[_]])
      assert(result.sparql == "\"Example resource\"@en")
    }

    test("create language-tagged literal using lang") {
      val term = ujson.Obj(
        "type" -> "literal",
        "value" -> "Ressource d'exemple",
        "lang" -> "fr"
      )

      val result = SparqlBuilder.create(term)

      assert(result.isInstanceOf[Literal[_]])
      assert(result.sparql == "\"Ressource d'exemple\"@fr")
    }

    test("create blank node from a SPARQL JSON result term") {
      val term = ujson.Obj(
        "type" -> "bnode",
        "value" -> "b1"
      )

      val result = SparqlBuilder.create(term)

      assert(result.isInstanceOf[Anonymous])
      assert(result.sparql == "b1")
      assert(result.naiveLabel == "Anonymous[b1]")
    }

    test("reject a SPARQL JSON term with an unknown type") {
      val term = ujson.Obj(
        "type" -> "unsupported",
        "value" -> "value"
      )

      val exception =
        try {
          SparqlBuilder.create(term)
          None
        } catch {
          case error: Error => Some(error)
        }

      assert(exception.nonEmpty)
      assert(
        exception.get.getMessage ==
          "Unknown SPARQL JSON term type: unsupported"
      )
    }

    test("convert a prefixed name string to URI") {
      val result: SparqlDefinition = "rdfs:label"

      assert(result.isInstanceOf[URI])
      assert(result.sparql == "rdfs:label")
      assert(result.naiveLabel == "label")
    }

    test("convert an absolute IRI string to URI") {
      val result: SparqlDefinition =
        "http://example.org/resource/1"

      assert(result.isInstanceOf[URI])
      assert(result.sparql == "<http://example.org/resource/1>")
    }

    test("convert a variable string to Var") {
      val result: SparqlDefinition = "?resource"

      assert(result.isInstanceOf[Var])
      assert(result.sparql == "?resource")
      assert(result.naiveLabel == "Variable[resource]")
    }

    test("convert an integer to a literal") {
      val result: SparqlDefinition = 42

      assert(result.isInstanceOf[Literal[_]])
      assert(result.sparql == "42")
      assert(result.naiveLabel == "42")
    }
  }
}