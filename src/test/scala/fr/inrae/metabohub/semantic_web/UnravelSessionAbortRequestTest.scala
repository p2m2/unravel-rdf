// SPDX-FileCopyrightText: 2020-2026 INRAE
// SPDX-License-Identifier: GPL-3.0-or-later

package fr.inrae.metabohub.semantic_web

import fr.inrae.metabohub.data.DataTestFactory
import fr.inrae.metabohub.semantic_web.rdf._
import fr.inrae.metabohub.semantic_web.configuration._
import utest._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.language.postfixOps

object UnravelSessionAbortRequestTest extends TestSuite {

  val insertData = DataTestFactory.insertVirtuoso1(
    """
      <aaSWAbortRequestTest> <bb> <cc> .
      <aa> <datatype> "testdatatype" .
      """.stripMargin, this.getClass.getSimpleName)

  val config: UnravelConfig = DataTestFactory.getConfigVirtuoso1()


  def tests = Tests {
    test("Abort Request steps") {
      insertData.map(_ => {
      val swr =
        UnravelSession(config).something("h1",_.out(Var("h2"),"h3"))
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .select(List("h1","h2","h3"))

      swr.abort()

      assert(swr.currentRequestEvent == "ABORTED_BY_THE_USER")

      swr.raw
        .map( v => assert(false))
        .recover( _ => assert(true))
      }).flatten
    }
  }
}
