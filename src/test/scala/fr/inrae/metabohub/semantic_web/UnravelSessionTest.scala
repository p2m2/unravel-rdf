package fr.inrae.metabohub.semantic_web

import fr.inrae.metabohub.data.DataTestFactory
import fr.inrae.metabohub.semantic_web.node.{Node, Root}
import fr.inrae.metabohub.semantic_web.rdf._
import fr.inrae.metabohub.semantic_web.configuration._
import utest._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.concurrent.Future
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

object UnravelSessionTest extends TestSuite {

  val insertData: Future[Any] = DataTestFactory.insertVirtuoso1(
    """
      <http://aa> <http://bb> <http://cc> .
      <http://aa> <http://bb2> <http://cc2> .
      <http://aa> <http://bb2> <http://cc3> .

      <http://bb2> a owl:ObjectProperty .

      <http://aa1> a <http://LeafType> .

      <http://aa2> a <http://LeafType> .
      <http://aa2> a <http://OwlClass> .


      <http://aa3> <http://propDatatype> "test" .

      <http://OwlClass> a owl:Class .
      """.stripMargin, this.getClass.getSimpleName)

  val config: UnravelConfig = DataTestFactory.getConfigVirtuoso1()

  override def utestAfterAll(): Unit = {
    DataTestFactory.deleteVirtuoso1(this.getClass.getSimpleName)
  }

  def startRequest: UnravelSession =
    UnravelSession(config)
      .graph(DataTestFactory.graph1(this.getClass.getSimpleName))
      .something("h1")

