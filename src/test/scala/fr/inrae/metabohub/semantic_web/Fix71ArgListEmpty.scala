// SPDX-FileCopyrightText: 2020-2026 INRAE
// SPDX-License-Identifier: GPL-3.0-or-later

package fr.inrae.metabohub.semantic_web

import fr.inrae.metabohub.data.DataTestFactory
import fr.inrae.metabohub.semantic_web.rdf.{IRI, URI}
import fr.inrae.metabohub.semantic_web.configuration._
import utest.{TestSuite, Tests, test}

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

object Fix71ArgListEmpty extends TestSuite {
  val insert_data = DataTestFactory.insertVirtuoso1(
    """
      <http://aa> <http://bb> 2 .
      <http://aa> <http://bb> 3 .
      <http://aa> <http://bb> 1 .
      <http://aa> <http://bb> 8 .
      <http://aa> <http://bb> 10 .
      """.stripMargin, this.getClass.getSimpleName)

  val config: UnravelConfig = DataTestFactory.getConfigVirtuoso1()

  def tests = Tests {
    test("Fix #73") {
      insert_data.map(_ => {
        UnravelSession(config)
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .something(
            _.out(URI("http://bb"), "v"))
          .select(Seq("*"))
          .commit()
          .raw.map(_ => {
            assert(true)
        })
      }).flatten
    }

    test("Fix #73 - 2") {
      insert_data.map(_ => {
        UnravelSession(config)
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .something(
            _.out(URI("http://bb"), "v"))
          .select()
          .commit()
          .raw.map(_ => {
          assert(true)
        })
      }).flatten
    }

    test("Fix #73 - 3") {
      insert_data.map(_ => {
        UnravelSession(config)
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .something(
           _.out(URI("http://bb"), "v"))
          .select(List())
          .commit()
          .raw.map(_ => {
          assert(false)
        }).recover(
          _ => assert(true)
        )
      }).flatten
    }

  }
}

