// SPDX-FileCopyrightText: 2020-2026 INRAE
// SPDX-License-Identifier: GPL-3.0-or-later

package facade.npm

import utest._

import scala.scalajs.js
import scala.scalajs.js.JSConverters._

object N3WriterTest extends TestSuite {
  private val DF = N3.DataFactory

  val tests = Tests {

    test("From quads to a string") {
      val writer = new N3Writer(
        N3Options(
          prefixes = Map(
            "c" -> "http://example.org/cartoons#"
          )
        )
      )

      writer.addQuad(
        DF.namedNode("http://example.org/cartoons#Tom"),
        DF.namedNode("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
        DF.namedNode("http://example.org/cartoons#Cat")
      )

      writer.addQuad(
        DF.quad(
          DF.namedNode("http://example.org/cartoons#Tom"),
          DF.namedNode("http://example.org/cartoons#name"),
          DF.literal("Tom")
        )
      )

      writer.end((error, result) => println(result))
    }

    test("Blank nodes and lists") {
      val writer = new N3Writer(
        N3Options(
          prefixes = Map(
            "c"    -> "http://example.org/cartoons#",
            "foaf" -> "http://xmlns.com/foaf/0.1/"
          )
        )
      )

      writer.addQuad(
        writer.blank(
          DF.namedNode("http://xmlns.com/foaf/0.1/givenName"),
          DF.literal("Tom", "en")
        ),
        DF.namedNode("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
        DF.namedNode("http://example.org/cartoons#Cat")
      )

      val props = Seq(
        PredicateObject(
          DF.namedNode("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
          DF.namedNode("http://example.org/cartoons#Cat")
        ),
        PredicateObject(
          DF.namedNode("http://xmlns.com/foaf/0.1/givenName"),
          DF.literal("Tom", "en")
        )
      ).toJSArray

      writer.addQuad(
        DF.quad(
          DF.namedNode("http://example.org/cartoons#Jerry"),
          DF.namedNode("http://xmlns.com/foaf/0.1/knows"),
          writer.blank(props)
        )
      )

      writer.addQuad(
        DF.namedNode("http://example.org/cartoons#Mammy"),
        DF.namedNode("http://example.org/cartoons#hasPets"),
        writer.list(
          Seq(
            DF.namedNode("http://example.org/cartoons#Tom"),
            DF.namedNode("http://example.org/cartoons#Jerry")
          ).toJSArray
        )
      )

      writer.end((error, result) => println(result))
    }
  }
}