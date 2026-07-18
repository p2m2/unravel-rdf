package fr.inrae.metabohub.semantic_web

import fr.inrae.metabohub.data.DataTestFactory
import fr.inrae.metabohub.semantic_web.configuration._
import fr.inrae.metabohub.semantic_web.rdf.URI
import utest.{TestSuite, Tests, test}

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

object FixDatatypeLiteralValuesTest extends TestSuite {

  private val xsdInteger =
    "http://www.w3.org/2001/XMLSchema#integer"

  private val entityIri = "http://aa"

  private val expectedValues = Set(
    ("literal", "1", xsdInteger),
    ("literal", "3", xsdInteger)
  )

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

  private def assertDatatypeValues(
                                    out: ujson.Value,
                                    propertyRef: String,
                                    resourceIri: String
                                  ): Unit = {
    val results = out.obj("results")
    val bindings = results.obj("bindings").arr

    assert(
      bindings.length == 1,
      s"Expected exactly one main binding, found ${bindings.length}"
    )

    val entity = bindings(0).obj("entity")

    assert(
      entity("type").str == "uri",
      s"Expected entity to be a URI, found: ${entity("type")}"
    )

    assert(
      entity("value").str == resourceIri,
      s"Expected entity IRI '$resourceIri', found: ${entity("value")}"
    )

    val datatypeValues = results
      .obj("datatypes")
      .obj(propertyRef)
      .obj(resourceIri)
      .arr

    val values = datatypeValues
      .map { term =>
        val literal = term.obj

        (
          literal("type").str,
          literal("value").str,
          literal("datatype").str
        )
      }
      .toSet

    assert(
      values == expectedValues,
      s"""|Unexpected RDF values for '$propertyRef'
          |Expected: $expectedValues
          |Actual:   $values
          |""".stripMargin
    )
  }

  def tests: Tests = Tests {

    test("datatype with entity resource and complete URI datatype property") {
      insertData.flatMap { _ =>
        UnravelSession(config)
          .something(
            "entity",
            _.set(URI(entityIri))
              .datatype("http://namespace/littvalue", "bb")
          )
          .select(Seq("entity", "bb"))
          .commit()
          .raw
          .map { out =>
            assertDatatypeValues(
              out = out,
              propertyRef = "bb",
              resourceIri = entityIri
            )
          }
      }
    }

    test("datatype with entity resource and prefixed URI datatype property") {
      insertData.flatMap { _ =>
        UnravelSession(config)
          .prefix("ex", "http://namespace/")
          .something(
            "entity",
            _.set(URI(entityIri))
              .datatype("ex:littvalue", "bb")
          )
          .select(Seq("entity", "bb"))
          .commit()
          .raw
          .map { out =>
            assertDatatypeValues(
              out = out,
              propertyRef = "bb",
              resourceIri = entityIri
            )
          }
      }
    }
  }
}