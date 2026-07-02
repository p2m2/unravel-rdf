package fr.inrae.metabohub.semantic_web

import fr.inrae.metabohub.data.NodeEnv
import fr.inrae.metabohub.semantic_web.configuration.UnravelConfig
import utest.{TestSuite, Tests, test}

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

object UrlFileConfigurationTest extends TestSuite{


  def tests = Tests {
    test("urlFile configuration") {
      val turtleBase: String = NodeEnv.get("TURTLE_BASE_URL", "http://localhost:8080")
      val url_file = s"$turtleBase/animals.ttl"
        UnravelSession(UnravelConfig.init().urlFile(url_file))
          .something("h1",h1=>h1.in("?rel","?obj"))
          .select(Seq("h1","rel","obj"))
          .commit()
          .raw.map(r => {
            println(r)
            assert(r("results")("bindings").arr.nonEmpty)
          })
    }

    test("urlFile configuration mesh") {
      val url_file = s"http://id.nlm.nih.gov/mesh/D012140.nt"
      UnravelSession(UnravelConfig.init().urlFile(url_file))
        .something("h1",h1=>h1.in("?rel","?obj"))
        .select(Seq("h1","rel","obj"))
        .commit()
        .raw.map(r => {
          println(r)
          assert(r("results")("bindings").arr.nonEmpty)
        })


    }
  }
}

