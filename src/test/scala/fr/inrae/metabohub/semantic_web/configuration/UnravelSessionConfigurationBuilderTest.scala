// SPDX-FileCopyrightText: 2020-2026 INRAE
// SPDX-License-Identifier: GPL-3.0-or-later

package fr.inrae.metabohub.semantic_web.configuration

import fr.inrae.metabohub.data.NodeEnv
import utest.{TestSuite, Tests, test}
import wvlet.log.LogLevel

import scala.util.Try

object UnravelSessionConfigurationBuilderTest extends TestSuite {
  val turtleBase: String = NodeEnv.get("TURTLE_BASE_URL", "https://localhost:8080")

  def tests: Tests = Tests {
    test("default") {
      assert(Try(UnravelConfig.init()).isSuccess)
    }

    test("urlfile") {
      val s = UnravelConfig.init().urlFile(s"$turtleBase/animals.ttl")
      assert(s.sources.length == 1)
      assert(s.sources.last.path == s"$turtleBase/animals.ttl")
      assert(s.sources.last.mimetype == "text/turtle")
    }

    test("sparqlEndpoint") {
      val s = UnravelConfig.init().sparqlEndpoint(s"$turtleBase/sparql")
      assert(s.sources.length == 1)
      assert(s.sources.last.path == s"$turtleBase/sparql")
      assert(s.sources.last.mimetype == "application/sparql-query")
    }

    test("sparqlEndpoint auth=basic") {
      val s = UnravelConfig.init().sparqlEndpoint(s"$turtleBase/sparql",auth="basic")
      assert(s.sources.length == 1)
      assert(s.sources.last.path == s"$turtleBase/sparql")
      assert(s.sources.last.mimetype == "application/sparql-query")
    }

    test("sparqlEndpoint login=xxxx") {
      val s = UnravelConfig.init().sparqlEndpoint(s"$turtleBase/sparql",login="xxxxx")
      assert(s.sources.length == 1)
      assert(s.sources.last.path == s"$turtleBase/sparql")
      assert(s.sources.last.mimetype == "application/sparql-query")
    }

    test("sparqlEndpoint password=xxxx") {
      val s = UnravelConfig.init().sparqlEndpoint(s"$turtleBase/sparql",password="xxxxx")
      assert(s.sources.length == 1)
      assert(s.sources.last.path == s"$turtleBase/sparql")
      assert(s.sources.last.mimetype == "application/sparql-query")
    }

    test("sparqlEndpoint login=xxxx,password=xxxx") {
      val s = UnravelConfig.init().sparqlEndpoint(s"$turtleBase/sparql",login="xxxxx",password="xxxxx")
      assert(s.sources.length == 1)
      assert(s.sources.last.path == s"$turtleBase/sparql")
      assert(s.sources.last.mimetype == "application/sparql-query")
    }

    test("sparqlEndpoint token=xxxx") {
      val s = UnravelConfig.init().sparqlEndpoint(s"$turtleBase/sparql",token="xxxxx")
      assert(s.sources.length == 1)
      assert(s.sources.last.path == s"$turtleBase/sparql")
      assert(s.sources.last.mimetype == "application/sparql-query")
    }

    test("localFile") {
      val s = UnravelConfig.init().localFile("/localhost/animals.ttl")
      assert(s.sources.length == 1)
      assert(s.sources.last.path == "/localhost/animals.ttl")
      assert(s.sources.last.mimetype == "text/turtle")
    }

    test("rdfContent") {
      val content= ":some :some2 :some3."
      val s = UnravelConfig.init().rdfContent(content)
      assert(s.sources.length == 1)
      assert(s.sources.last.content == content)
      assert(s.sources.last.mimetype == "content/turtle")
    }

    test("federation") {
      val content= ":some :some2 :some3."

      val s = UnravelConfig.init()
        .urlFile(s"$turtleBase/animals.ttl")
        .sparqlEndpoint(s"https://$turtleBase/sparql")
        .localFile("/localhost/animals.ttl")
        .rdfContent(content)

      assert(s.sources.length == 4)
      assert(s.sources.last.content == content)
    }

    test("getSourcesSize") {
      assert(UnravelConfig.init().localFile("/localhost/animals.ttl").sourcesSize == 1)
    }

    test("setPageSize/getPageSize") {
      assert(UnravelConfig.init().setPageSize(3).pageSize == 3)
    }

    test("setSizeBatchProcessing/getSizeBatchProcessing") {
      assert(UnravelConfig.init().setSizeBatchProcessing(3).sizeBatchProcessing == 3)
    }

    test("setLogLevel/getLogLevel") {
      assert(UnravelConfig.init().setLogLevel("debug").logLevel == "debug")
      assert(UnravelConfig.init().setLogLevel("info").settings._logLevel == LogLevel.INFO)
      assert(UnravelConfig.init().setLogLevel("some").settings._logLevel == LogLevel.WARN)
    }

    test("setCache/getCache") {
      assert(!UnravelConfig.init().setCache(false).cache)
      assert(UnravelConfig.init().setCache(true).cache)
    }

  }
}
