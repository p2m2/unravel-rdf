// SPDX-FileCopyrightText: 2020-2026 INRAE
// SPDX-License-Identifier: GPL-3.0-or-later

package fr.inrae.metabohub.semantic_web

import fr.inrae.metabohub.semantic_web.UnravelSessionTest.config
import utest.{TestSuite, Tests, test}

import scala.util.Try

object UnravelQueryTest extends TestSuite {
  def tests: Tests = Tests {
    test("console") {
      assert(Try(UnravelQuery(UnravelSession(config).something("h1")).console).isSuccess)
    }

    test("commit - failure because no selection") {
      assert(Try(UnravelQuery(UnravelSession(config).something("h1")).commit()).isFailure)
    }

    test("commit - failure because no selection") {
      assert(Try(UnravelSession(config).something("h1").select(List("h1")).commit()).isSuccess)
    }

    test("commit - empty projection") {
      assert(Try(UnravelSession(config).something("h1").select(List()).commit().projection).isSuccess)
    }
  }

}
