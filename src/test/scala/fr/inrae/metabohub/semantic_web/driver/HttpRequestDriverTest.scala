package fr.inrae.metabohub.semantic_web.driver

import fr.inrae.metabohub.data.{DataTestFactory, NodeEnv}
import fr.inrae.metabohub.semantic_web.configuration.SourcePath
import utest.{TestRunner, TestSuite, Tests, test}
import wvlet.log.{LogLevel, Logger}

import scala.concurrent.Future

object HttpRequestDriverTest extends TestSuite {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global
  val turtleBase: String = NodeEnv.get("TURTLE_BASE_URL", "http://localhost:8080")

  val insertData : Future[Any] = DataTestFactory.insertVirtuoso1(
    """
      <aaRosHttpDriverTest> <bb> <cc> .
      """.stripMargin, this.getClass.getSimpleName)


  Logger.setDefaultLogLevel(LogLevel.OFF)


  val query : String = "select ?b ?c where { <aaRosHttpDriverTest> ?b ?c . } limit 1"

  def tests: Tests = Tests {

    test("AxiosRequestDriver get") {
      insertData.map(_ => {
        AxiosRequestDriver(idName = "test", method = "get", url = DataTestFactory.urlEndpoint)
          .request(query)
          .map(qr => {
            assert(qr.json("results")("bindings").arr(0)("b")("value").value == "bb")
            assert(qr.json("results")("bindings").arr(0)("c")("value").value == "cc")
          })
      }).flatten
    }

    test("AxiosRequestDriver get bad request") {
      insertData.map(_ => {
        AxiosRequestDriver(idName = "test", method = "get", url = DataTestFactory.urlEndpoint)
          .request("bad request")
          .map(_ => assert(false))
          .recover(_ => assert(true))
      }).flatten
    }

    test("AxiosRequestDriver post") {
      insertData.map(_ => {
        AxiosRequestDriver(idName = "test", method = "post", url = DataTestFactory.urlEndpoint)
          .request(query)
          .map(qr => {
            assert(qr.json("results")("bindings").arr(0)("b")("value").value == "bb")
            assert(qr.json("results")("bindings").arr(0)("c")("value").value == "cc")
          })
      }).flatten
    }

    test("AxiosRequestDriver post bad request") {
      insertData.map(_ => {
        AxiosRequestDriver(idName = "test", method = "post", url = DataTestFactory.urlEndpoint)
          .request("bad request")
          .map(_ => assert(false))
          .recover(_ => assert(true))
      }).flatten
    }

    test("ComunicaRequestDriver TTL") {
      val url_file = s"$turtleBase/animals.ttl"
      ComunicaRequestDriver(idName = "test", path = url_file, sourcePath =SourcePath.UrlPath,mimetype="text/turtle")
        .request("select * where { ?a ?b ?c . } limit 5")
        .map(qr => {

          println(url_file + " --> " + qr.json("results")("bindings").arr.length)
          assert(qr.json("results")("bindings").arr.length == 5)
        })
        .recover(err => {
          println(err)
          assert(false)
        })

    }

    test("ComunicaRequestDriver JSON-LD") {
      val url_file = s"$turtleBase/animals.jsonld"
      ComunicaRequestDriver(idName = "test", path = url_file, sourcePath=SourcePath.UrlPath,mimetype="application/json+ld")
        .request("select * where { ?a ?b ?c . } limit 5")
        .map(qr => {
          println(url_file + " --> " + qr.json("results")("bindings").arr.length)
          assert(qr.json("results")("bindings").arr.length == 5)
        })
        .recover(_ => {
          assert(false)
        })
    }

    test("ComunicaRequestDriver N3") {
      val url_file = s"$turtleBase/animals.n3"
      ComunicaRequestDriver(idName = "test", path = url_file, sourcePath=SourcePath.UrlPath,mimetype="text/n3")
        .request("select * where { ?a ?b ?c . } limit 5")
        .map(qr => {
          println(url_file + " --> " + qr.json("results")("bindings").arr.length)
          assert(qr.json("results")("bindings").arr.length == 5)
        })
        .recover(_ => {
          assert(false)
        })
    }

  }
  TestRunner.runAsync(tests).map { _ => DataTestFactory.deleteVirtuoso1(this.getClass.getSimpleName) }
}
