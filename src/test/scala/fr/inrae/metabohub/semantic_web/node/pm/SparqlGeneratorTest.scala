// SPDX-FileCopyrightText: 2020-2026 INRAE
// SPDX-License-Identifier: GPL-3.0-or-later

package fr.inrae.metabohub.semantic_web.node.pm

import fr.inrae.metabohub.data.ApplyAllNode
import fr.inrae.metabohub.semantic_web.node._
import fr.inrae.metabohub.semantic_web.rdf._
import utest.{TestSuite, Tests, assert, test}

object SparqlGeneratorTest extends TestSuite {

  /*
   * Normalize line endings so golden tests behave identically on Linux,
   * macOS, and Windows.
   *
   * Internal spaces are intentionally not normalized: they are part of the
   * deterministic formatting contract of SparqlGenerator.
   */
  private def normalize(value: String): String =
    value.replace("\r\n", "\n").replace("\r", "\n").trim

  private def assertSparql(actual: String, expected: String): Unit =
    assert(normalize(actual) == normalize(expected))

  private def render(
                      node: Node,
                      sire: String = "nothingSire",
                      name: String = "nothingVar"
                    ): String =
    SparqlGenerator.sparqlNode(node, sire, name)

  def tests: Tests = Tests {

    test("prefixes are deterministic and formatted") {
      val prefixes: Map[String, IRI] = Map(
        "some2" -> "http://something2",
        "some"  -> "http://something"
      )

      assertSparql(
        SparqlGenerator.prefixes(prefixes),
        """
          |PREFIX some: <http://something>
          |PREFIX some2: <http://something2>
          |""".stripMargin
      )
    }

    test("from emits one clause per line") {
      val graphs: Seq[IRI] = Seq(
        "http://something",
        "http://something2"
      )

      assertSparql(
        SparqlGenerator.from(graphs),
        """
          |FROM <http://something>
          |FROM <http://something2>
          |""".stripMargin
      )
    }

    test("fromNamed emits one clause per line") {
      val graphs: Seq[IRI] = Seq(
        "http://something",
        "http://something2"
      )

      assertSparql(
        SparqlGenerator.fromNamed(graphs),
        """
          |FROM NAMED <http://something>
          |FROM NAMED <http://something2>
          |""".stripMargin
      )
    }

    test("prologCountSelection renders a COUNT projection") {
      assertSparql(
        SparqlGenerator.prologCountSelection("myvar"),
        "SELECT (COUNT(*) AS ?myvar)"
      )
    }

    test("SubjectOf renders a triple pattern") {
      assertSparql(
        render(
          SubjectOf("varId", URI("http://test"), Var("varId")),
          sire = "varSire",
          name = "varId"
        ),
        "?varSire <http://test> ?varId ."
      )
    }

    test("ObjectOf renders a triple pattern") {
      assertSparql(
        render(ObjectOf("1234", URI("test"), Var("nothingVar"))),
        "?nothingVar <test> ?nothingSire ."
      )
    }

    test("SubjectOf supports a variable predicate") {
      assertSparql(
        render(SubjectOf("1234", Var("nothingVar"), URI("test"))),
        "?nothingSire ?nothingVar <test> ."
      )
    }

    test("ObjectOf supports a variable predicate") {
      assertSparql(
        render(ObjectOf("1234", Var("nothingVar"), URI("test"))),
        "<test> ?nothingVar ?nothingSire ."
      )
    }

    test("Value with a constant renders VALUES") {
      assertSparql(
        render(Value(URI("test"))),
        "VALUES ?nothingSire { <test> }"
      )
    }

    test("ListValues renders all values") {
      assertSparql(
        render(ListValues(List(URI("test"), URI("test2")))),
        "VALUES ?nothingSire { <test> <test2> }"
      )
    }

    test("Something without children renders a fallback graph pattern") {
      assertSparql(
        render(Something("1234")),
        """
          |{
          |  { ?nothingVar ?property_nothingVar ?object_nothingVar . }
          |  UNION
          |  { [] ?nothingVar [] . }
          |  UNION
          |  { ?subject_nothingVar ?property_nothingVar ?nothingVar . }
          |}
          |""".stripMargin
      )
    }

    test("Something with children does not render a fallback graph pattern") {
      val node = Something(
        "1234",
        List(SubjectOf("test", URI("http://test"), Var("nothingVar")))
      )

      assert(render(node).isEmpty)
    }

    test("isBlank filter") {
      assertSparql(
        render(isBlank(negation = false, "")),
        "FILTER (ISBLANK(?nothingSire))"
      )
    }

    test("negated isBlank filter") {
      assertSparql(
        render(isBlank(negation = true, "")),
        "FILTER (!ISBLANK(?nothingSire))"
      )
    }

    test("isLiteral filter") {
      assertSparql(
        render(isLiteral(negation = false, "")),
        "FILTER (ISLITERAL(?nothingSire))"
      )
    }

    test("negated isLiteral filter") {
      assertSparql(
        render(isLiteral(negation = true, "")),
        "FILTER (!ISLITERAL(?nothingSire))"
      )
    }

    test("isURI filter") {
      assertSparql(
        render(isURI(negation = false, "")),
        "FILTER (ISURI(?nothingSire))"
      )
    }

    test("negated isURI filter") {
      assertSparql(
        render(isURI(negation = true, "")),
        "FILTER (!ISURI(?nothingSire))"
      )
    }

    test("Contains filter") {
      assertSparql(
        render(Contains("h", negation = false, "")),
        """FILTER (CONTAINS(STR(?nothingSire), STR("h")))"""
      )
    }

    test("negated Contains filter") {
      assertSparql(
        render(Contains("h", negation = true, "")),
        """FILTER (!CONTAINS(STR(?nothingSire), STR("h")))"""
      )
    }

    test("StrStarts filter") {
      assertSparql(
        render(StrStarts("h", negation = false, "")),
        """FILTER (STRSTARTS(STR(?nothingSire), STR("h")))"""
      )
    }

    test("StrEnds filter") {
      assertSparql(
        render(StrEnds("h", negation = false, "")),
        """FILTER (STRENDS(STR(?nothingSire), STR("h")))"""
      )
    }

    test("Equal filter") {
      assertSparql(
        render(Equal("h", negation = false, "")),
        """FILTER ((?nothingSire = "h"))"""
      )
    }

    test("negated Equal filter") {
      assertSparql(
        render(Equal("h", negation = true, "")),
        """FILTER (!(?nothingSire = "h"))"""
      )
    }

    test("NotEqual filter") {
      assertSparql(
        render(NotEqual("h", negation = false, "")),
        """FILTER ((?nothingSire != "h"))"""
      )
    }

    test("numeric comparisons render explicit operators") {
      assertSparql(
        render(Inf(0.5, negation = false, "")),
        "FILTER ((?nothingSire < 0.5))"
      )

      assertSparql(
        render(InfEqual(0.5, negation = false, "")),
        "FILTER ((?nothingSire <= 0.5))"
      )

      assertSparql(
        render(Sup(0.5, negation = false, "")),
        "FILTER ((?nothingSire > 0.5))"
      )

      assertSparql(
        render(SupEqual(0.5, negation = false, "")),
        "FILTER ((?nothingSire >= 0.5))"
      )
    }

    test("typed literal comparison") {
      assertSparql(
        render(
          Inf(Literal("0.5", "xsd:double"), negation = false, "")
        ),
        """FILTER ((?nothingSire < "0.5"^^xsd:double))"""
      )
    }

    test("negated typed literal comparison") {
      assertSparql(
        render(
          InfEqual(
            Literal("0.5", "xsd:double"),
            negation = true,
            ""
          )
        ),
        """FILTER (!(?nothingSire <= "0.5"^^xsd:double))"""
      )
    }

    test("Datatype expression") {
      assertSparql(
        render(Datatype("")),
        "DATATYPE(?nothingSire)"
      )
    }

    test("Str expression") {
      assertSparql(
        render(Str(URI("test"), "")),
        "STR(?nothingSire)"
      )
    }

    /*
     * Regression test for InChIKey canonicalization.
     *
     * Important: the first argument of STRDT must be a lexical string.
     * Therefore the generated expression must be:
     *
     * STRDT(STR(?rawInchiKey), xsd:string)
     *
     * and not:
     *
     * STRDT(?rawInchiKey, xsd:string)
     */
    test("StrDt canonicalizes a raw value as xsd:string") {
      val actual = SparqlGenerator.sparqlNode(
        StrDt(
          term = Var("rawInchiKey"),
          idRef = "",
          datatype = URI("string", "xsd"),
          children = Seq.empty,
          decorations = Map.empty
        ),
        varIdSire = "rawInchiKey",
        variableName = "inchiKey"
      )

      assertSparql(
        actual,
        "STRDT(STR(?rawInchiKey), xsd:string)"
      )
    }

    /*
     * This is the direct regression test for the missing-newline problem.
     * It verifies that a parent triple and its child triple are always
     * serialized on separate lines.
     */
    test("body emits one graph pattern per line") {
      val node = SubjectOf(
        "record",
        URI("http://example.org/predicate"),
        Var("object"),
        children = List(
          SubjectOf(
            "object",
            URI("http://example.org/nextPredicate"),
            Literal("value")
          )
        )
      )

      assertSparql(
        SparqlGenerator.body(node, "record"),
        """
          |?record <http://example.org/predicate> ?object .
          |?record <http://example.org/nextPredicate> "value" .
          |""".stripMargin
      )
    }

    /*
     * Smoke test only: it detects a newly-added AST node for which
     * SparqlGenerator has no rendering implementation.
     *
     * Exact golden tests above remain the primary correctness tests.
     */
    test("all currently supported node examples serialize") {
      ApplyAllNode.listNodes
        .filterNot {
          case _: NotBlock => true
          case _: SparqlDefinitionExpression => true
          case _ => false
        }
        .foreach { node =>
          SparqlGenerator.sparqlNode(node, "nothingSire", "nothingVar")
        }

      assert(true)
    }

    test("projection keeps a traversal variable") {
      val projection = Projection(
        variables = Seq(Var("var")),
        idRef = "",
        children = Seq.empty,
        decorations = Map.empty
      )

      assertSparql(
        SparqlGenerator.sparqlNode(projection, "", ""),
        "?var"
      )
    }
  }
}