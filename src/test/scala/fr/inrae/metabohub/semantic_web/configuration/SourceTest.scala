// SPDX-FileCopyrightText: 2020-2026 INRAE
// SPDX-License-Identifier: GPL-3.0-or-later

package fr.inrae.metabohub.semantic_web.configuration

import utest.{TestSuite, Tests, test}

import scala.util.{Failure, Success, Try}

object SourceTest extends TestSuite {
  def tests: Tests = Tests {
    test("bad auth parameter") {
      assert(Try(Source(id="test",path="path",content="",mimetype="application/sparql-query",auth=Some("test"))).isFailure)
    }

    test("ok auth parameter") {
      assert(Try(Source(id="test",path="path",content="",mimetype="application/sparql-query",auth=Some("basic"))).isSuccess)
    }
  }
}
