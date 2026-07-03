package fr.inrae.metabohub.semantic_web

import fr.inrae.metabohub.data.DataTestFactory
import fr.inrae.metabohub.semantic_web.rdf.URI
import fr.inrae.metabohub.semantic_web.configuration._
import utest.{TestSuite, Tests, test}

object Fix144TriplyDb extends TestSuite {
  val config: UnravelConfig = UnravelConfig.setConfigString(
    """
        {
          "sources" : [{
               "id"  : "triplydb",
               "path" : "https://api.triplydb.com/datasets/gr/gr/services/gr/sparql",
               "content" : "",
               "mimetype" : "application/sparql-query"
            }],
            "settings" : {
              "cache" : true,
              "logLevel" : "info",
              "sizeBatchProcessing" : 10,
              "pageSize" : 10
             }
        }
        """.stripMargin)

  override def utestAfterAll(): Unit = {
    DataTestFactory.deleteVirtuoso1(this.getClass.getSimpleName)
  }

  def tests = Tests {
    test("Fix Triplydb access #144") {
        UnravelSession(config)
          .something("h1",
            _.out(URI("a"),"?type",
             _.filter.contains("Business")))
          .select(Seq("type"))

      /*
      ********* Inactive test because remote access ******
          .commit()
          .raw.map(r => {
            println(r)
        })*/
    }
  }
}