  def tests = Tests {
    test("No sources definition") {
      insertData.map(_ => {
        val config: UnravelConfig = UnravelConfig.setConfigString(""" { "sources" : [] } """)
        UnravelSession(config)
          .something("h1")
          .select(List("h1"))
          .commit()
          .raw
          .map(_ => assert(false))
          .recover(_ => assert(true))
      }).flatten
    }

    test("something") {
      insertData.map(_ => {
        startRequest
          .select(List("h1"))
          .commit()
          .raw
          .map(_ => assert(true))
      }).flatten
    }

    test("isSubjectOf") {
      insertData.map(_ => {
        startRequest
          .from("h1",
            _.set(URI("http://aa"))
             .out(URI("http://bb"), "?var"))
          .select(List("var"))
          .commit()
          .raw
          .map(result => {
            assert(result("results")("bindings").arr.length == 1)
            assert(SparqlBuilder.createUri(result("results")("bindings")(0)("var")).localName == "http://cc")
          })
      }).flatten
    }
    test("datatype 1") {
      insertData.map(_ => {
        startRequest
          .from("h1",
            _.set(URI("http://aa3"))
             .datatype(URI("http://propDatatype"), "d"))
          .select(List("h1","d"))
          .commit()
          .raw
          .map(
            response => {
              println(response)
              assert(response("results")("datatypes")("d")("http://aa3")(0)("value").toString().nonEmpty)
            }
          )
      }).flatten
    }

    test("datatype 2") {
      insertData.map(_ => {
        startRequest
          .from("h1",
            _.set(URI("http://aa3"))
             .datatype(URI("http://propDatatype"), "d"))
          .select(List("d","h1"))
          .commit()
          .raw
          .map(
            response => {
              assert(response("results")("datatypes")("d")("http://aa3")(0)("value").toString().nonEmpty)
            }
          )
      }).flatten
    }

    test("datatype 3") {
        Try(
          startRequest
            .from("h1",
              _.set(URI("http://aa3"))
              .datatype(URI("http://propDatatype"), "d"))
          .select(List("d"))
          .commit()) match {
            case Success(_) => assert(false)
            case Failure(_) => assert(true)
          }
    }

    test("datatype 4") {
      insertData.map(_ => {
        startRequest
          .from("h1",_.set(URI("http://aa3"))
          .datatype(URI("http://propDatatype"), "d"))
          .select(List("h1"))
          .commit()
          .raw
          .map(
            response => {
              assert(SparqlBuilder.createUri(response("results")("bindings")(0)("h1")).localName == "http://aa3" )
            }
          )
      }).flatten
    }

    test("focus") {
      val disco = UnravelSession(config)
      val f = disco.current()
      assert(Try(disco.from(f)).isSuccess)
      assert(Try(disco.something("h1").from(f)).isSuccess)
    }

    test("bad focus") {
      assert(Try(startRequest.from("h2")).isFailure)
    }

    test("use named graph") {
      assert(Try(startRequest.from("h1",_.out(URI("http://bb2")))).isSuccess)
    }

    test("test console") {
      assert(Try(startRequest.from("h1",_.out(URI("http://bb2"))).console).isSuccess)
    }

    test("refExist") {
      assert(Try(startRequest.refExist("h1")).isSuccess)
    }

    test("refExist2") {
      assert(Try(startRequest.refExist("h2")).isFailure)
    }

    test("remove Something h1") {
      val sw = startRequest.remove("h1")
      assert(sw.rootNode.idRef == sw.current())
      sw.something("h",h=>{assert(h.current() == "h");h})
    }

    test("Remove nothing") {

      val sw =  UnravelSession(config)
                  .remove("h1")
      assert(sw.rootNode.idRef == sw.current())

    }

    test("Remove root") {
      val sw =  UnravelSession(config)
      sw.remove(sw.rootNode.idRef)
      assert(sw.rootNode.idRef == sw.current())
    }

    test("Remove branch") {
      UnravelSession(config)
          .something("h1",
            _.in(URI("http://h1"),"?h2")
             .in(URI("http://h11"),"?h22")
          )
          .something("d1",
             _.in(URI("http://d1"),"?d2")
              .in(URI("http://d11"),"?d22"))
            .remove("h1")
          .browse( (n: Node, _:Integer) => {
            n match {
              case _ : Root => assert(true)
              case _ => assert(n.idRef.startsWith("d"))
            }
          })
    }

    test("browse") {
      val listBrowse : Seq[String] =
        startRequest
          .something("h1",_.out("http://test",Var("h2")))
         .browse( (n : Node, _:Integer) => { n.idRef} )
      assert( listBrowse.contains("h1") )
      assert( listBrowse.contains("h2") )
    }

    test("sparql get") {
       assert(startRequest
         .from("h1",_.out("http://test", "h2"))
         .sparql_get.nonEmpty)
    }

    test("sparql curl") {
      assert(startRequest
        .from("h1",_.out("http://test", "h2"))
        .sparql_curl.nonEmpty)
    }

    test("prefix") {
      assert(
        startRequest
          .prefix("some","http://something")
          .getPrefix("some") == IRI("http://something"))
    }

    test("prefix 2") {
      assert(
        startRequest
          .prefix("some","http://something")
          .getPrefixes().contains("some") )
    }
    test("prefix 3") {
      assert(
        startRequest
          .prefixes(Map("some"->"http://something"))
          .getPrefixes().contains("some") )
    }

    test("setConfig/getConfig") {
      assert(startRequest.getConfig.sources.head.id == DataTestFactory.getConfigVirtuoso1().sources.head.id)

      assert(startRequest.setConfig(DataTestFactory.getConfigVirtuoso2()).getConfig.sources.head.id ==
        DataTestFactory.getConfigVirtuoso2().sources.head.id)
    }

    test("setConfig/getConfig during query build") {
      assert(
        startRequest
        .setConfig(DataTestFactory.getConfigVirtuoso2())
         .from("h1",_.in("http://test11"))
          .getConfig.sources.head.id == DataTestFactory.getConfigVirtuoso2().sources.head.id )
    }

    test("namedGraph") {
      assert(Try(startRequest.namedGraph(DataTestFactory.graph1(this.getClass.getSimpleName))).isSuccess)
    }

    test("directive") {
      assert(Try(startRequest.directive("something directive")
        .toString.startsWith("something directive")).isSuccess)
    }

    test("decoration") {
      assert(UnravelSession(config).something("h").setDecoration("test","something").getDecoration("test")
        == "something")
    }

    test("bad decoration") {
      assert(Try(UnravelSession(config,rootNode = Root(),fn =  Some("idNotExistInRootNode"))
        .setDecoration("bad declaration because not node is defined","something")).isFailure)
    }

    test("get unknown decoration") {
      assert(UnravelSession(config).something("h").getDecoration("test") == "")
    }

    test("sparql_get without configuration sources definition") {
      assert(Try(UnravelSession(UnravelConfig.init()).sparql_get).isSuccess)
    }

  }
}
