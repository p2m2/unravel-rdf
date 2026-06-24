package fr.inrae.metabohub.semantic_web

import fr.inrae.metabohub.data.DataTestFactory
import fr.inrae.metabohub.semantic_web.configuration.UnravelConfig
import fr.inrae.metabohub.semantic_web.rdf.{IRI, Literal, SparqlBuilder, URI}
import utest.{TestSuite, Tests, test}

import scala.concurrent.Future

object BindTest extends TestSuite {
  implicit val ec: scala.concurrent.ExecutionContext = scala.scalajs.concurrent.JSExecutionContext.queue

  val insertData: Future[Any] = DataTestFactory.insertVirtuoso1(
    """
      <http://aa1> <http://bb> "abcdef" .
      <http://aa2> <http://bb> "abcdefghij" .
      <http://aa3> <http://bb> "abcdefghijklmn" .
      <http://aa1> <http://bb> "defijklm" .
      """.stripMargin, this.getClass.getSimpleName)

  val config: UnravelConfig = DataTestFactory.getConfigVirtuoso1()


  override def utestAfterAll(): Unit = {
      val _ = DataTestFactory.deleteVirtuoso1(this.getClass.getSimpleName)
  }

  def tests: Tests = Tests {
    val regexv = "defg"

    test("filter regex") {
      insertData.map(_ => {
        UnravelSession(config)
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .something(
            _.out(URI("http://bb"), "?r",_.filter.regex(regexv))
            )
          .select(Seq("r", "reg"))
          .distinct
          .commit()
          .raw.map(r => {
          assert(r("results")("bindings").arr.count(v => v("r")("value").toString.contains(regexv)) == 2)
        })
      }).flatten
    }

    test("bind replace") {
      val pat = "defg"
      val repl = "aaaaa"
      val req = UnravelSession(config)
        .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
        .something(_.out(URI("http://bb"), "?r",_.bind("rep").replace(pat, repl)))


      insertData.map(_ => {
        req
          .select(Seq("rep"))
          .distinct
          .commit()
          .raw.map(r => {
          assert(r("results")("bindings").arr.count(v => v("rep")("value").toString.contains(repl)) == 2)
        }).recover(e => println(e))
      }).flatten

      insertData.map(_ => {
        UnravelSession().setSerializedString(req.getSerializedString)
          .select(Seq("rep"))
          .distinct
          .commit()
          .raw.map(r => {
          println(r("results")("bindings"))
          assert(r("results")("bindings").arr.count(v => v("rep")("value").toString.contains(repl)) == 2)
        })
      }).flatten

    }

    test("bind abs") {
      insertData.map(_ => {
        UnravelSession(config)
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .something(
            _.set(Literal("-5.5", "http://www.w3.org/2001/XMLSchema#decimal"))
            .bind("new_value").abs())
          .select(Seq("new_value"))
          .commit()
          .raw.map(r => {
          assert(SparqlBuilder.createLiteral(r("results")("bindings").arr(0)("new_value")).toDouble == 5.5)
        })
      }).flatten
    }

    test("bind abs with something linked") {
      insertData.map(_ => {
        UnravelSession(config)
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .something(
            _.set(Literal("-5.5", "http://www.w3.org/2001/XMLSchema#decimal"))
            .bind("new_value").abs()
            .in(URI("http://test")))
          .select(Seq("new_value"))
          .commit()
          .raw.map(r => {
          assert(r("results")("bindings").arr.isEmpty)
        })
      }).flatten
    }

    test("bind round") {
      insertData.map(_ => {
        UnravelSession(config)
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .something(
            _.set(Literal("-5.5", "http://www.w3.org/2001/XMLSchema#decimal"))
              .bind("new_value").round()
          )
          .select(Seq("new_value"))
          .commit()
          .raw.map(r => {
          assert(SparqlBuilder.createLiteral(r("results")("bindings").arr(0)("new_value")).toInt == -5)
        })
      }).flatten
    }
    test("bind ceil") {
      insertData.map(_ => {
        UnravelSession(config)
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .something(
            _.set(Literal("-5.5", "http://www.w3.org/2001/XMLSchema#decimal"))
            .bind("new_value").ceil())
          .select(Seq("new_value"))
          .commit()
          .raw.map(r => {
          assert(SparqlBuilder.createLiteral(r("results")("bindings").arr(0)("new_value")).toInt == -5)
        })
      }).flatten
    }

    test("bind floor") {
      insertData.map(_ => {
        UnravelSession(config)
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .something(
            _.set(Literal("-5.5", "http://www.w3.org/2001/XMLSchema#decimal"))
              .bind("new_value").floor())
          .select(Seq("new_value"))
          .commit()
          .raw.map(r => {
          assert(SparqlBuilder.createLiteral(r("results")("bindings").arr(0)("new_value")).toInt == -6)
        })
      }).flatten
    }
    test("bind rand") {
      insertData.map(_ => {
        UnravelSession(config)
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .something(_.bind("new_value").rand())
          .select(Seq("new_value"))
          .commit()
          .raw.map(r => {
          val v = SparqlBuilder.createLiteral(r("results")("bindings").arr(0)("new_value")).toDouble
          assert(v <= 1.0 && v > 0.0)
        })
      }).flatten
    }
    test("datatype") {
      insertData.map(_ => {
        UnravelSession(config)
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .something(_.out(URI("http://bb"), "?r",_.bind("dt").datatype()))
          .select(Seq("dt"))
          .distinct
          .commit()
          .raw.map(r => {
          assert(
            SparqlBuilder.createLiteral(r("results")("bindings")(0)("dt")).naiveLabel == "http://www.w3.org/2001/XMLSchema#string"
          )
        })
      }).flatten
    }
    test("str") {
      insertData.map(_ => {
        UnravelSession(config)
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .something(
            _.in(URI("http://bb"), "?r",_.bind("convert_str").str())
            )
          .select(Seq("convert_str"))
          .distinct
          .commit()
          .raw.map(r => {
          assert(
            r("results")("bindings")(0)("convert_str")("type").value.toString.contains("literal")
          )
          assert(
            r("results")("bindings")(0)("convert_str")("value").value.toString.contains("http://")
          )
        })
      }).flatten
    }
  }

}
