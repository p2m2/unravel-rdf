package fr.inrae.metabohub.semantic_web

import fr.inrae.metabohub.data.DataTestFactory
import fr.inrae.metabohub.semantic_web.rdf._
import fr.inrae.metabohub.semantic_web.configuration._
import utest._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.postfixOps

object UnravelSessionFilterTest extends TestSuite {


  val insertData: Future[Any] = DataTestFactory.insertVirtuoso1(
    """
      <http://aaSWFilterTest> <http://propUri> <http://cc> .
      <http://aaSWFilterTest> <http://propLiteral> "test" .
      <http://aaSWFilterTest> <http://propBlank> _:something .

      <http://aaSWFilterTest> <http://propContains> "something regex_expected somethingElse" .
      <http://aaSWFilterTest> <http://propNotContains> "something other test ... somethingElse" .

      <http://aaSWFilterTest> <http://propNum> 1 .
      <http://aaSWFilterTest> <http://propNum> 1.2 .
      <http://aaSWFilterTest> <http://propNum> "2"^^xsd:integer .
      <http://aaSWFilterTest> <http://propNum> "2.3"^^xsd:double .

      <http://aaSWFilterTest> <http://propNum> "5"^^xsd:integer .
      <http://aaSWFilterTest> <http://propNum> 5.1 .
      <http://aaSWFilterTest> <http://propNum> "6"^^xsd:integer .
      <http://aaSWFilterTest> <http://propNum> "5.2"^^xsd:double .

      <http://aaSWFilterTest> <http://propNum> 10 .
      <http://aaSWFilterTest> <http://propNum> 10.2 .
      <http://aaSWFilterTest> <http://propNum> "11"^^xsd:integer .
      <http://aaSWFilterTest> <http://propNum> "11.4"^^xsd:double .

      <http://aaSWFilterTest> <http://propDate> "1790-01-01"^^xsd:date .
      <http://aaSWFilterTest> <http://propDate> "1990-01-01"^^xsd:date .

      """.stripMargin, this.getClass.getSimpleName)

  val config: UnravelConfig = DataTestFactory.getConfigVirtuoso1()

