package fr.inrae.metabohub.semantic_web.node.pm

import fr.inrae.metabohub.semantic_web.node._

object QueryPrettyPrinter {

  def renderChildren(children: Seq[Node]): String = {
    val header =
      Seq(
        "CHILDREN",
        s"Count: ${children.size}"
      )

    val body =
      children.zipWithIndex.flatMap { case (child, idx) =>
        renderNode(child, prefix = "", isLast = idx == children.size - 1)
      }

    (header ++ body).mkString("\n")
  }

  def renderNode(node: Node): String =
    renderNode(node, prefix = "", isLast = true).mkString("\n")

  private def renderNode(node: Node, prefix: String, isLast: Boolean): Seq[String] = {
    val branch =
      if (prefix.isEmpty) ""
      else if (isLast) "└─ "
      else "├─ "

    val line = prefix + branch + label(node)

    val nextPrefix =
      if (prefix.isEmpty) ""
      else prefix + (if (isLast) "   " else "│  ")

    val childLines =
      node.children.zipWithIndex.flatMap { case (child, idx) =>
        renderNode(child, nextPrefix, idx == node.children.size - 1)
      }

    line +: childLines
  }

  private def label(node: Node): String = node match {

    case n: Root =>
      s"Root@${shortRef(n.idRef)}"

    case n: Something =>
      s"Something@${shortRef(n.idRef)}"

    case n: SomethingVar =>
      s"SomethingVar@${shortRef(n.idRef)}"

    case n: SubjectOf =>
      s"SubjectOf@${shortRef(n.idRef)} --${term(n.propertyTerm)}--> ${term(n.objectTerm)}"

    case n: ObjectOf =>
      s"ObjectOf@${shortRef(n.idRef)} <--${term(n.propertyTerm)}-- ${term(n.subjectTerm)}"

    case n: Value =>
      s"Value ${term(n.term)}"

    case n: ListValues =>
      s"ListValues(${n.terms.size}) ${n.terms.map(term).mkString(", ")}"

    case n: UnionBlock =>
      s"UnionBlock@${shortRef(n.idRef)}"

    case n: NotBlock =>
      s"NotBlock@${shortRef(n.idRef)}"

    case n: DatatypeNode =>
      s"DatatypeNode@${shortRef(n.idRef)} ref=${n.refNode} property=${label(n.property)}"

    case n: SourcesNode =>
      s"SourcesNode@${shortRef(n.idRef)} ref=${n.refNode} sources=${n.sources.mkString(", ")}"

    case n: Bind =>
      s"Bind@${shortRef(n.idRef)} expr=${expressionLabel(n.expression)}"

    case n: ProjectionExpression =>
      s"ProjectionExpression@${shortRef(n.idRef)} var=${term(n.`var`)} expr=${aggregateLabel(n.expression)}"

    case n: Count =>
      s"Count@${shortRef(n.idRef)} distinct=${n.distinct} vars=${n.listVarToCount.map(term).mkString(", ")}"

    case n: OrderByAsc =>
      s"OrderByAsc@${shortRef(n.idRef)} ${n.list.map(term).mkString(", ")}"

    case n: OrderByDesc =>
      s"OrderByDesc@${shortRef(n.idRef)} ${n.list.map(term).mkString(", ")}"

    case n: Projection =>
      s"Projection@${shortRef(n.idRef)} ${n.variables.map(term).mkString(", ")}"

    case n: Distinct =>
      s"Distinct@${shortRef(n.idRef)}"

    case n: Reduced =>
      s"Reduced@${shortRef(n.idRef)}"

    case n: Offset =>
      s"Offset@${shortRef(n.idRef)} ${n.value}"

    case n: Limit =>
      s"Limit@${shortRef(n.idRef)} ${n.value}"

    case n: isBlank =>
      filterLabel("isBlank", n.negation, n.idRef)

    case n: isLiteral =>
      filterLabel("isLiteral", n.negation, n.idRef)

    case n: isURI =>
      filterLabel("isURI", n.negation, n.idRef)

    case n: Regex =>
      s"Regex@${shortRef(n.idRef)} neg=${n.negation} pattern=${term(n.pattern)} flags=${term(n.flags)}"

    case n: Contains =>
      s"Contains@${shortRef(n.idRef)} neg=${n.negation} value=${term(n.value)}"

    case n: StrStarts =>
      s"StrStarts@${shortRef(n.idRef)} neg=${n.negation} value=${term(n.value)}"

    case n: StrEnds =>
      s"StrEnds@${shortRef(n.idRef)} neg=${n.negation} value=${term(n.value)}"

    case n: Equal =>
      s"Equal@${shortRef(n.idRef)} neg=${n.negation} value=${term(n.value)}"

    case n: NotEqual =>
      s"NotEqual@${shortRef(n.idRef)} neg=${n.negation} value=${term(n.value)}"

    case n: Inf =>
      s"Inf@${shortRef(n.idRef)} neg=${n.negation} value=${term(n.value)}"

    case n: InfEqual =>
      s"InfEqual@${shortRef(n.idRef)} neg=${n.negation} value=${term(n.value)}"

    case n: Sup =>
      s"Sup@${shortRef(n.idRef)} neg=${n.negation} value=${term(n.value)}"

    case n: SupEqual =>
      s"SupEqual@${shortRef(n.idRef)} neg=${n.negation} value=${term(n.value)}"

    case n: SparqlDefinitionExpression =>
      s"SparqlDefinitionExpression@${shortRef(n.idRef)} ${term(n.sd)}"

    case n: SubStr =>
      s"SubStr@${shortRef(n.idRef)} start=${term(n.start)} length=${term(n.length)}"

    case n: Replace =>
      s"Replace@${shortRef(n.idRef)} pattern=${term(n.pattern)} replacement=${term(n.replacement)} flags=${term(n.flags)}"

    case n: Abs =>
      s"Abs@${shortRef(n.idRef)}"

    case n: Round =>
      s"Round@${shortRef(n.idRef)}"

    case n: Ceil =>
      s"Ceil@${shortRef(n.idRef)}"

    case n: Floor =>
      s"Floor@${shortRef(n.idRef)}"

    case n: Rand =>
      s"Rand@${shortRef(n.idRef)}"

    case n: Datatype =>
      s"Datatype@${shortRef(n.idRef)}"

    case n: Str =>
      s"Str@${shortRef(n.idRef)} term=${term(n.term)}"

    case n: StrDt =>
      s"StrDt@${shortRef(n.idRef)} term=${term(n.term)} datatype=${term(n.datatype)}"

    case n: Lang =>
      s"Lang@${shortRef(n.idRef)} term=${term(n.term)}"

    case n: LangMatches =>
      s"LangMatches@${shortRef(n.idRef)} term=${term(n.term)}"

    case other =>
      s"${other.getClass.getSimpleName}@${shortRef(other.idRef)}"
  }

  private def filterLabel(name: String, negation: Boolean, idRef: String): String =
    s"$name@${shortRef(idRef)} neg=$negation"

  private def aggregateLabel(node: AggregateNode): String = node match {
    case n: Count =>
      s"Count(distinct=${n.distinct}, vars=[${n.listVarToCount.map(term).mkString(", ")}])"
    case other =>
      other.getClass.getSimpleName
  }

  private def expressionLabel(node: ExpressionNode): String = node match {
    case n: SparqlDefinitionExpression => term(n.sd)
    case other => other.getClass.getSimpleName
  }

  private def term(v: Any): String =
    Option(v).map(_.toString).getOrElse("null")

  private def shortRef(v: String): String =
    if (v == null || v.isEmpty) "?" else v
}