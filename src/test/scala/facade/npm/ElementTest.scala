// SPDX-FileCopyrightText: 2020-2026 INRAE
// SPDX-License-Identifier: GPL-3.0-or-later

package facade.npm

import utest._

object ElementTest extends TestSuite {
  private val DF = N3.DataFactory

  val tests: Tests = Tests {

    test("NamedNode") {
      val v1: NamedNode = DF.namedNode("test")
      val v2: NamedNode = DF.namedNode("test")

      assert(v1.equalsTerm(v2))
      assert(!DF.namedNode("test").equalsTerm(DF.namedNode("test2")))
    }

    test("BlankNode") {
      assert(DF.blankNode("test").equalsTerm(DF.blankNode("test")))
      assert(!DF.blankNode("test").equalsTerm(DF.blankNode("test2")))
    }

    test("Literal") {
      assert(DF.literal("test", "en").equalsTerm(DF.literal("test", "en")))
      assert(!DF.literal("test", "en").equalsTerm(DF.literal("test2", "en")))
      assert(!DF.literal("test", "en").equalsTerm(DF.literal("test", "fr")))
      assert(
        !DF.literal("test", DF.namedNode("test"))
          .equalsTerm(DF.literal("test", DF.namedNode("test2")))
      )
    }

    test("Variable") {
      assert(DF.variable("test").equalsTerm(DF.variable("test")))
      assert(!DF.variable("test").equalsTerm(DF.variable("test2")))
    }

    test("DefaultGraph") {
      assert(DF.defaultGraph().equalsTerm(DF.defaultGraph()))
    }
  }
}