// SPDX-FileCopyrightText: 2020-2026 INRAE
// SPDX-License-Identifier: GPL-3.0-or-later

package fr.inrae.metabohub.semantic_web.node.pm

import fr.inrae.metabohub.semantic_web.node._
import fr.inrae.metabohub.semantic_web.rdf.{IRI, Var}
import wvlet.log.Logger.rootLogger._

final case class SparqlGeneratorException(
                                           private val message: String = "",
                                           private val cause: Throwable = None.orNull
                                         ) extends Exception(message, cause)

/**
 * Serializes the project query AST into deterministic, readable SPARQL 1.1.
 *
 * Formatting is deliberately centralized: each graph-pattern item ends with
 * exactly one line break, which prevents adjacent patterns from being emitted
 * as a single invalid or unreadable line.
 */
object SparqlGenerator {

  private val NL = "\n"

  private def lines(parts: Iterable[String]): String =
    parts.iterator.map(_.trim).filter(_.nonEmpty).mkString(NL)

  private def variable(name: String): String =
    if (name.startsWith("?") || name.startsWith("$")) name else s"?$name"

  private def term(value: Any): String = value match {
    case value: IRI => value.sparql
    case value: Var => value.sparql
    case value     => value.toString
  }

  /** Stable order is important for snapshots, query caches and debugging. */
  def prefixes(prefixes: Map[String, IRI]): String =
    prefixes.toSeq
      .sortBy(_._1)
      .map { case (prefix, iri) => s"PREFIX $prefix: ${iri.sparql}" }
      .mkString(NL)

  def from(graphs: Seq[IRI]): String =
    graphs.map(graph => s"FROM ${graph.sparql}").mkString(NL)

  def fromNamed(graphs: Seq[IRI]): String =
    graphs.map(graph => s"FROM NAMED ${graph.sparql}").mkString(NL)

  def solutionSequenceModifierStart(root: Root): String = {
    val selectModifier =
      root.lSolutionSequenceModifierNode.lastOption match {
        case Some(_: Distinct) => "DISTINCT "
        case Some(_: Reduced)  => "REDUCED "
        case _                 => ""
      }

    /*
     * Do not filter projection variables here.
     *
     * A projected variable can come from:
     * - an RDF graph variable, for example ?h1;
     * - a variable stored inside a traversal node, for example ?property;
     * - an expression alias, for example ?count.
     *
     * NodeVisitor.getAllAncestorsRef(root) is not a complete variable-scope
     * analysis, so it must not remove projection variables.
     */
    val projection =
      root.lSolutionSequenceModifierNode
        .collect { case proj: Projection => proj }
        .lastOption

    val projectionSparql =
      projection
        .map { proj =>
          val children =
            proj.children
              .map(child => sparqlNode(child, "", ""))
              .filter(_.nonEmpty)

          (Seq(sparqlNode(proj, "", "")) ++ children)
            .filter(_.nonEmpty)
            .mkString(" ")
        }
        .getOrElse("*")

    val datasetClauses =
      Seq(
        from(root.defaultGraph),
        fromNamed(root.namedGraph)
      ).filter(_.nonEmpty)

    (Seq(s"SELECT $selectModifier$projectionSparql") ++ datasetClauses)
      .mkString("\n") + "\nWHERE {"
  }

  def solutionSequenceModifierEnd(root: Root): String = {
    val orderAsc = root.lSolutionSequenceModifierNode.collect {
      case node: OrderByAsc if node.list.nonEmpty => node.list.mkString(" ")
    }

    val orderDesc = root.lSolutionSequenceModifierNode.collect {
      case node: OrderByDesc if node.list.nonEmpty =>
        node.list.map(value => s"DESC($value)").mkString(" ")
    }

    val orderBy =
      (orderAsc ++ orderDesc).lastOption.map(values => s"ORDER BY $values")

    val limit = root.lSolutionSequenceModifierNode.collect {
      case node: Limit if node.value > 0 => s"LIMIT ${node.value}"
    }.lastOption

    val offset = root.lSolutionSequenceModifierNode.collect {
      case node: Offset if node.value > 0 => s"OFFSET ${node.value}"
    }.lastOption

    lines(Seq("}") ++ orderBy ++ limit ++ offset)
  }

  def prologCountSelection(varCount: String): String =
    s"SELECT (COUNT(*) AS ${variable(varCount)})"

