package facade.npm

import utest._
import scala.scalajs.js

object N3ParserTest extends TestSuite {
  private val DF = N3.DataFactory

  val tests = Tests {

    test("Creating triples/quads") {
      val myQuad = DF.quad(
        DF.namedNode("https://ruben.verborgh.org/profile/#me"),
        DF.namedNode("http://xmlns.com/foaf/0.1/givenName"),
        DF.literal("Ruben", "en"),
        DF.defaultGraph()
      )

      println(myQuad.subject.value)
      println(myQuad.`object`.value)
      println(myQuad.`object`.asInstanceOf[Literal].datatype.value)
      println(myQuad.`object`.asInstanceOf[Literal].language)

      assert(myQuad.subject.value == "https://ruben.verborgh.org/profile/#me")
      assert(myQuad.`object`.value == "Ruben")
      assert(
        myQuad.`object`.asInstanceOf[Literal].datatype.value ==
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#langString"
      )
      assert(myQuad.`object`.asInstanceOf[Literal].language == "en")
    }

    test("Parsing - N-Triples minimal") {
      val parser = new N3Parser(
        N3Options(
          baseIRI = "http://example.org/",
          format = N3FormatOption.`N-Triples`
        )
      )

      val quads = parser.parse(
        """
          _:a <http://ex.org/b> "c" .
        """.stripMargin
      )

      println(s"Parsed quads count = ${quads.length}")
      assert(quads.nonEmpty)
    }
  }
}