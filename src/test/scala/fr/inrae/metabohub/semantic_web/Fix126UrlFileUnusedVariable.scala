package fr.inrae.metabohub.semantic_web

import fr.inrae.metabohub.data.NodeEnv
import fr.inrae.metabohub.semantic_web.rdf.URI
import fr.inrae.metabohub.semantic_web.configuration._
import utest.{TestSuite, Tests, test}

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

object Fix126UrlFileUnusedVariable extends TestSuite {
  val turtleBase: String = NodeEnv.get("TURTLE_BASE_URL", "http://localhost:8080")
  val config: UnravelConfig = UnravelConfig.setConfigString(
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
        """.stripMargin)

  def tests = Tests {
    test("order by") {
        UnravelSession(config)
          .prefix("ns0","http://www.some-ficticious-zoo.com/rdf#")
          .something("animal",_.datatype(URI("ns0:name"),"name")).console
          .select(List("animal","name"))
          .commit()
          .raw.map(response => {
            assert(response("results")("bindings").arr.nonEmpty)
            assert(response("results")("datatypes").obj.nonEmpty)
          }
        )
    }
  }
}

