package fr.inrae.metabohub.semantic_web

import fr.inrae.metabohub.data.DataTestFactory
import fr.inrae.metabohub.semantic_web.configuration._
import fr.inrae.metabohub.semantic_web.rdf.URI
import utest.{TestSuite, Tests, test}

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

object FixDatatypeLiteralValuesTest extends TestSuite {
  private val recordIri =
    "http://example.org/record/C00002906"

  private val molecularEntityIri =
    "http://mb-wiki.nig.ac.jp/resource/C00002906/names"

  private val entityWithoutDatatypeIri =
    "http://aa-without-datatype"

  private val xsdString =
    "http://www.w3.org/2001/XMLSchema#string"

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
        |
        |<http://example.org/record/C00002906>
        |  <http://purl.org/dc/elements/1.1/identifier> "C00002906" ;
        |  <http://semanticscience.org/resource/SIO_000008>
        |    <http://mb-wiki.nig.ac.jp/resource/C00002906/names> .
        |
        |<http://mb-wiki.nig.ac.jp/resource/C00002906/names>
        |  a <http://semanticscience.org/resource/CHEMINF_000043> ;
        |  <http://semanticscience.org/resource/SIO_000300> "Test molecule" .
        |
        |<http://aa-without-datatype> <http://bb> <http://cc> .
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

    test(
      "datatype with nested resource, prefixed property, and IRI containing slashes"
    ) {
      insertData.flatMap { _ =>
        UnravelSession(config)
          .prefix("dc", "http://purl.org/dc/elements/1.1/")
          .prefix("sio", "http://semanticscience.org/resource/")
          .prefix("cheminf", "http://semanticscience.org/resource/")
          .something(
            "record",
            record =>
              record
                .set(URI(recordIri))
                .out("dc:identifier", "?knapsackId")
                .out(
                  "sio:SIO_000008",
                  "?molecularEntity",
                  molecularEntity =>
                    molecularEntity
                      .isA("cheminf:CHEMINF_000043")
                      .datatype(
                        "sio:SIO_000300",
                        "molecularEntityName"
                      )
                )
          )
          .select(
            Seq(
              "record",
              "knapsackId",
              "molecularEntity",
              "molecularEntityName"
            )
          )
          .limit(20)
          .commit()
          .raw
          .map { out =>
            val results = out.obj("results")
            val bindings = results.obj("bindings").arr

            assert(
              bindings.length == 1,
              s"Expected exactly one binding, found ${bindings.length}"
            )

            val binding = bindings(0).obj

            assert(
              binding("record")("value").str == recordIri,
              s"Unexpected record: ${binding("record")}"
            )

            assert(
              binding("knapsackId")("value").str == "C00002906",
              s"Unexpected identifier: ${binding("knapsackId")}"
            )

            assert(
              binding("molecularEntity")("value").str == molecularEntityIri,
              s"Unexpected molecular entity: ${binding("molecularEntity")}"
            )

            val names = results
              .obj("datatypes")
              .obj("molecularEntityName")
              .obj(molecularEntityIri)
              .arr

            assert(
              names.length == 1,
              s"Expected one molecular entity name, found: $names"
            )

            val name = names(0).obj

            assert(
              name("type").str == "literal",
              s"Expected a literal, found: ${name("type")}"
            )

            assert(
              name("value").str == "Test molecule",
              s"Unexpected molecular entity name: ${name("value")}"
            )

            assert(
              name("datatype").str == xsdString,
              s"Unexpected datatype: ${name("datatype")}"
            )
          }
      }

      test("datatype includes an empty array for a resource without datatype value") {
        insertData.flatMap { _ =>
          UnravelSession(config)
            .something(
              "entity",
              _.set(URI(entityWithoutDatatypeIri))
                .datatype("http://namespace/littvalue", "bb")
            )
            .select(Seq("entity", "bb"))
            .commit()
            .raw
            .map { out =>
              val results = out.obj("results")
              val bindings = results.obj("bindings").arr

              assert(
                bindings.length == 1,
                s"Expected exactly one main binding, found ${bindings.length}"
              )

              val entity = bindings(0).obj("entity")

              assert(entity("type").str == "uri")
              assert(entity("value").str == entityWithoutDatatypeIri)

              val valuesByUri = results
                .obj("datatypes")
                .obj("bb")
                .obj

              assert(
                valuesByUri.contains(entityWithoutDatatypeIri),
                s"Expected '$entityWithoutDatatypeIri' to exist in datatypes.bb"
              )

              val datatypeValues =
                valuesByUri(entityWithoutDatatypeIri).arr

              assert(
                datatypeValues.isEmpty,
                s"Expected no datatype values, found: $datatypeValues"
              )
            }
        }
      }
    }
  }
}