  def sparqlNode(
                  n: Node,
                  varIdSire: String,
                  variableName: String
                ): String = {
    trace(s"$varIdSire - $variableName")

    n match {
      // Triple patterns.
      case node: SubjectOf =>
        s"${variable(varIdSire)} ${node.propertyTerm.sparql} ${node.objectTerm.sparql} ."

      case node: ObjectOf =>
        s"${node.subjectTerm.sparql} ${node.propertyTerm.sparql} ${variable(varIdSire)} ."

        // Values and bindings.
      case node: Value =>
        node.term match {
          case _: Var =>
            s"BIND(${variable(varIdSire)} AS ${node.term.sparql})"
          case _ =>
            s"VALUES ${variable(varIdSire)} { ${node.term.sparql} }"
        }

      case node: ListValues =>
        s"VALUES ${variable(varIdSire)} { ${node.terms.map(_.sparql).mkString(" ")} }"

      case node: ProjectionExpression =>
        s"(${sparqlNode(node.expression, node.idRef, variableName)} AS ${node.`var`})"

      case node: Bind =>
        s"BIND(${sparqlNode(node.expression, varIdSire, variableName)} AS ${variable(node.idRef)})"

      case node: Count =>
        val distinct = if (node.distinct) "DISTINCT " else ""
        val expression = node.listVarToCount
          .map(value => s"STR(${value.sparql})")
          .mkString("CONCAT(", ", ", ")")
        s"COUNT($distinct$expression)"

        // SELECT modifiers.
      case _: Distinct => "DISTINCT"
      case _: Reduced  => "REDUCED"

      case node: Projection if node.variables.nonEmpty =>
        node.variables.mkString(" ")

      case _: Projection =>
        ""

      case node: Limit =>
        s"LIMIT ${node.value}"

      case node: Offset =>
        s"OFFSET ${node.value}"

      case node: OrderByAsc =>
        node.list.mkString(" ")

      case node: OrderByDesc =>
        node.list.map(value => s"DESC($value)").mkString(" ")

        // Expressions.
      case node: SubStr =>
        s"SUBSTR(${variable(varIdSire)}, ${node.start}, ${node.length})"

      case node: Replace =>
        s"REPLACE(${variable(varIdSire)}, ${node.pattern.sparql}, " +
          s"${node.replacement.sparql}, ${node.flags.sparql})"

      case _: Abs   => s"ABS(${variable(varIdSire)})"
      case _: Round => s"ROUND(${variable(varIdSire)})"
      case _: Floor => s"FLOOR(${variable(varIdSire)})"
      case _: Ceil  => s"CEIL(${variable(varIdSire)})"
      case _: Rand  => "RAND()"

      case _: Datatype =>
        s"DATATYPE(${variable(varIdSire)})"

      case _: Str if varIdSire.nonEmpty =>
        s"STR(${variable(varIdSire)})"

        /*
         * STRDT requires a lexical string as first argument.
         * This explicitly canonicalizes ?raw to an xsd:string literal:
         * STRDT(STR(?raw), xsd:string)
         */
      case node: StrDt if varIdSire.nonEmpty =>
        s"STRDT(STR(${variable(varIdSire)}), ${term(node.datatype)})"

      case node: Str =>
        s"STR(${node.term.sparql})"

      case node: Lang =>
        s"LANG(${node.term.sparql})"

        /*
         * Kept compatible with the current AST. Prefer later replacing this node
         * by LangMatches(valueExpression, languageRangeExpression).
         */
      case node: LangMatches =>
        s"LANGMATCHES(LANG(${node.term.sparql}), ${node.term.sparql})"

        // Filters.
      case node: FilterNode =>
        val negation = if (node.negation) "!" else ""

        val condition = node match {
          case regex: Regex =>
            s"REGEX(STR(${variable(varIdSire)}), ${regex.pattern.sparql}, ${regex.flags.sparql})"

          case contains: Contains =>
            s"CONTAINS(STR(${variable(varIdSire)}), STR(${contains.value.sparql}))"

          case starts: StrStarts =>
            s"STRSTARTS(STR(${variable(varIdSire)}), STR(${starts.value.sparql}))"

          case ends: StrEnds =>
            s"STRENDS(STR(${variable(varIdSire)}), STR(${ends.value.sparql}))"

          case equal: Equal =>
            s"(${variable(varIdSire)} = ${equal.value.sparql})"

          case notEqual: NotEqual =>
            s"(${variable(varIdSire)} != ${notEqual.value.sparql})"

          case inf: Inf =>
            s"(${variable(varIdSire)} < ${inf.value.sparql})"

          case infEqual: InfEqual =>
            s"(${variable(varIdSire)} <= ${infEqual.value.sparql})"

          case sup: Sup =>
            s"(${variable(varIdSire)} > ${sup.value.sparql})"

          case supEqual: SupEqual =>
            s"(${variable(varIdSire)} >= ${supEqual.value.sparql})"

          case _: isBlank =>
            s"ISBLANK(${variable(varIdSire)})"

          case _: isURI =>
            s"ISURI(${variable(varIdSire)})"

          case _: isLiteral =>
            s"ISLITERAL(${variable(varIdSire)})"

          case unsupported =>
            throw SparqlGeneratorException(
              s"Unsupported FILTER node: ${unsupported.getClass.getName}"
            )
        }

        s"FILTER ($negation$condition)"

        // Structural nodes.
      case _: Root        => ""
      case _: SomethingVar => ""
      case _: Something if n.children.nonEmpty => ""

      case _: Something =>
        val value = variable(variableName)
        s"""{
           |  { $value ?property_$variableName ?object_$variableName . }
           |  UNION
           |  { [] $value [] . }
           |  UNION
           |  { ?subject_$variableName ?property_$variableName $value . }
           |}""".stripMargin

      case _: UnionBlock                => ""
      case _: NotBlock                  =>
        throw SparqlGeneratorException("NotBlock serialization is not implemented")
      case _: DatatypeNode              => ""
      case _:SourcesNode                => ""
      case _: SparqlDefinitionExpression =>
        throw SparqlGeneratorException(
          "SparqlDefinitionExpression serialization is not implemented"
        )

      case unsupported =>
        throw SparqlGeneratorException(
          s"Unsupported SPARQL node: ${unsupported.getClass.getName}"
        )
    }
  }

  /**
   * Emits one graph-pattern item per line. This is the key change for the
   * missing-newline issue: a parent never directly concatenates child output.
   */
  def body(
            n: Node,
            varIdSire: String = ""
          ): String = {
    val variableName = n.idRef

    n match {
      case union: UnionBlock if union.children.nonEmpty =>
        union.children
          .map { child =>
            s"{\n${indent(body(child, varIdSire))}\n}"
          }
          .mkString("{\n", "\nUNION\n", "\n}")

      case node: Node =>
        lines(
          Seq(sparqlNode(node, varIdSire, variableName)) ++
            node.children.map { child =>
              body(child, variableName)
            }
        )
    }
  }

  private def indent(value: String, prefix: String = "  "): String =
    value.linesIterator.map(prefix + _).mkString(NL)
}