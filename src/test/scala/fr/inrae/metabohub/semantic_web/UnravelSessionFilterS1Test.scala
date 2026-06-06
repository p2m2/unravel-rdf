package fr.inrae.metabohub.semantic_web

import fr.inrae.metabohub.data.DataTestFactory
import fr.inrae.metabohub.semantic_web.rdf.{IRI, SparqlBuilder, URI}
import fr.inrae.metabohub.semantic_web.configuration._
import utest._

import scala.concurrent.Future

object UnravelSessionFilterS1Test extends TestSuite {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  val insertData: Future[Any] =
    DataTestFactory.insertVirtuoso1(
      """
      <http://aaSWFilterTest> a <http://www.w3.org/2002/07/owl#Thing> .
      <http://aaSWFilterTest> <http://some> "test" .
      <http://aaSWFilterTest2> a <http://url_w3_class_stuff> .
      <http://aaSWFilterTest2> <http://some> "test" .

      <http://www.w3.org/2002/07/owl#Thing> a <http://www.w3.org/2002/07/owl#Class> .
      <http://url_w3_class_stuff> a <http://www.w3.org/2002/07/owl#Class> .
      """.stripMargin, this.getClass.getSimpleName)

  val config: UnravelConfig = DataTestFactory.getConfigVirtuoso1()

  def tests = Tests {
    test("SW Filter contains") {
      insertData.map(_ => {
        val trans = UnravelSession(config)
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .something("instance",_.isSubjectOf(URI("a"),_.set(URI("Class", "owl"))))
          .from("instance",_.filter.contains("w3"))
          .from("instance",_.filter.not.contains("http://www.w3.org/2002/07/owl"))
          .select(List("instance"))


        trans.commit()
          .raw
          .map(result => {
            assert(result("results")("bindings").arr.nonEmpty)
            assert(SparqlBuilder.createUri(result("results")("bindings")(0)("instance")).localName.contains("w3"))
            assert(!SparqlBuilder.createUri(result("results")("bindings")(0)("instance")).localName.contains("http://www.w3.org/2002/07/owl"))
          })
      }).flatten
    }
  }
}
