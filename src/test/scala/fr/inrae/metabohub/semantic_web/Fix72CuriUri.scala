package fr.inrae.metabohub.semantic_web

import fr.inrae.metabohub.data.DataTestFactory
import fr.inrae.metabohub.semantic_web.rdf.{IRI, URI}
import fr.inrae.metabohub.semantic_web.configuration._
import utest.{TestSuite, Tests, test}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Fix72CuriUri extends TestSuite {
  val insert_data: Future[Any] = DataTestFactory.insertVirtuoso1(
    """
      <http://aa> <http://bb> <http://cc> .
      <http://aa> <http://test#dd> "test" .
      """.stripMargin, this.getClass.getSimpleName)

  val config: UnravelConfig = DataTestFactory.getConfigVirtuoso1()

  override def utestAfterAll(): Unit = {
    DataTestFactory.deleteVirtuoso1(this.getClass.getSimpleName)
  }

  def tests = Tests {
    test("Fix #72") {
      insert_data.map(_ => {
        UnravelSession(config)
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .prefix("test","http://test#")
          .something("h1", h1 => h1.datatype(URI("dd","test"),"dt"))
         .select(Seq("h1","dt"))
          .commit()
          .raw.map(r => {
            assert(r("results")("bindings").arr.length == 6)
            assert(r("results")("datatypes").obj.nonEmpty)
        })
      }).flatten
    }
  }
}

