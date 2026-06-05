package fr.inrae.metabohub.semantic_web.js

import fr.inrae.metabohub.data.DataTestFactory
import fr.inrae.metabohub.semantic_web.configuration._
import fr.inrae.metabohub.semantic_web.rdf.URI
import utest.{TestSuite, Tests, test}

import scala.concurrent.Future
import scala.scalajs.js

object UnravelSessionHelperJsTest extends TestSuite {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  val insertData: Future[Any] = DataTestFactory.insertVirtuoso1(
    """<aa> <bb> <cc> .
       <aa> <bb> <dd> .
       <aa> a <ee> .
       <dd> <datatype_prop> "1" .
      """.stripMargin, this.getClass.getSimpleName)

  val config: UnravelConfig = DataTestFactory.getConfigVirtuoso1()

  override def utestAfterAll(): Unit = {
    DataTestFactory.deleteVirtuoso1(this.getClass.getSimpleName)
  }

  def startRequest: UnravelSessionJs =
    UnravelSessionJs(config)
    .graph(DataTestFactory.graph1(this.getClass.getSimpleName))
    .something("h1")

  def tests = Tests {
    test("count") {
      insertData.map(_ => {
        val session = startRequest.isSubjectOf(URI("<bb>"), "h1_obj")
        session
          .from("h1_obj")
          .finder
          .count(js.Array("h1_obj"))
          .toFuture
          .map( (count : Int) => { assert( count == 2 ) })
      }).flatten
    }

    test("classes 1") {
      insertData.map(_ => {
        val session = startRequest.isSubjectOf(URI("<bb>"), "h1_obj")
        session
          .from("h1_obj")
          .finder
            .classes()
            .toFuture
            .map( (lUris : js.Array[URI]) => {
              assert( lUris.toList == List(URI("ee")) )
            })
      }).flatten
    }
    test("classes 2") {
        insertData.map(_ => {
          val session = startRequest.isSubjectOf(URI("<bb>"), "h1_obj")
          session
            .from("h1_obj")
            .finder
            .classes()
            .toFuture
            .map((lUris: js.Array[URI]) => {
              assert(lUris.toList == List())
            })
        }).flatten
      }
      test("classes 3") {
        insertData.map(_ => {
          startRequest
            .finder
            .classes()
            .toFuture
            .map( (lUris : js.Array[URI]) => {
              assert( lUris.toList == List(URI("ee")) )
            })
        }).flatten
      }

    test("objectProperties 1") {
      insertData.map(_ => {
        startRequest
          .finder
          .objectProperties()
          .toFuture
          .map( (lUris : js.Array[URI]) => {
            assert( lUris.toList == List(URI("bb")) )
          })
      }).flatten
    }
    test("objectProperties 2") {
      insertData.map(_ => {
        val session = startRequest.isSubjectOf(URI("<bb>"), "h1_obj")
        session
          .from("h1_obj")
          .finder
          .objectProperties()
          .toFuture
          .map( (lUris : js.Array[URI]) => {
            assert( lUris.toList == List() )
          })
      }).flatten
    }
    test("subjectProperties 1") {
      insertData.map(_ => {
        startRequest
          .finder
          .subjectProperties()
          .toFuture
          .map( (lUris : js.Array[URI]) => {
            assert( lUris.toList.contains(URI("bb")) )
            assert( lUris.toList.contains(URI("datatype_prop")) )
          })
      }).flatten
    }
    test("subjectProperties 2") {
      insertData.map(_ => {
        val session = startRequest.isSubjectOf(URI("<bb>"), "h1_obj")
        session
          .from("h1_obj")
          .finder
          .subjectProperties()
          .toFuture
          .map( (lUris : js.Array[URI]) => {
            assert( lUris.toList == List(URI("bb")) )
          })
      }).flatten
    }
    test("datatypeProperties 1") {
      insertData.map(_ => {
        startRequest
          .finder
          .datatypeProperties()
          .toFuture
          .map((lUris: js.Array[URI]) => {
            assert(lUris.toList == List(URI("datatype_prop")))
          })
      }).flatten
    }
    test("datatypeProperties 2") {
      insertData.map(_ => {
        val session = startRequest.isSubjectOf(URI("<bb>"), "h1_obj")
        session
          .from("h1_obj")
          .finder
          .datatypeProperties()
          .toFuture
          .map( (lUris : js.Array[URI]) => {
            assert( lUris.toList == List(URI("datatype_prop")) )
          })
      }).flatten
    }
    test("datatypeProperties 3") {
      insertData.map(_ => {
        val session = startRequest.isObjectOf(URI("<bb>"), "h1_sub")
        session
          .from("h1_sub")
          .finder
          .datatypeProperties()
          .toFuture
          .map( (lUris : js.Array[URI]) => {
            assert( lUris.toList == List() )
          })
      }).flatten
    }
  }
}