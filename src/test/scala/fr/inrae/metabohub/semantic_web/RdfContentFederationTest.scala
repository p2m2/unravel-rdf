package fr.inrae.metabohub.semantic_web

import fr.inrae.metabohub.semantic_web.configuration.UnravelConfig
import fr.inrae.metabohub.semantic_web.rdf.{Var, URI}
import utest.{TestSuite, Tests, test}

import scala.concurrent.ExecutionContext.Implicits.global

object RdfContentFederationTest extends TestSuite {

  val turtleContent: String =
    """@prefix ns0: <http://www.some-ficticious-zoo.com/rdf#> .
      ns0:lion ns0:name "Lion" ;
               ns0:species "Panthera leo" ;
               ns0:class "Mammal" .
      ns0:tarantula
               ns0:name "Tarantula" ;
               ns0:species "Avicularia avicularia" ;
               ns0:class "Arachnid" .
      ns0:hippopotamus
               ns0:name "Hippopotamus" ;
               ns0:species "Hippopotamus amphibius" ;
               ns0:class "Mammal" .
      """.stripMargin

  val turtleContent2: String =
    """@prefix ns1: <http://www.some-ficticious-zoo.com/rdf#> .
      ns1:lion ns1:color "Yellow" .
      ns1:tarantula ns1:color "Black" .
      ns1:hippopotamus ns1:color "Grey" .
      """.stripMargin

  def tests: Tests = Tests {

    test("prefix with datatype") {
      val configTurtleContent: UnravelConfig = UnravelConfig
        .init()
        .rdfContent(turtleContent)
        .rdfContent(turtleContent2)
        .setPageSize(5)
        .setSizeBatchProcessing(10)
        .setLogLevel("info")
        .setCache(true)

      UnravelSession(configTurtleContent)
        .prefix("ns0", "http://www.some-ficticious-zoo.com/rdf#")
        .something("h1",
          _.out(URI("ns0:class"),Var("Mammal"))
            .datatype(URI("ns0:color"), "color")
        ).console
        .select(List("h1", "color"))
        .commit()
        .raw
        .map { result =>
          assert(result("results")("datatypes")("color").obj.size == 3)
        }
    }
  }
}