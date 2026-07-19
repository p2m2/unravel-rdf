package fr.inrae.metabohub.semantic_web.node.pm

import fr.inrae.metabohub.semantic_web.node._
import org.scalajs.dom
import org.scalajs.dom.document
import org.scalajs.dom.html

object QueryTreeHtmlRenderer {

  def renderChildren(children: Seq[Node], focusRef: Option[String] = None): html.Element = {
    val container = document.createElement("div").asInstanceOf[html.Div]
    container.className = "unravel-tree-panel"

    val meta = document.createElement("div").asInstanceOf[html.Div]
    meta.className = "unravel-tree-meta"
    meta.textContent = s"Children: ${children.size}"
    container.appendChild(meta)

    val rootList = document.createElement("ul").asInstanceOf[html.UList]
    rootList.className = "unravel-tree"

    children.foreach { child =>
      rootList.appendChild(renderNode(child, focusRef))
    }

    container.appendChild(rootList)
    container
  }

  def renderNode(node: Node, focusRef: Option[String] = None): html.Element = {
    val li = document.createElement("li").asInstanceOf[html.LI]
    li.className = "unravel-tree-item"

    val hasChildren = node.children.nonEmpty
    val isFocus = focusRef.exists(_ == safeRef(node.idRef))

    if (hasChildren) {
      val details = document.createElement("details").asInstanceOf[html.Element]
      details.setAttribute("open", "true")
      details.className = "unravel-tree-details"

      val summary = document.createElement("summary").asInstanceOf[html.Element]
      summary.className = if (isFocus) "unravel-tree-summary is-focus" else "unravel-tree-summary"
      summary.appendChild(nodeHeader(node, isFocus))

      val ul = document.createElement("ul").asInstanceOf[html.UList]
      ul.className = "unravel-tree-children"

      node.children.foreach { child =>
        ul.appendChild(renderNode(child, focusRef))
      }

      details.appendChild(summary)
      details.appendChild(ul)
      li.appendChild(details)
    } else {
      val row = document.createElement("div").asInstanceOf[html.Div]
      row.className = if (isFocus) "unravel-tree-row is-focus" else "unravel-tree-row"
      row.appendChild(nodeHeader(node, isFocus))
      li.appendChild(row)
    }

    li
  }

  private def nodeHeader(node: Node, isFocus: Boolean): html.Element = {
    val row = document.createElement("div").asInstanceOf[html.Div]
    row.className = "unravel-node"

    val typeBadge = badge(nodeKind(node), s"kind kind-${kindClass(node)}")
    val refBadge = badge("@" + safeRef(node.idRef), if (isFocus) "ref is-focus-ref" else "ref")
    val detailSpan = document.createElement("span").asInstanceOf[html.Span]
    detailSpan.className = "node-detail"
    detailSpan.textContent = nodeDetail(node)

    row.appendChild(typeBadge)
    row.appendChild(refBadge)

    if (detailSpan.textContent.trim.nonEmpty) {
      row.appendChild(detailSpan)
    }

    row
  }

  private def badge(text: String, cls: String): html.Span = {
    val el = document.createElement("span").asInstanceOf[html.Span]
    el.className = s"badge $cls"
    el.textContent = text
    el
  }