  def tests: Tests = Tests {

    test("SW Filter isLiteral") {
      insertData.map(_ => {
        UnravelSession(config)
          .something(
            _.isSubjectOf(QueryVariable("prop"),_.filter.isLiteral))
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .transaction
          .distinct
          .projection(List("prop"))
          .commit()
          .raw
          .map(result => {
            assert(result("results")("bindings").arr.length == 5)
          })
      }).flatten
    }

    test("SW Filter isUri") {
      insertData.map(_ => {
        UnravelSession(config)
          .something(
            _.isSubjectOf(QueryVariable("prop"),_.filter.isUri))
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .select(List("prop"))
          .commit()
          .raw
          .map(result => {
            assert(result("results")("bindings").arr.length == 1)
            assert(SparqlBuilder.createUri(result("results")("bindings")(0)("prop")).localName == "http://propUri")
          })
      }).flatten
    }

    test("SW Filter isBlank") {
      insertData.map(_ => {
        UnravelSession(config)
          .something(
            _.isSubjectOf(QueryVariable("prop"),_.filter.isBlank))
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .transaction
          .distinct
          .projection(List("prop"))
          .commit()
          .raw
          .map(result => {
            assert(result("results")("bindings").arr.length == 1)
            assert(SparqlBuilder.createUri(result("results")("bindings")(0)("prop")).localName == "http://propBlank")
          })
      }).flatten
    }

    test("SW Filter contains") {
      insertData.map(_ => {
        UnravelSession(config)
          .something("x",
            _.isSubjectOf(URI("http://propContains"),_.filter.contains("regex_expected")))
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .select(List("x"))
          .commit()
          .raw
          .map(result => {
            assert(result("results")("bindings").arr.length == 1)
          })
      }).flatten
    }

    test("SW Filter not contains") {
      insertData.map(_ => {
        UnravelSession(config)
          .something(
            _.isSubjectOf(URI("http://propContains"),_.filter.not.contains("regex_expected")))
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .select(List("prop"))
          .commit()
          .raw
          .map(result => {
            assert(false)
          })
          .recover(e => assert(true))
      }).flatten
    }
    test("SW Filter not contains 2") {
      insertData.map(_ => {
        UnravelSession(config)
          .something(
            _.isSubjectOf(URI("propContains"),
              _.filter.contains("bidon")
              .filter.not.contains("regex_expected")))
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .select(List("prop"))
          .commit()
          .raw
          .map(result => {
            assert(false)
          })
          .recover(e => assert(true))
      }).flatten
    }

    test("SW Filter strStarts") {
      insertData.map(_ => {
        UnravelSession(config)
          .something(
            _.isSubjectOf(QueryVariable("prop"),_.filter.strStarts("tes")))
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .select(List("prop"))
          .commit()
          .raw
          .map(result => {
            assert(result("results")("bindings").arr.length == 1)
            assert(SparqlBuilder.createUri(result("results")("bindings")(0)("prop")).localName == "http://propLiteral")
          })
      }).flatten
    }

    test("SW Filter strEnds") {
      insertData.map(_ => {
        UnravelSession(config)
          .something(
            _.isSubjectOf(QueryVariable("prop"),_.filter.strEnds("est")))
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .select(List("prop"))
          .commit()
          .raw
          .map(result => {
            assert(result("results")("bindings").arr.length == 1)
            assert(SparqlBuilder.createUri(result("results")("bindings")(0)("prop")).localName == "http://propLiteral")
          })
      }).flatten
    }

    test("SW Filter equal") {
      insertData.map(_ => {
        UnravelSession(config)
          .something(
            _.isSubjectOf(QueryVariable("prop"), "value",_.filter.equal("test")))
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .select(List("prop"))
          .commit()
          .raw
          .map(result => {
            assert(result("results")("bindings").arr.length == 1)
            assert(SparqlBuilder.createUri(result("results")("bindings")(0)("prop")).localName == "http://propLiteral")
          })
      }).flatten
    }

    test("SW Filter notEqual") {
      insertData.map(_ => {
        UnravelSession(config)
          .something(
            _.isSubjectOf(QueryVariable("prop"), "v",_.filter.notEqual("test")))
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .select(List("v"))
          .commit()
          .raw
          .map(result => {
            assert(result("results")("bindings").arr.map(v => v("v")("value").value).filter(_ == "test").length == 0)
          })
      }).flatten
    }

    test("SW Filter inf") {
      insertData.map(_ => {
        UnravelSession(config)
          .something(
            _.isSubjectOf(URI("http://propNum"), "value",_.filter.inf(5))
          )
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .select(List("value"))
          .commit()
          .raw
          .map(result => {
            assert(result("results")("bindings").arr.length == 4)
          })
      }).flatten
    }

    test("SW Filter inf 2") {
      insertData.map(_ => {
        UnravelSession(config)
          .something(
            _.isSubjectOf(URI("http://propDate"), "value",
              _.filter.inf(Literal("1900-01-01", URI("date", "xsd"))))
             )
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .select(List("value"))
          .commit()
          .raw
          .map(result => {
            assert(result("results")("bindings").arr.length == 1)
          })
      }).flatten
    }

    test("SW Filter infEqual") {
      insertData.map(_ => {
        UnravelSession(config)
          .something(_.isSubjectOf(URI("http://propNum"), "value",_.filter.infEqual(5)))
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .select(List("value"))
          .commit()
          .raw
          .map(result => {
            assert(result("results")("bindings").arr.length == 5)
          })
      }).flatten
    }

    test("SW Filter Sup") {
      insertData.map(_ => {
        UnravelSession(config)
          .something(
            _.isSubjectOf(URI("http://propNum"), "value",_.filter.sup(5)))
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .select(List("value"))
          .commit()
          .raw
          .map(result => {
            assert(result("results")("bindings").arr.length == 7)
          })
      }).flatten
    }

    test("SW Filter SupEqual") {
      insertData.map(_ => {
        UnravelSession(config)
          .something(
            _.isSubjectOf(URI("http://propNum"), "value",_.filter.supEqual(5)))
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .select(List("value"))
          .commit()
          .raw
          .map(result => {
            assert(result("results")("bindings").arr.length == 8)
          })
      }).flatten
    }
  }

}
