package fr.inrae.metabohub.semantic_web

import fr.inrae.metabohub.data.DataTestFactory
import fr.inrae.metabohub.semantic_web.configuration._
import fr.inrae.metabohub.semantic_web.rdf._
import utest._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object UnravelSessionTraverseTest  extends TestSuite {

  val insertData: Future[Any] = DataTestFactory.insertVirtuoso1(
    """
      <some_subject> <http://bb_1> <http://aa> .
      <http://aa> <http://bb_2> <some_object2> .
      <http://aa> <http://bb_2> <http://cc2> .
      """.stripMargin, this.getClass.getSimpleName)

  val config: UnravelConfig = DataTestFactory.getConfigVirtuoso1()

  override def utestAfterAll(): Unit = {
    DataTestFactory.deleteVirtuoso1(this.getClass.getSimpleName)
  }


  def tests: Tests = Tests {
   /* test("h1 detection") {
      insertData.map(_ => {
        UnravelSession(config)
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .something("h1",
            _.set(URI("http://aa")).traverse("?property","?var"))
          .select(List("h1"))
          .commit()
          .raw
          .map(r => assert(r("results")("bindings").arr.length==3))
      }).flatten
    }*/
    test("var detection") {
      insertData.map(_ => {
        UnravelSession(config)
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .something("h1",
            _.set(URI("http://aa")).traverse("?property","?var"))
          .select(List("var"))
          .commit()
          .raw
          .map(r => assert(r("results")("bindings").arr.length==3))
      }).flatten
    }
  }
}
