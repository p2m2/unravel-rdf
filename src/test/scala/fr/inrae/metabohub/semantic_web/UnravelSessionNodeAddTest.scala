package fr.inrae.metabohub.semantic_web

import fr.inrae.metabohub.data.DataTestFactory
import fr.inrae.metabohub.semantic_web.rdf._
import fr.inrae.metabohub.semantic_web.configuration._
import utest._

import scala.language.postfixOps
import scala.util.matching.Regex
import scala.util.{Failure, Success, Try}

object UnravelSessionNodeAddTest extends TestSuite {

  val config: UnravelConfig = DataTestFactory.getConfigVirtuoso1()

  def tests: Tests = Tests {

    test("something") {
      UnravelSession(config).something("h1")
    }

    test("isSubjectOf on the root") {
      Try(UnravelSession(config).out(URI("bb"), "var")) match {
        case Success(_) => assert(false)
        case Failure(_) => assert(true)
      }
    }

    test("isSubjectOf") {
      val s = UnravelSession(config)
               .something("h1",_.out(URI("bb"), "?var"))

      val triplet: Regex = "\\?h1+ <bb> \\?var+".r

      triplet.findFirstMatchIn(s.sparql) match {
        case Some(_) => assert(true)
        case None => assert(false)
      }
    }

    test("isObjectOf on the root") {
      Try(UnravelSession(config)
        .from("h1",_.in(URI("bb"), "var"))) match {
        case Success(_) => assert(false)
        case Failure(_) => assert(true)
      }
    }

    test("isObjectOf") {
      val s = UnravelSession(config)
        .something("h1",_.in(URI("bb"), "?var"))

      val triplet: Regex = "\\?var <bb> \\?h1".r
      triplet.findFirstMatchIn(s.sparql) match {
        case Some(_) => assert(true)
        case None => assert(false)
      }
    }

    test("isLinkTo on the root") {

      Try(UnravelSession(config).from("h1",_.out(Var("var"),URI("bb")))) match {
        case Success(_) => assert(false)
        case Failure(_) => assert(true)
      }
    }

    test("isLinkTo") {
      val s = UnravelSession(config)
        .something("h1",_.out(Var("var"),URI("bb")))

      val triplet: Regex = "\\?h1 \\?var+ <bb>".r

      triplet.findFirstMatchIn(s.sparql) match {
        case Some(_) => assert(true)
        case None => assert(false)
      }
    }

    test("isLinkFrom on the root") {
      Try(UnravelSession(config).from("h1",_.in(Var("var"),URI("bb")))) match {
        case Success(_) => assert(false)
        case Failure(_) => assert(true)
      }
    }

    test("isLinkFrom") {
      val s =
        UnravelSession(config)
          .something("h1",_.in(Var("var"),URI("<bb>")))

      val triplet: Regex = "<bb> \\?var \\?h1".r

      triplet.findFirstMatchIn(s.sparql) match {
        case Some(_) => assert(true)
        case None => println(s.sparql);assert(false)
      }
    }

    test("isLinkFrom QueryVariable") {
      val s =
        UnravelSession(config)
        .something("h1",_.in(Var("var"),URI("<bb>")))

      val triplet: Regex = "<bb> \\?var \\?h1".r
      println(s.sparql)
      triplet.findFirstMatchIn(s.sparql) match {
        case Some(_) => assert(true)
        case None => assert(false)
      }
    }

    test("isA on the root") {
      Try(UnravelSession(config).from("h1",_.isA(URI("class")))) match {
        case Success(_) => assert(false)
        case Failure(_) => assert(true)
      }
    }

    test("isA") {
      val s = UnravelSession(config)
        .something("h1",_.isA(URI("class")))

      val triplet: Regex = "\\?h1 rdf:type <class>".r

      triplet.findFirstMatchIn(s.sparql) match {
        case Some(_) => assert(true)
        case None => println(s.sparql);assert(false)
      }
    }
  }
}
