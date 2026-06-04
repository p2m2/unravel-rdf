package fr.inrae.metabohub.semantic_web.js

import fr.inrae.metabohub.data.DataTestFactory
import fr.inrae.metabohub.semantic_web.configuration._
import utest.{TestSuite, Tests, test}

import scala.scalajs.js.JSConverters._

object UnravelQueryJsTest extends TestSuite{
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  val insertData = DataTestFactory.insertVirtuoso1(
    """<aa> <bb> <cc> .
       <aa> <bb> <dd> .
       <aa> a <ee> .
       <dd> <datatype_prop> "1" .
      """.stripMargin, this.getClass.getSimpleName)

  val config: UnravelConfig = DataTestFactory.getConfigVirtuoso1()

  override def utestAfterAll(): Unit = {
    DataTestFactory.deleteVirtuoso1(this.getClass.getSimpleName)
  }

  def startRequest =
    UnravelSessionJs(config)
      .graph(DataTestFactory.graph1(this.getClass.getSimpleName))
      .something("h1")
      .select()

  def tests = Tests {
    test("progression") {
      startRequest.progression(
        ( (percent: Double) => { } )
      )
    }
    test("requestEvent") {
      startRequest.requestEvent(
        ( (percent: String) => { } )
      )
    }

    test("abort") {
      startRequest.abort()
    }

    test("commit") {
      startRequest.commit()
    }
    test("raw") {
      startRequest.raw()
    }

    test("aggregate") {
      startRequest.aggregate("test")
    }
    test("distinct") {
      startRequest.distinct()
    }
    test("reduced") {
      startRequest.reduced()
    }
    test("limit") {
      startRequest.limit(1)
    }
    test("offset") {
      startRequest.offset(1)
    }
    test("orderByAsc") {
      startRequest.orderByAsc("h1")
    }
    test("orderByAsc") {
      startRequest.orderByAsc(Seq("h1").toJSArray)
    }
    test("orderByDesc") {
      startRequest.orderByAsc("h1")
    }
    test("orderByDesc") {
      startRequest.orderByAsc(Seq("h1").toJSArray)
    }
    test("getSerializedString") {
      startRequest.getSerializedString()
    }
    test("setSerializedString") {
      startRequest.setSerializedString(startRequest.getSerializedString())
    }
    test("console") {
      startRequest.console()
    }

  }
}
