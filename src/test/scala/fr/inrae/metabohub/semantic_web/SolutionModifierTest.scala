package fr.inrae.metabohub.semantic_web
import fr.inrae.metabohub.data.DataTestFactory
import fr.inrae.metabohub.semantic_web.rdf.{IRI, Literal, Var, URI}
import fr.inrae.metabohub.semantic_web.configuration._
import utest.{TestSuite, Tests, test}

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

object SolutionModifierTest extends TestSuite {
  val insertData = DataTestFactory.insertVirtuoso1(
    """
      <http://p1>    <http://xmlns.com/foaf/0.1/name> "Alice" .
      <http://p1>    <http://xmlns.com/foaf/0.1/mbox>  <mailto:alice@example.com> .

      <http://p2>    <http://xmlns.com/foaf/0.1/name>  "Alice" .
      <http://p2>    <http://xmlns.com/foaf/0.1/mbox>   <mailto:asmith@example.com> .

      <http://p3>    <http://xmlns.com/foaf/0.1/name> "Alice" .
      <http://p3>    <http://xmlns.com/foaf/0.1/mbox>   <mailto:alice.smith@example.com> .
      """.stripMargin, this.getClass.getSimpleName)

  val config: UnravelConfig = DataTestFactory.getConfigVirtuoso1()

  val basereq : UnravelQuery = UnravelSession(config)
    .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
    .prefix("foaf","http://xmlns.com/foaf/0.1/")
    .something(_.out(URI("name","foaf"), "?name"))
    .select(Seq("name"))

  val basereq2 : UnravelQuery = UnravelSession(config)
    .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
    .prefix("foaf","http://xmlns.com/foaf/0.1/")
    .something(_.out("?var", Literal("Alice")))
    .select(Seq("var"))

  val basereq3 : UnravelQuery = UnravelSession(config)
    .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
    .prefix("foaf","http://xmlns.com/foaf/0.1/")
    .something(_.in("?var", "<http://p1>"))
    .select(Seq("var"))

  def tests = Tests {
    test("no modifier") {
      insertData.map(_ => {
        basereq.commit()
          .raw.map(r => {
          assert(r("results")("bindings").arr.length == 3)
        })
      }).flatten
    }

    test("limit") {
      insertData.map(_ => {
        basereq
          .limit(1)
          .commit()
          .raw.map(r => {
          assert(r("results")("bindings").arr.length == 1)
        })
      }).flatten
    }

    test("offset") {
      insertData.map(_ => {
        basereq
          .limit(2)
          .offset(1)
          .commit()
          .raw.map(_ => {
            assert(true)
        })
      }).flatten
    }

    test("distinct") {
      insertData.map(_ => {
        basereq
          .distinct
          .commit()
          .raw.map(r => {
          assert(r("results")("bindings").arr.length == 1)
        })
      }).flatten
    }

    test("reduced") {
      insertData.map(_ => {
        basereq
          .reduced
          .commit()
          .raw.map(r => {
          assert(r("results")("bindings").arr.length <= 3)
        })
      }).flatten
    }

    test("no modifier queryvar as property subjectof") {
      insertData.map(_ => {
        basereq2.commit()
          .raw.map(r => {
            assert(r("results")("bindings").arr.length == 3)
          })
      }).flatten
    }
    test("no modifier queryvar as property - objectof") {
      insertData.map(_ => {
        basereq3.commit()
          .raw.map(r => {
            println(r("results")("bindings").arr.length)
            assert(r("results")("bindings").arr.length == 2)
          })
      }).flatten
    }
  }
}
