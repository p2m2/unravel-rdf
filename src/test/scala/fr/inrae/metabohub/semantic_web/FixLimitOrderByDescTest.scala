package fr.inrae.metabohub.semantic_web

import fr.inrae.metabohub.data.NodeEnv
import fr.inrae.metabohub.semantic_web.rdf.URI
import fr.inrae.metabohub.semantic_web.configuration._
import utest.{TestSuite, Tests, test}

import scala.concurrent.ExecutionContext.Implicits.global

object FixLimitOrderByDescTest extends TestSuite {
  val turtleBase: String = NodeEnv.get("TURTLE_BASE_URL", "http://localhost:8080")
  val config: SWDiscoveryConfiguration = SWDiscoveryConfiguration.setConfigString(
    s"""
      {
       "sources" : [{
         "id"       : "file_turtle",
         "path"     : "$turtleBase/animals_basic.ttl",
         "mimetype" : "text/turtle"
       }],
       "settings" : {
          "logLevel" : "off",
          "sizeBatchProcessing" : 100
        }
       }
      """.stripMargin
  )

  def tests: Tests = Tests {
    test("order by asc") {
      SWDiscovery(config)
        .prefix("ns0", "http://www.some-ficticious-zoo.com/rdf#")
        .something("animal")
        .isSubjectOf(URI("ns0:name"), "name")
        .select(Seq("animal", "name"))
        .limit(10)
        .console
        .commit()
        .raw
        .map { response =>
          println(response("results")("bindings"))
          println(response("results")("datatype"))
        }
    }

    test("order by desc") {
      SWDiscovery(config)
        .prefix("ns0", "http://www.some-ficticious-zoo.com/rdf#")
        .something("animal")
        .isSubjectOf(URI("ns0:name"), "name")
        .select(Seq("animal", "name"))
        .limit(10)
        .console
        .commit()
        .raw
        .map { response =>
          println(response("results")("bindings"))
          println(response("results")("datatype"))
        }
    }
  }
}