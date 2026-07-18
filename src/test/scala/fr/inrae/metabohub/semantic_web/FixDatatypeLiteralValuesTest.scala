package fr.inrae.metabohub.semantic_web

import fr.inrae.metabohub.data.DataTestFactory
import fr.inrae.metabohub.semantic_web.configuration._
import fr.inrae.metabohub.semantic_web.rdf.URI
import utest.{TestSuite, Tests, test}

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

object FixDatatypeLiteralValuesTest extends TestSuite {

  val insertData: Future[Any] =
    DataTestFactory.insertVirtuoso1(
      """
        |<http://aa> <http://bb> <http://cc> .
        |<http://aa> <http://namespace/littvalue> 3 .
        |<http://aa> <http://namespace/littvalue> 1 .
        |""".stripMargin,
      this.getClass.getSimpleName
    )

  val config: UnravelConfig =
    DataTestFactory.getConfigVirtuoso1()

  override def utestAfterAll(): Unit = {
    DataTestFactory.deleteVirtuoso1(this.getClass.getSimpleName)
  }
  /*
   * - results.bindings remains a standard SPARQL Results JSON result.
   * - results.datatypes is an Unravel extension.
   * - datatypes(variable)(resourceIri) always returns an array of RDF terms,
   *   including for functional properties.
   * - A missing value should be represented either by an empty array or by an
   *   absent key. An empty array is recommended when it can be produced
   *   consistently, as it simplifies client-side processing.
   * - The second-level key must be the canonical RDF representation of the
   *   resource, rather than an arbitrary string.
   * - This mechanism should be restricted to URI subjects. If `entity` can be
   *   a blank node, its representation (for example, `_:b1`) must be defined
   *   and its scope must not be ambiguous.
   */
  def tests: Tests = Tests {
    test("datatype with entity resource and complete URI datatype property") {
      insertData.flatMap { _ =>
        UnravelSession(config)
          .something(
            "entity",
            _.set(URI("http://aa"))
              .datatype("http://namespace/littvalue", "bb")
          )
          .select(Seq("entity", "bb"))
          .commit()
          .raw
          .map { out =>
            val results = out.obj("results")
            val bindings = results.obj("bindings").arr

            assert(bindings.length == 1)

            val entity = bindings(0).obj("entity")
            assert(entity.obj("type").str == "uri")
            assert(entity.obj("value").str == "http://aa")

            val datatypeValues = results
              .obj("datatypes")
              .obj("bb")
              .obj("http://aa")
              .arr
              .flatMap(_.arr)

            val values = datatypeValues
              .map { value =>
                val literal = value.obj

                (
                  literal("type").str,
                  literal("value").str,
                  literal("datatype").str
                )
              }
              .toSet

            val expectedValues = Set(
              (
                "literal",
                "1",
                "http://www.w3.org/2001/XMLSchema#integer"
              ),
              (
                "literal",
                "3",
                "http://www.w3.org/2001/XMLSchema#integer"
              )
            )
            assert(
              values == expectedValues,
              s"Unexpected RDF values for bb: $values"
            )
          }
      }
    }

    test("datatype with entity resource and URI datatype with prefix property") {
      insertData.flatMap { _ =>
        UnravelSession(config)
          .prefix("ex","http://namespace/")
          .something(
            "entity",
            _.set(URI("http://aa"))
              .datatype("ex:littvalue", "bb")
          )
          .select(Seq("entity", "bb"))
          .commit()
          .raw
          .map { out =>
            val results = out.obj("results")
            val bindings = results.obj("bindings").arr

            assert(bindings.length == 1)

            val entity = bindings(0).obj("entity")
            assert(entity.obj("type").str == "uri")
            assert(entity.obj("value").str == "http://aa")

            val datatypeValues = results
              .obj("datatypes")
              .obj("bb")
              .obj("http://aa")
              .arr
              .flatMap(_.arr)

            val values = datatypeValues
              .map { value =>
                val literal = value.obj

                (
                  literal("type").str,
                  literal("value").str,
                  literal("datatype").str
                )
              }
              .toSet

            val expectedValues = Set(
              (
                "literal",
                "1",
                "http://www.w3.org/2001/XMLSchema#integer"
              ),
              (
                "literal",
                "3",
                "http://www.w3.org/2001/XMLSchema#integer"
              )
            )
            assert(
              values == expectedValues,
              s"Unexpected RDF values for bb: $values"
            )
          }
      }
    }
  }
}