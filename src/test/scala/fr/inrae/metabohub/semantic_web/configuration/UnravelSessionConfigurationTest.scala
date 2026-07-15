// SPDX-FileCopyrightText: 2020-2026 INRAE
// SPDX-License-Identifier: GPL-3.0-or-later

package fr.inrae.metabohub.semantic_web.configuration

import fr.inrae.metabohub.data.NodeEnv
import fr.inrae.metabohub.semantic_web.exception.SWStatementConfigurationException
import fr.inrae.metabohub.semantic_web.rdf.URI
import utest._
import wvlet.log.LogLevel

import scala.util.{Failure, Try}

object UnravelSessionConfigurationTest extends TestSuite {
  val turtleBase: String = NodeEnv.get("TURTLE_BASE_URL", "http://localhost:8080")
  val configBase: String = """
            {
             "sources" : [{
               "id"  : "dbpedia",
               "path" : "https://dbpedia.org/sparql",
               "content" : "",
               "mimetype" : "application/sparql-query",
               "method" : "POST"
             }],
             "settings" : {
               "cache" : true,
               "logLevel" : "info",
               "sizeBatchProcessing" : 10,
               "pageSize" : 10
             },
             "proxy" : null
            }
            """.stripMargin

  val configBaseProxy: String = s"""
            {
             "sources" : [{
               "id"  : "dbpedia",
               "path" : "https://dbpedia.org/sparql",
               "content" : "",
               "mimetype" : "application/sparql-query",
               "method" : "POST"
             }],
             "settings" : {
               "cache" : true,
               "logLevel" : "info",
               "sizeBatchProcessing" : 10,
               "pageSize" : 10
             },
             "proxy" : {
                "url"    : "$turtleBase",
                "method" : "post"
             }
            }
            """.stripMargin


  def tests: Tests = Tests {

    test("init") {
      UnravelConfig.init()
    }

    test("proxy") {
      UnravelConfig.proxy("http://something")
    }

    test("Create a simple source with string configuration") {
      UnravelConfig.setConfigString(configBase)
    }

    test("Create a string proxy configuration") {
      UnravelConfig.setConfigString(configBaseProxy)
    }

    test("Get message error") {
      Try(UnravelConfig.setConfigString("""
            {
            """.stripMargin)) match {
        case Failure(_: SWStatementConfigurationException) => assert(true)
        case _ => assert(false)
      }
    }

    test("Get message error - general setting - virgule") {
      Try(UnravelConfig.setConfigString("""
            {
             "settings" : {
               "cache" : true,
             }
             }
            """.stripMargin)) match {
        case Failure(_: SWStatementConfigurationException) => assert(true)
        case _ => assert(false)
      }
    }

    test("Get a unknown source") {
      assert(Try(UnravelConfig.setConfigString(configBase).source("something")).isFailure)
    }

    test("Create a simple source") {

      val dbname = "dbpedia"
      val url = "http://test"
      val mimetype = "application/sparql-query"

      val configDbpediaBasic: UnravelConfig =
        UnravelConfig(
          settings = GeneralSetting(),
          sources=Seq(Source(id=dbname, path=url, content="",mimetype=mimetype)),
          proxy = None)
      val source = configDbpediaBasic.source("dbpedia")

      assert(source.id == dbname)
      assert(source.path == url)
      assert(source.mimetype == mimetype)
    }

    test("create a proxy with default method") {
      assert(Try(UnravelConfig(
        settings = GeneralSetting(),
        sources=Seq(),
        proxy = Some(ProxyConfiguration("http://something")))).isSuccess)
    }

    test("create a proxy with unknown method") {
      assert(Try(UnravelConfig(
        settings = GeneralSetting(),
        sources=Seq(),
        proxy = Some(ProxyConfiguration("http://something","other")))).isFailure)
    }

    test("unknown mimetype") {
      assert(Try(UnravelConfig(
        settings = GeneralSetting(),
        sources=Seq(Source(id="dbpedia", path="http://test", content="",mimetype="-")),
        proxy = None)).isFailure)
    }

    test("unknown method") {
      assert(Try(UnravelConfig(
        settings = GeneralSetting(),
        sources=
        Seq(Source(id="dbpedia", path="http://test", content="",mimetype="application/sparql-query",method=Some("-"))),
        proxy = None)).isFailure)
    }

    test("Create a request config with an unknown log level ") {
      assert(UnravelConfig
        .setConfigString(
          configBase.replace("\"info\"",
          "\"hello.world\"")).settings._logLevel == LogLevel.WARN)
    }

    test("Create a request config log level debug ") {
      assert(Try(UnravelConfig
        .setConfigString(configBase.replace("\"info\"",
          "\"debug\"")).settings._logLevel == LogLevel.DEBUG).isSuccess)
    }

    test("Create a request config log level info ") {

      val c = UnravelConfig
        .setConfigString(configBase)
      assert(c.settings._logLevel == LogLevel.INFO)

    }
    test("Create a request config log level trace ") {
      val c = UnravelConfig
        .setConfigString(configBase.replace("\"info\"",
          "\"trace\""))
      assert(c.settings._logLevel == LogLevel.TRACE)
    }
    test("Create a request config log level warn ") {
      val c = UnravelConfig
        .setConfigString(configBase.replace("\"info\"",
          "\"warn\""))
      assert(c.settings._logLevel == LogLevel.WARN)
    }

    test("Create a request config log level error ") {
      val c = UnravelConfig
        .setConfigString(configBase.replace("\"info\"",
          "\"error\""))
      assert(c.settings._logLevel == LogLevel.ERROR)
    }

    test("Create a request config log level all ") {
      val c = UnravelConfig
        .setConfigString(configBase.replace("\"info\"",
          "\"all\""))
      assert(c.settings._logLevel == LogLevel.ALL)
    }

    test("Create a request config log level off ") {
      val c = UnravelConfig
        .setConfigString(configBase.replace("\"info\"",
          "\"off\""))
      assert(c.settings._logLevel == LogLevel.OFF)
    }

    test("pageSize can not be negative") {
      assert(Try(UnravelConfig
        .setConfigString(configBase.replace("\"pageSize\" : 10",
          "\"pageSize\" : -1"))).isFailure)
    }

    test("pageSize can be equal to zero") {
      assert(Try(UnravelConfig
        .setConfigString(configBase.replace("\"pageSize\" : 10",
          "\"pageSize\" : 0"))).isFailure)
    }
    test("pageSize") {
      val c = UnravelConfig
        .setConfigString(configBase.replace("\"pageSize\" : 10",
          "\"pageSize\" : 5"))
      assert(c.settings.pageSize == 5)
    }

    test("sizeBatchProcessing can not be negative") {
      assert(Try(UnravelConfig
        .setConfigString(configBase.replace("\"sizeBatchProcessing\" : 10",
          "\"sizeBatchProcessing\" : -1"))).isFailure)
    }
    test("sizeBatchProcessing can be equal to zero") {
      assert(Try(UnravelConfig
        .setConfigString(configBase.replace("\"sizeBatchProcessing\" : 10",
          "\"sizeBatchProcessing\" : 0"))).isFailure)
    }
    test("sizeBatchProcessing") {
      val c = UnravelConfig
        .setConfigString(configBase.replace("\"sizeBatchProcessing\" : 10",
          "\"sizeBatchProcessing\" : 5"))
      assert(c.settings.sizeBatchProcessing == 5)
    }
  }
}
