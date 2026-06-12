package fr.inrae.metabohub.semantic_web

import fr.inrae.metabohub.data.DataTestFactory
import fr.inrae.metabohub.semantic_web.configuration._
import fr.inrae.metabohub.semantic_web.rdf._
import utest._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object UnravelSessionTraverseTest  extends TestSuite {

  val insertData: Future[Any] = DataTestFactory.insertVirtuoso1(
    """
      <some_subject> <http://bb_1> <http://aa> .
      <http://aa> <http://bb_2> <some_object1> .
      <http://aa> <http://bb_2> <some_object2> .
      <some_subject> <datatype_prop> "value1" .
      <some_object1> <datatype_prop2> "value2" .
      <http://bb_1>  <datatype_prop3> "value3" .
      """.stripMargin, this.getClass.getSimpleName)

  val config: UnravelConfig = DataTestFactory.getConfigVirtuoso1()

  override def utestAfterAll(): Unit = {
    DataTestFactory.deleteVirtuoso1(this.getClass.getSimpleName)
  }


  def tests: Tests = Tests {
   test("basic") {
      insertData.map(_ => {
        UnravelSession(config)
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .something("h1", _.traverse("?property","?var"))
          .select()
          .distinct
          .commit()
          .raw
          .map(r => {
            r("results")("bindings").arr.foreach( s=>{
              println(s"${s("h1")("value")} ${s("property")("value")} ${s("var")("value")}" )
            assert(r("results")("bindings").arr.length==12)
            })
          })
      }).flatten
    }

   test("traverse with h1 variate detection") {
      insertData.map(_ => {
        UnravelSession(config)
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .something("h1",
            _.set(URI("http://aa")).traverse("?property","?var"))
          .select(List("h1"))
          .distinct
          .commit()
          .raw
          .map(r => {
            assert(r("results")("bindings").arr.length==1)
            assert(r("results")("bindings").arr.forall(x => x.obj("h1")("value").str.equals("http://aa")))
          })
      }).flatten
    }
    test("traverse with var variate detection") {
      insertData.map(_ => {
        UnravelSession(config)
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .something("h1",
            _.set(URI("http://aa")).traverse("?property","?var"))
          .select(List("var"))
          .distinct
          .commit()
          .raw
          .map(r => {
            assert(r("results")("bindings").arr.length == 3)
            assert(r("results")("bindings").arr.forall(x =>
              x.obj("var")("value").str.contains("some_")))
            })
      }).flatten
    }
    test("traverse with property variate detection") {
      insertData.map(_ => {
        UnravelSession(config)
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .something("h1",
            _.set(URI("http://aa")).traverse("?property","?var"))
          .select(List("property"))
          .distinct
          .commit()
          .raw
          .map(r => {
            assert(r("results")("bindings").arr.length == 2)
            assert(r("results")("bindings").arr.forall(x =>
              x.obj("property")("value").str.contains("bb_")))
          })
      }).flatten
    }
    test("traverse adding triplet constraint - h1") {
      insertData.map(_ => {
        UnravelSession(config)
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .something("h1",
            _.set(URI("http://aa"))
              .traverse("?property","?var"))
          .from("h1",_.out(URI("datatype_prop"),"?datatype"))
          .select(List("datatype"))
          .distinct
          .commit()
          .raw
          .map(r => {
            assert(r("results")("bindings").arr.isEmpty)
          })
      }).flatten
    }

    test("traverse adding triplet constraint - Var ") {
      insertData.map(_ => {
        UnravelSession(config)
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .something("h1",
            _.set(URI("http://aa"))
              .traverse("?property","?var"))
          .from("var",_.out(URI("datatype_prop"),"?datatype"))
          .select(List("datatype"))
          .distinct
          .commit()
          .raw
          .map(r => {
            assert(r("results")("bindings").arr.length == 1)
          })
      }).flatten
    }

    test("traverse adding triplet constraint - Var ") {
      insertData.map(_ => {
        UnravelSession(config)
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .something("h1",
            _.set(URI("http://aa"))
              .traverse("?property", "?var"))
          .from("property", _.console.out(URI("datatype_prop3"), "?datatype"))
          .select(List("datatype"))
          .commit()
          .raw
          .map(r => {
            assert(r("results")("bindings").arr.length == 1)
          })
      }).flatten
    }
  }
}