  private def nodeKind(node: Node): String = node match {
    case _: Root => "Root"
    case _: Something => "Something"
    case _: SomethingVar => "SomethingVar"
    case _: SubjectOf => "SubjectOf"
    case _: ObjectOf => "ObjectOf"
    case _: Value => "Value"
    case _: ListValues => "ListValues"
    case _: UnionBlock => "UnionBlock"
    case _: NotBlock => "NotBlock"
    case _: DatatypeNode => "DatatypeNode"
    case _: SourcesNode => "SourcesNode"
    case _: Bind => "Bind"
    case _: ProjectionExpression => "ProjectionExpression"
    case _: Count => "Count"
    case _: OrderByAsc => "OrderByAsc"
    case _: OrderByDesc => "OrderByDesc"
    case _: Projection => "Projection"
    case _: Distinct => "Distinct"
    case _: Reduced => "Reduced"
    case _: Offset => "Offset"
    case _: Limit => "Limit"
    case _: isBlank => "isBlank"
    case _: isLiteral => "isLiteral"
    case _: isURI => "isURI"
    case _: Regex => "Regex"
    case _: Contains => "Contains"
    case _: StrStarts => "StrStarts"
    case _: StrEnds => "StrEnds"
    case _: Equal => "Equal"
    case _: NotEqual => "NotEqual"
    case _: Inf => "Inf"
    case _: InfEqual => "InfEqual"
    case _: Sup => "Sup"
    case _: SupEqual => "SupEqual"
    case _: SparqlDefinitionExpression => "SparqlDefinitionExpression"
    case _: SubStr => "SubStr"
    case _: Replace => "Replace"
    case _: Abs => "Abs"
    case _: Round => "Round"
    case _: Ceil => "Ceil"
    case _: Floor => "Floor"
    case _: Rand => "Rand"
    case _: Datatype => "Datatype"
    case _: Str => "Str"
    case _: StrDt => "StrDt"
    case _: Lang => "Lang"
    case _: LangMatches => "LangMatches"
    case other => other.getClass.getSimpleName
  }

  private def kindClass(node: Node): String =
    nodeKind(node).replaceAll("[^A-Za-z0-9]+", "-").toLowerCase

  private def nodeDetail(node: Node): String = node match {
    case n: SubjectOf =>
      s"${term(n.propertyTerm)} → ${term(n.objectTerm)}"
    case n: ObjectOf =>
      s"${term(n.propertyTerm)} ← ${term(n.subjectTerm)}"
    case n: Bind =>
      s"expr=${expressionLabel(n.expression)}"
    case n: ProjectionExpression =>
      s"var=${term(n.`var`)} expr=${aggregateLabel(n.expression)}"
    case n: Count =>
      s"distinct=${n.distinct} vars=[${n.listVarToCount.map(term).mkString(", ")}]"
    case n: Projection =>
      s"[${n.variables.map(term).mkString(", ")}]"
    case n: OrderByAsc =>
      s"[${n.list.map(term).mkString(", ")}]"
    case n: OrderByDesc =>
      s"[${n.list.map(term).mkString(", ")}]"
    case n: Offset =>
      s"value=${n.value}"
    case n: Limit =>
      s"value=${n.value}"
    case n: Regex =>
      s"neg=${n.negation} pattern=${term(n.pattern)} flags=${term(n.flags)}"
    case n: Contains =>
      s"neg=${n.negation} value=${term(n.value)}"
    case n: StrStarts =>
      s"neg=${n.negation} value=${term(n.value)}"
    case n: StrEnds =>
      s"neg=${n.negation} value=${term(n.value)}"
    case n: Equal =>
      s"neg=${n.negation} value=${term(n.value)}"
    case n: NotEqual =>
      s"neg=${n.negation} value=${term(n.value)}"
    case n: Inf =>
      s"neg=${n.negation} value=${term(n.value)}"
    case n: InfEqual =>
      s"neg=${n.negation} value=${term(n.value)}"
    case n: Sup =>
      s"neg=${n.negation} value=${term(n.value)}"
    case n: SupEqual =>
      s"neg=${n.negation} value=${term(n.value)}"
    case n: Str =>
      s"term=${term(n.term)}"
    case n: StrDt =>
      s"term=${term(n.term)} datatype=${term(n.datatype)}"
    case n: Lang =>
      s"term=${term(n.term)}"
    case n: LangMatches =>
      s"term=${term(n.term)}"
    case n: Value =>
      term(n.term)
    case n: ListValues =>
      s"size=${n.terms.size} [${n.terms.map(term).mkString(", ")}]"
    case n: DatatypeNode =>
      s"ref=${n.refNode}"
    case n: SourcesNode =>
      s"ref=${n.refNode} sources=[${n.sources.mkString(", ")}]"
    case _ =>
      ""
  }

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

  private def safeRef(v: String): String =
    if (v == null || v.trim.isEmpty) "?" else v
}