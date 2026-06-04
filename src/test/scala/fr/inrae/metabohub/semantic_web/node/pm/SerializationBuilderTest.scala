package fr.inrae.metabohub.semantic_web.node.pm

import fr.inrae.metabohub.data.{ApplyAllNode, DataTestFactory}
import fr.inrae.metabohub.semantic_web.{UnravelSession, UnravelQuery}
import fr.inrae.metabohub.semantic_web.node.Root
import fr.inrae.metabohub.semantic_web.rdf.{Literal, QueryVariable, URI}
import upickle.default.{read, write}
import utest.{TestSuite, Tests, test}


object SerializationBuilderTest extends TestSuite  {
  def tests: Tests = Tests {
    test("serialization basic 1") {
      val sw = UnravelSession( DataTestFactory.getConfigVirtuoso1(),Root(),Some("test"))
      assert(UnravelSession().setSerializedString(sw.getSerializedString) == sw)
      val swt : UnravelQuery = sw.select()
      assert(UnravelQuery().setSerializedString(swt.getSerializedString) == swt)
    }

    test("serialization basic 2") {
      val sw = UnravelSession( DataTestFactory.getConfigVirtuoso1())
      assert(UnravelSession().setSerializedString(sw.getSerializedString) == sw)
      val swt : UnravelQuery = sw.select()
      assert(UnravelQuery().setSerializedString(swt.getSerializedString) == swt)
    }

    test("serialization Something") {
      val sw =
        UnravelSession( DataTestFactory.getConfigVirtuoso1())
          .something ("h1" )

      assert(UnravelSession().setSerializedString(sw.getSerializedString) == sw)
      val swt : UnravelQuery = sw.select()
      assert(UnravelQuery().setSerializedString(swt.getSerializedString) == swt)
    }

    test("serialization SubjectOf") {
      val sw =
        UnravelSession( DataTestFactory.getConfigVirtuoso1())
          .something ("h1" )
          .isSubjectOf(URI("bb"))

      assert(UnravelSession().setSerializedString(sw.getSerializedString) == sw)
      val swt : UnravelQuery = sw.select()
      assert(UnravelQuery().setSerializedString(swt.getSerializedString) == swt)
    }

    test("serialization ObjectOf") {
      val sw =
        UnravelSession( DataTestFactory.getConfigVirtuoso1())
          .something ("h1" )
          .isObjectOf(URI("bb"))

      assert(UnravelSession().setSerializedString(sw.getSerializedString) == sw)
      val swt : UnravelQuery = sw.select()
      assert(UnravelQuery().setSerializedString(swt.getSerializedString) == swt)
    }

    test("serialization LinkTo") {
      val sw =
        UnravelSession( DataTestFactory.getConfigVirtuoso1())
          .something ("h1" )
          .isLinkTo(URI("bb"))

      assert(UnravelSession().setSerializedString(sw.getSerializedString) == sw)
      val swt : UnravelQuery = sw.select()
      assert(UnravelQuery().setSerializedString(swt.getSerializedString) == swt)
    }

    test("serialization LinkFrom") {
      val sw =
        UnravelSession( DataTestFactory.getConfigVirtuoso1())
          .something ("h1" )
          .isLinkFrom(QueryVariable("bb"))

      assert(UnravelSession().setSerializedString(sw.getSerializedString) == sw)
      val swt : UnravelQuery = sw.select()
      assert(UnravelQuery().setSerializedString(swt.getSerializedString) == swt)
    }

    test("serialization Value QueryVariable") {
      val sw =
        UnravelSession( DataTestFactory.getConfigVirtuoso1())
          .something ("h1" )
          .set(QueryVariable("bb"))

      assert(UnravelSession().setSerializedString(sw.getSerializedString) == sw)
      val swt : UnravelQuery = sw.select()
      assert(UnravelQuery().setSerializedString(swt.getSerializedString) == swt)
    }

    test("serialization Value URI") {
      val sw =
        UnravelSession( DataTestFactory.getConfigVirtuoso1())
          .something ("h1" )
          .set(URI("bb"))

      assert(UnravelSession().setSerializedString(sw.getSerializedString) == sw)
      val swt : UnravelQuery = sw.select()
      assert(UnravelQuery().setSerializedString(swt.getSerializedString) == swt)
    }

    test("serialization Literal URI") {
      val sw =
        UnravelSession( DataTestFactory.getConfigVirtuoso1())
          .something ("h1" )
          .set(Literal("bb"))

      assert(UnravelSession().setSerializedString(sw.getSerializedString) == sw)
      val swt : UnravelQuery = sw.select()
      assert(UnravelQuery().setSerializedString(swt.getSerializedString) == swt)
    }

    test("serialization ListValue URI") {
      val sw =
        UnravelSession( DataTestFactory.getConfigVirtuoso1())
          .something ("h1" )
          .setList(Seq(URI("bb")))

      assert(UnravelSession().setSerializedString(sw.getSerializedString) == sw)
      val swt : UnravelQuery = sw.select()
      assert(UnravelQuery().setSerializedString(swt.getSerializedString) == swt)
    }

    test("serialization graph") {
      val sw =
        UnravelSession( DataTestFactory.getConfigVirtuoso1())
          .graph("h")
          .something ("h1" )
          .setList(Seq(URI("bb")))

      assert(UnravelSession().setSerializedString(sw.getSerializedString) == sw)
      val swt : UnravelQuery = sw.select()
      assert(UnravelQuery().setSerializedString(swt.getSerializedString) == swt)
    }

    test("serialization filter") {
      val sw =
        UnravelSession( DataTestFactory.getConfigVirtuoso1())
          .something ("h1" )
          .isLinkTo(URI("bb"))
          .filter.not.contains("filter")

      assert(UnravelSession().setSerializedString(sw.getSerializedString) == sw)
      val swt : UnravelQuery = sw.select()
      assert(UnravelQuery().setSerializedString(swt.getSerializedString) == swt)
    }

    test("serialization datatype") {
      val sw =
        UnravelSession(DataTestFactory.getConfigVirtuoso1())
          .something ("h1" )
          .isLinkTo(URI("bb"))
          .datatype(URI("some"),"v")

      assert(UnravelSession().setSerializedString(sw.getSerializedString) == sw)
      val swt : UnravelQuery = sw.select()
      assert(UnravelQuery().setSerializedString(swt.getSerializedString) == swt)
    }
  }
}
