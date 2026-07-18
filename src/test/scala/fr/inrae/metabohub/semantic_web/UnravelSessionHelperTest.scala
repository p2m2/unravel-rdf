// SPDX-FileCopyrightText: 2020-2026 INRAE
// SPDX-License-Identifier: GPL-3.0-or-later

package fr.inrae.metabohub.semantic_web
import fr.inrae.metabohub.data.DataTestFactory
import fr.inrae.metabohub.semantic_web.rdf._
import fr.inrae.metabohub.semantic_web.configuration._
import utest._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.concurrent.Future

object UnravelSessionHelperTest  extends TestSuite  {

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
      <http://aa3> <http://objprop> <http://something_else> .

      <http://OwlClass> a owl:Class .
      """.stripMargin, this.getClass.getSimpleName)

  val config: UnravelConfig = DataTestFactory.getConfigVirtuoso1()

  override def utestAfterAll(): Unit = {
    DataTestFactory.deleteVirtuoso1(this.getClass.getSimpleName)
  }


  def tests: Tests = Tests {
   test("count") {
     insertData.map(_ => {
        UnravelSession(config)
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .something("h1", //http://rdf.ebi.ac.uk/terms/chembl#BioComponent
          _.out(URI("http://bb2"),"?obj"))
          .from("obj").finder
          .count(Seq("h1"))
          .map(count => assert(count == 2))
      }).flatten
    }

    test("count distinct") {
      insertData.map(_ => {
        UnravelSession(config)
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .something("h1", //http://rdf.ebi.ac.uk/terms/chembl#BioComponent
            _.out(URI("http://bb2"),"?obj"))
          .from("obj").finder
          .count(Seq("h1"),distinct = true)
          .map(count => assert(count == 1))
      }).flatten
    }

    test("count with datatype") {
      insertData.map(_ => {
        UnravelSession(config)
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .something("h1", //http://rdf.ebi.ac.uk/terms/chembl#BioComponent
             _.datatype(URI("http://fake/"),"dt1")
             .out(URI("http://bb2"),"?obj"))
          .from("obj",_.finder
          .count(Seq("h1"))
          .map(count => assert(count == 2)))
      }).flatten
    }

    test("findClasses") {
      val query = UnravelSession(config)
        .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
        .something("h1",
        _.set(URI("http://aa1")))

      insertData.map(_ => {
        query.from("h1",_.finder.classes()
          .map(types => assert(types.length == 1)))
      }).flatten

      insertData.map(_ => {
        query.from("h1",_.finder.classes("", "", 1)
          .map(types => assert(types.isEmpty)))
      }).flatten

      insertData.map(_ => {
        query.from("h1",_.finder.classes("eaf")
          .map(types => assert(types.length == 1)))
      }).flatten

      insertData.map(_ => {
        query.from("h1",_.finder.classes("^(eaf)")
          .map(types => assert(types.isEmpty)))
      }).flatten

    }

    test("findClasses with mother class -> owl:Class") {
      val query = UnravelSession(config)
        .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
        .something("h1",_.set(URI("http://aa2")))

      insertData.map(_ => {
        query.from("h1",_.finder.classes()
          .map(types => assert(types.length == 1)))
      }).flatten

      insertData.map(_ => {
        query.from("h1",_.finder.classes(regex="")
          .map(types => assert(types.length == 1)))
      }).flatten

      insertData.map(_ => {
        query.from("h1",_.finder.classes("", URI("Class", "owl"))
          .map(types => assert(types.length == 1)))
      }).flatten

      insertData.map(_ => {
        query.from("h1",_.finder.classes("OwlClass", URI("Class", "owl"))
          .map(types => assert(types.length == 1)))
      }).flatten

      insertData.map(_ => {
        query.from("h1",_.finder.classes("eafTyp", URI("Class", "owl"))
          .map(types => assert(types.isEmpty)))
      }).flatten

      insertData.map(_ => {
        query.from("h1",_.finder.classes("OwlClass", URI("Class", "owl"), 1)
          .map(types => assert(types.isEmpty)))
      }).flatten
    }


    test("findObjectProperties") {
      val query = UnravelSession(config).something("h1",_.set(URI("http://aa"))).finder

      insertData.map(_ => {
        query.objectProperties()
          .map(response => assert(response.length == 2))
      }).flatten

      insertData.map(_ => {
        query.objectProperties("bb")
          .map(response => assert(response.length == 2))
      }).flatten

      insertData.map(_ => {
        query.objectProperties("bb", "")
          .map(response => assert(response.length == 2))
      }).flatten

      insertData.map(_ => {
        query.objectProperties("bb", "", 1)
          .map(response => assert(response.isEmpty))
      }).flatten
    }

    test("findObjectProperties mother class --> owl:ObjectProperty ") {
      val query = UnravelSession(config).something("h1", _.set(URI("http://aa")))
        .finder

      insertData.map(_ => {
        query.objectProperties("", URI("ObjectProperty", "owl"))
          .map(response => assert(response.length == 1))
      }).flatten

      insertData.map(_ => {
        query.objectProperties("bb", URI("ObjectProperty", "owl"))
          .map(response => assert(response.length == 1))
      }).flatten

      insertData.map(_ => {
        query.objectProperties("bb", URI("ObjectProperty", "owl"), 1)
          .map(response => assert(response.isEmpty))
      }).flatten
    }

    test("datatypeProperties") {
      val query = UnravelSession(config).something("h1",_.set(URI("http://aa3"))).finder

      insertData.map(_ => {
        query.datatypeProperties()
          .map(response => assert(response.length == 1))
      }).flatten

      insertData.map(_ => {
        query.datatypeProperties("propDatatype")
          .map(response => assert(response.length == 1))
      }).flatten

      insertData.map(_ => {
        query.datatypeProperties("propDatatype", "")
          .map(response => assert(response.length == 1))
      }).flatten

      insertData.map(_ => {
        query.datatypeProperties("propDatatype", "", 1)
          .map(response => assert(response.isEmpty))
      }).flatten
    }

    test("subjectProperties 1") {

      val query = UnravelSession(config).something("h1", _.set(URI("http://cc")))
      insertData.map(_ => {
        query.from("h1",_.finder.subjectProperties().map(response => assert(response.length == 1)))
      }).flatten
    }

    test("subjectProperties 2") {

      val query = UnravelSession(config).something("h1", _.set(URI("http://cc")))

      insertData.map(_ => {
        query.from("h1",_.finder.subjectProperties("bb").map(response => assert(response.length == 1)))
      }).flatten

    }

    test("subjectProperties 3") {

      val query = UnravelSession(config).something("h1", _.set(URI("http://cc")))
      insertData.map(_ => {
        query.from("h1",_.finder.subjectProperties("bb", "").map(response => assert(response.length == 1)))
      }).flatten
    }
    test("subjectProperties 4") {
      val query = UnravelSession(config).something("h1", _.set(URI("http://cc")))
      insertData.map(_ => {
        query.from("h1",_.finder.subjectProperties("bb",
          URI("http://anything")).map(response => assert(response.isEmpty)))
      }).flatten
    }
    test("subjectProperties 5") {
      val query = UnravelSession(config).something("h1", _.set(URI("http://cc")))

      insertData.map(_ => {
        query.from("h1",_.finder.subjectProperties("bb", "", 1))
          .map(response => assert(response.isEmpty))
      }).flatten
    }
  }

}
