// SPDX-FileCopyrightText: 2020-2026 INRAE
// SPDX-License-Identifier: GPL-3.0-or-later

package facade.npm

import org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits._
import utest.{assert, _}

import scala.language.implicitConversions
import scala.scalajs.js
import scala.util.{Failure, Success}

object QueryEngineTest extends TestSuite {
  val stdout = js.Dynamic.global.process.stdout
  private val DF = N3.DataFactory

  def initStore(): N3Store = {
    val store = new N3Store()

    store.addQuad(
      DF.quad(
        DF.namedNode("a"),
        DF.namedNode("b"),
        DF.namedNode("http://dbpedia.org/resource/Belgium")
      )
    )

    store.addQuad(
      DF.quad(
        DF.namedNode("a"),
        DF.namedNode("b"),
        DF.namedNode("http://dbpedia.org/resource/Ghent")
      )
    )

    store
  }

  val tests: Tests = Tests {
    test("newEngine bindings - N3Store - bindings ") {
      new QueryEngine().queryBindings(
        "SELECT * {  ?s ?p ?o . VALUES ?o { <http://dbpedia.org/resource/Belgium> } . } LIMIT 100",
        context = QueryEngineOptions(sources = List(initStore()))
      ).toFuture.onComplete {
        case Success(results) =>
          results.asInstanceOf[js.Dynamic].on("data", js.Any.fromFunction1 { (chunk: Any) =>
            val v = chunk.asInstanceOf[Bindings]
            assert(v.has("s"))
            assert(v.get("s").equalsTerm(DF.namedNode("a")))
            assert(v.get("p").equalsTerm(DF.namedNode("b")))
            assert(v.get("o").equalsTerm(DF.namedNode("http://dbpedia.org/resource/Belgium")))
          })

        case Failure(t) =>
          println("An error has occurred: " + t.getMessage)
          t.getStackTrace.foreach(println)
          assert(false)
      }
    }

    test("newEngine bindings - N3Store - Construct - quads ") {
      new QueryEngine().queryQuads(
        "CONSTRUCT WHERE { ?s ?p ?o } LIMIT 100",
        QueryEngineOptions(sources = List(initStore()))
      ).toFuture.onComplete {
        case Success(results) =>
          results.asInstanceOf[js.Dynamic].on("data", js.Any.fromFunction1 { (chunk: Any) =>
            val v = chunk.asInstanceOf[Quad]
            assert(v.subject.value == "a")
            assert(v.predicate.value == "b")
          })

        case Failure(t) =>
          println("An error has occurred: " + t.getMessage)
          t.getStackTrace.foreach(println)
          assert(false)
      }
    }

    test("Serializing to a specific result format") {
      new QueryEngine().query(
        "SELECT * {  ?s ?p ?o . VALUES ?o { <http://dbpedia.org/resource/Belgium> } . } LIMIT 100",
        QueryEngineOptions(sources = List(initStore()))
      ).toFuture.onComplete {
        case Success(results) =>
          val data = new QueryEngine().resultToString(results, "application/sparql-results+json")
          data.toFuture.onComplete {
            case Success(r) => r.data.pipe(stdout)
            case Failure(t) => println("message :" + t)
          }

        case Failure(t) =>
          println("An error has occurred: " + t.getMessage)
      }
    }

    test("Serializing to a specific result format 2 ") {
      new QueryEngine().query(
        "SELECT * {  ?s ?p ?o . VALUES ?o { <http://dbpedia.org/resource/Belgium> } . } LIMIT 100",
        QueryEngineOptions(sources = List(initStore()))
      ).toFuture.onComplete {
        case Success(results) =>
          val data = new QueryEngine().resultToString(results, "application/sparql-results+json")
          data.toFuture.onComplete {
            case Success(r) =>
              r.data.on("data", js.Any.fromFunction1 { (chunk: Any) =>
                println("chunk :" + chunk.toString)
              })
            case Failure(t) =>
              println("message :" + t)
          }

        case Failure(t) =>
          println("An error has occurred: " + t.getMessage)
      }
    }

    test("newEngine bindings - SOURCES = List(N3Store + hypermedia) ") {
      new QueryEngine().queryBindings(
        "SELECT ?s {  ?s ?p ?o . } LIMIT 100",
        QueryEngineOptions(
          sources = List(
            initStore(),
            SourceDefinitionNewQueryEngine(
              `type` = SourceType.hypermedia,
              value = "https://fragments.dbpedia.org/2016-04/en"
            )
          )
        )
      ).toFuture.onComplete {
        case Success(results) =>
          println("  = N3Store + file =")
          println(results)

          results.asInstanceOf[js.Dynamic]
            .on("data", js.Any.fromFunction1 { (chunk: Any) =>
              val v = chunk.asInstanceOf[Bindings]
              println("test....................")
              println("?s store+hypermedia ->")
              println(v.toString)
            })
            .on("end", js.Any.fromFunction1 { (_: Any) =>
              println(" ======== FIN store+hypermedia ============== ")
            })

        case Failure(t) =>
          println("An error has occurred: " + t.getMessage)
          t.getStackTrace.foreach(println)
          assert(false)
      }
    }
  }
}