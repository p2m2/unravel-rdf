package fr.inrae.metabohub.semantic_web.node.pm

import fr.inrae.metabohub.semantic_web.{UnravelQuery, UnravelSession}
import fr.inrae.metabohub.semantic_web.node.Node
import org.scalajs.dom
import org.scalajs.dom.document
import org.scalajs.dom.html

import scala.scalajs.js

case class DebugLink(
                      label: String,
                      endpoint: String,
                      endpointUrl: String,
                      yasguiUrl: String
                    )

case class PrefixEntry(
                        prefix: String,
                        namespace: String
                      )

case class DebugSnapshot(
                          title: String = "Unravel Debug Screen",
                          userRequest: String,
                          focusNode: String,
                          sources: Seq[String],
                          endpointLinks: Seq[DebugLink] = Seq.empty,
                          prefixes: Seq[PrefixEntry] = Seq.empty,
                          sparql: String = "",
                          rootNode: Option[Node] = None,
                          errorTitle: Option[String] = None,
                          errorDetail: Option[String] = None,
                          stackTrace: Option[String] = None
                        )

object DebugTheme {
  val bgOverlay = "rgba(2, 6, 23, 0.92)"
  val panel = "#111827"
  val panel2 = "#0f172a"
  val border = "#334155"
  val text = "#e2e8f0"

  val accent = "#22d3ee"
  val accent2 = "#a78bfa"
  val success = "#84cc16"
  val warn = "#f59e0b"
  val danger = "#fb7185"

  val codeBg = "#020617"
}

object DebugSnapshot {

  private val YasguiBase = "https://yasgui.triply.cc/"

  def fromSession(session: UnravelSession): DebugSnapshot =
    DebugSnapshot(
      userRequest = SimpleConsole(consoleColor = false).get(session.rootNode),
      focusNode = String.valueOf(session.focusNode),
      sources = session.config.sources.map(_.path),
      endpointLinks = buildEndpointLinks(session),
      rootNode = Option(session.rootNode),
      prefixes = extractPrefixes(session),
      sparql = session.sparql
    )

  def fromTransaction(query: UnravelQuery): DebugSnapshot =
    fromSession(query.sw)

  private def buildEndpointLinks(session: UnravelSession): Seq[DebugLink] = {
    val sparql = session.sparql

    session.config.sources
      .filter(_.mimetype.trim.toLowerCase == "application/sparql-query")
      .map(_.resolvedPath)
      .distinct
      .filter(_.nonEmpty)
      .map { endpoint =>
        DebugLink(
          label = endpoint,
          endpoint = endpoint,
          endpointUrl = buildEndpointUrl(endpoint, sparql),
          yasguiUrl = buildYasguiUrl(endpoint, sparql)
        )
      }
  }

  private def buildEndpointUrl(endpoint: String, sparql: String): String = {
    val q = js.URIUtils.encodeURIComponent(sparql)
    val sep = if (endpoint.contains("?")) "&" else "?"
    s"$endpoint${sep}query=$q"
  }

  private def buildYasguiUrl(endpoint: String, sparql: String): String = {
    val e = js.URIUtils.encodeURIComponent(endpoint)
    val q = js.URIUtils.encodeURIComponent(sparql)
    s"$YasguiBase?endpoint=$e&query=$q"
  }

  private def extractPrefixes(session: UnravelSession): Seq[PrefixEntry] = {
    try {
      Option(session.rootNode.prefixes)
        .map(_.toSeq)
        .getOrElse(Seq.empty)
        .map { case (k, v) => PrefixEntry(k, String.valueOf(v)) }
        .sortBy(_.prefix)
    } catch {
      case _: Throwable => Seq.empty
    }
  }
}

object DebugOverlay {

  private val overlayId = "unravel-debug-overlay"

  def show(snapshot: DebugSnapshot): Unit = {
    remove()
    QueryTreeHtmlStyle.ensureInjected()

    val overlay = div("div", overlayId)
    styleOverlay(overlay)

    val panel = div("div")
    stylePanel(panel)

    panel.appendChild(header(snapshot))
    snapshot.errorTitle.foreach(_ => panel.appendChild(errorBox(snapshot)))

    if (snapshot.sparql.nonEmpty) {
      panel.appendChild(section("SPARQL request", snapshot.sparql, open = true))
    }

    if (snapshot.endpointLinks.nonEmpty) {
      panel.appendChild(endpointSection(snapshot.endpointLinks))
    }

    if (snapshot.prefixes.nonEmpty) {
      panel.appendChild(prefixesSection(snapshot.prefixes))
    }

    snapshot.rootNode.foreach { root =>
      panel.appendChild(treeSection(root, snapshot.focusNode))
    }

    snapshot.stackTrace.foreach(st => panel.appendChild(section("Stack trace", st)))

    overlay.appendChild(panel)
    document.body.appendChild(overlay)
  }

  def remove(): Unit =
    Option(document.getElementById(overlayId)).foreach(_.remove())

  private def header(snapshot: DebugSnapshot): html.Div = {
    val box = div("div")
    box.style.display = "flex"
    box.style.setProperty("justify-content", "space-between")
    box.style.setProperty("align-items", "center")
    box.style.setProperty("gap", "16px")
    box.style.marginBottom = "20px"
    box.style.padding = "0 0 14px 0"
    box.style.borderBottom = s"1px solid ${DebugTheme.border}"

    val title = document.createElement("h1").asInstanceOf[html.Heading]
    title.textContent = snapshot.title
    title.style.margin = "0"
    title.style.color = DebugTheme.text
    title.style.fontSize = "24px"
    title.style.letterSpacing = "0.02em"

    val actions = div("div")
    actions.style.display = "flex"
    actions.style.setProperty("gap", "10px")

    val closeBtn = button("Close")
    closeBtn.onclick = (_: dom.MouseEvent) => {
      remove()
      ()
    }

    actions.appendChild(closeBtn)
    box.appendChild(title)
    box.appendChild(actions)
    box
  }

  private def errorBox(snapshot: DebugSnapshot): html.Div = {
    val box = div("div")
    box.style.background = "linear-gradient(180deg, rgba(127,29,29,0.35) 0%, rgba(69,10,10,0.25) 100%)"
    box.style.border = s"1px solid ${DebugTheme.danger}"
    box.style.borderRadius = "10px"
    box.style.padding = "14px"
    box.style.marginBottom = "18px"
    box.style.boxShadow = "0 10px 30px rgba(127,29,29,0.22)"

    val title = document.createElement("div").asInstanceOf[html.Div]
    title.textContent = snapshot.errorTitle.getOrElse("Error")
    title.style.fontWeight = "700"
    title.style.marginBottom = "6px"
    title.style.color = "#fecdd3"

    box.appendChild(title)

    snapshot.errorDetail.foreach { d =>
      val msg = document.createElement("div").asInstanceOf[html.Div]
      msg.textContent = d
      msg.style.color = "#ffe4e6"
      box.appendChild(msg)
    }

    box
  }

  private def section(label: String, content: String, open: Boolean = false): html.Element = {
    val color = sectionColor(label)

    val details = document.createElement("details").asInstanceOf[html.Element]
    if (open) details.setAttribute("open", "true")
    details.style.marginBottom = "14px"
    details.style.background = "#111827"
    details.style.border = s"1px solid ${DebugTheme.border}"
    details.style.borderRadius = "10px"
    details.style.boxShadow = "0 8px 24px rgba(0,0,0,0.18)"

    val summary = document.createElement("summary").asInstanceOf[html.Element]
    summary.textContent = label
    summary.style.cursor = "pointer"
    summary.style.padding = "12px 14px"
    summary.style.fontWeight = "700"
    summary.style.color = color
    summary.style.setProperty("list-style", "none")
    summary.style.borderLeft = s"4px solid $color"
    summary.style.background = "rgba(255,255,255,0.02)"

    val pre = document.createElement("pre").asInstanceOf[html.Pre]
    pre.textContent = content
    pre.style.margin = "0"
    pre.style.padding = "14px"
    pre.style.whiteSpace = "pre-wrap"
    pre.style.wordBreak = "break-word"
    pre.style.overflowX = "auto"
    pre.style.color = DebugTheme.text
    pre.style.background = DebugTheme.codeBg
    pre.style.borderTop = s"1px solid ${DebugTheme.border}"
    pre.style.lineHeight = "1.5"
    pre.style.fontSize = "13px"

    details.appendChild(summary)
    details.appendChild(pre)
    details
  }

  private def buildPrefixCcPrefixUrl(prefix: String): String = {
    val p = js.URIUtils.encodeURIComponent(prefix.trim)
    s"https://prefix.cc/$p"
  }

  private def buildPrefixCcUrl(url: String): String = {
    val raw = Option(url).getOrElse("").trim
    val cleaned =
      if (raw.startsWith("<") && raw.endsWith(">")) raw.substring(1, raw.length - 1).trim
      else raw
    val p = js.URIUtils.encodeURIComponent(cleaned)
    s"https://prefix.cc/?q=$p"
  }

  private def badgeLink(
                         label: String,
                         href: String,
                         title: String,
                         background: String,
                         border: String,
                         color: String
                       ): html.Anchor = {
    val a = document.createElement("a").asInstanceOf[html.Anchor]
    a.textContent = label
    a.href = href
    a.target = "_blank"
    a.rel = "noopener noreferrer"
    a.title = title
    a.style.display = "inline-block"
    a.style.padding = "6px 10px"
    a.style.borderRadius = "999px"
    a.style.background = background
    a.style.border = s"1px solid $border"
    a.style.color = color
    a.style.textDecoration = "none"
    a.style.fontSize = "12px"
    a.style.fontWeight = "700"
    a.style.lineHeight = "1.35"
    a.style.fontFamily = "ui-monospace, SFMono-Regular, Menlo, Consolas, monospace"
    a.style.wordBreak = "break-word"
    a.style.maxWidth = "100%"
    a.style.overflow = "hidden"
    a.style.textOverflow = "ellipsis"
    a.style.whiteSpace = "nowrap"
    a
  }

  private def shortNamespace(ns: String, max: Int = 64): String = {
    val value = Option(ns).getOrElse("")
    if (value.length <= max) value
    else value.take(max - 1) + "…"
  }

  private def prefixesSection(prefixes: Seq[PrefixEntry]): html.Element = {
    val color = "#34d399"

    val details = document.createElement("details").asInstanceOf[html.Element]
    details.style.marginBottom = "14px"
    details.style.background = "#111827"
    details.style.border = s"1px solid ${DebugTheme.border}"
    details.style.borderRadius = "10px"
    details.style.boxShadow = "0 8px 24px rgba(0,0,0,0.18)"

    val summary = document.createElement("summary").asInstanceOf[html.Element]
    summary.textContent = s"Prefixes (${prefixes.size})"
    summary.style.cursor = "pointer"
    summary.style.padding = "12px 14px"
    summary.style.fontWeight = "700"
    summary.style.color = color
    summary.style.setProperty("list-style", "none")
    summary.style.borderLeft = s"4px solid $color"
    summary.style.background = "rgba(255,255,255,0.02)"

    val content = document.createElement("div").asInstanceOf[html.Div]
    content.style.padding = "14px"
    content.style.display = "grid"
    content.style.setProperty("gridTemplateColumns", "repeat(auto-fit, minmax(320px, 1fr))")
    content.style.setProperty("gap", "10px")
    content.style.setProperty("alignItems", "start")

    prefixes.foreach { p =>
      val row = document.createElement("div").asInstanceOf[html.Div]
      row.style.display = "flex"
      row.style.setProperty("flexWrap", "wrap")
      row.style.setProperty("alignItems", "center")
      row.style.setProperty("gap", "8px")
      row.style.padding = "10px 12px"
      row.style.border = s"1px solid ${DebugTheme.border}"
      row.style.borderRadius = "10px"
      row.style.background = "rgba(255,255,255,0.02)"
      row.style.minWidth = "0"

      val prefixBadge = badgeLink(
        label = p.prefix,
        href = buildPrefixCcPrefixUrl(p.prefix),
        title = s"Open prefix ${p.prefix} on prefix.cc",
        background = "rgba(52, 211, 153, 0.12)",
        border = "rgba(52, 211, 153, 0.30)",
        color = "#6ee7b7"
      )

      val nsBadge = badgeLink(
        label = shortNamespace(p.namespace),
        href = buildPrefixCcUrl(p.namespace),
        title = p.namespace,
        background = "rgba(96, 165, 250, 0.12)",
        border = "rgba(96, 165, 250, 0.28)",
        color = "#93c5fd"
      )

      row.appendChild(prefixBadge)
      row.appendChild(nsBadge)
      content.appendChild(row)
    }

    details.appendChild(summary)
    details.appendChild(content)
    details
  }

  private def treeSection(root: Node, focusNode: String): html.Element = {
    val details = document.createElement("details").asInstanceOf[html.Element]
    details.style.marginBottom = "14px"
    details.style.background = "#111827"
    details.style.border = s"1px solid ${DebugTheme.border}"
    details.style.borderRadius = "10px"
    details.style.boxShadow = "0 8px 24px rgba(0,0,0,0.18)"

    val summary = document.createElement("summary").asInstanceOf[html.Element]
    summary.textContent = "Unravel node tree"
    summary.style.cursor = "pointer"
    summary.style.padding = "12px 14px"
    summary.style.fontWeight = "700"
    summary.style.color = "#fda4af"
    summary.style.setProperty("list-style", "none")
    summary.style.borderLeft = s"4px solid #fda4af"
    summary.style.background = "rgba(255,255,255,0.02)"

    val content = document.createElement("div").asInstanceOf[html.Div]
    content.style.padding = "14px"

    val ul = document.createElement("ul").asInstanceOf[html.UList]
    ul.className = "unravel-tree"
    ul.appendChild(
      QueryTreeHtmlRenderer.renderNode(
        root,
        focusRef = Option(focusNode).filter(_.nonEmpty)
      )
    )

    content.appendChild(ul)
    details.appendChild(summary)
    details.appendChild(content)
    details
  }

  private def endpointSection(links: Seq[DebugLink]): html.Element = {
    val color = "#38bdf8"

    val details = document.createElement("details").asInstanceOf[html.Element]
    details.setAttribute("open", "true")
    details.style.marginBottom = "14px"
    details.style.background = "#111827"
    details.style.border = s"1px solid ${DebugTheme.border}"
    details.style.borderRadius = "10px"
    details.style.boxShadow = "0 8px 24px rgba(0,0,0,0.18)"

    val summary = document.createElement("summary").asInstanceOf[html.Element]
    summary.textContent = "Generated test SPARQL requests"
    summary.style.cursor = "pointer"
    summary.style.padding = "12px 14px"
    summary.style.fontWeight = "700"
    summary.style.color = color
    summary.style.setProperty("list-style", "none")
    summary.style.borderLeft = s"4px solid $color"
    summary.style.background = "rgba(255,255,255,0.02)"

    val content = document.createElement("div").asInstanceOf[html.Div]
    content.style.padding = "14px"
    content.style.display = "flex"
    content.style.setProperty("flexDirection", "column")
    content.style.setProperty("gap", "12px")

    links.foreach { link =>
      val row = document.createElement("div").asInstanceOf[html.Div]
      row.style.display = "flex"
      row.style.setProperty("flexWrap", "wrap")
      row.style.setProperty("alignItems", "center")
      row.style.setProperty("gap", "8px")

      val badge = document.createElement("span").asInstanceOf[html.Span]
      badge.textContent = link.label
      badge.style.display = "inline-block"
      badge.style.padding = "6px 10px"
      badge.style.borderRadius = "999px"
      badge.style.border = s"1px solid ${DebugTheme.border}"
      badge.style.background = "rgba(34, 211, 238, 0.10)"
      badge.style.color = DebugTheme.accent
      badge.style.fontSize = "12px"
      badge.style.lineHeight = "1.4"
      badge.style.wordBreak = "break-all"
      badge.style.fontWeight = "600"
      badge.style.fontFamily = "ui-monospace, SFMono-Regular, Menlo, Consolas, monospace"

      val sparqlLink = document.createElement("a").asInstanceOf[html.Anchor]
      sparqlLink.textContent = "Run request"
      sparqlLink.href = link.endpointUrl
      sparqlLink.target = "_blank"
      sparqlLink.rel = "noopener noreferrer"
      sparqlLink.title = s"Open endpoint ${link.endpoint} with current SPARQL query"
      styleActionLink(sparqlLink, "#60a5fa")

      val yasguiLink = document.createElement("a").asInstanceOf[html.Anchor]
      yasguiLink.textContent = "Open in YASGUI"
      yasguiLink.href = link.yasguiUrl
      yasguiLink.target = "_blank"
      yasguiLink.rel = "noopener noreferrer"
      yasguiLink.title = s"Open ${link.endpoint} in YASGUI with current SPARQL query"
      styleActionLink(yasguiLink, DebugTheme.accent2)

      row.appendChild(badge)
      row.appendChild(sparqlLink)
      row.appendChild(yasguiLink)
      content.appendChild(row)
    }

    details.appendChild(summary)
    details.appendChild(content)
    details
  }

  private def styleActionLink(a: html.Anchor, color: String): Unit = {
    a.style.display = "inline-block"
    a.style.padding = "6px 10px"
    a.style.borderRadius = "8px"
    a.style.border = s"1px solid ${DebugTheme.border}"
    a.style.background = "rgba(255,255,255,0.03)"
    a.style.color = color
    a.style.textDecoration = "none"
    a.style.fontSize = "12px"
    a.style.fontWeight = "700"
  }

  private def div(tag: String, id: String = ""): html.Div = {
    val el = document.createElement(tag).asInstanceOf[html.Div]
    if (id.nonEmpty) el.id = id
    el
  }

  private def button(label: String): html.Button = {
    val b = document.createElement("button").asInstanceOf[html.Button]
    b.textContent = label
    b.style.padding = "8px 12px"
    b.style.border = s"1px solid ${DebugTheme.border}"
    b.style.borderRadius = "8px"
    b.style.background = "#1e293b"
    b.style.color = DebugTheme.text
    b.style.cursor = "pointer"
    b.style.fontWeight = "600"
    b.style.boxShadow = "inset 0 1px 0 rgba(255,255,255,0.05)"
    b
  }

  private def styleOverlay(el: html.Div): Unit = {
    el.style.position = "fixed"
    el.style.top = "0"
    el.style.right = "0"
    el.style.bottom = "0"
    el.style.left = "0"
    el.style.zIndex = "2147483647"
    el.style.background = DebugTheme.bgOverlay
    el.style.padding = "24px"
    el.style.overflow = "auto"
    el.style.fontFamily = "ui-monospace, SFMono-Regular, Menlo, Consolas, monospace"
    el.style.color = DebugTheme.text
  }

  private def stylePanel(el: html.Div): Unit = {
    el.style.maxWidth = "1200px"
    el.style.margin = "0 auto"
    el.style.background = s"linear-gradient(180deg, ${DebugTheme.panel} 0%, ${DebugTheme.panel2} 100%)"
    el.style.border = s"1px solid ${DebugTheme.border}"
    el.style.borderRadius = "14px"
    el.style.padding = "20px"
    el.style.boxShadow = "0 24px 80px rgba(0,0,0,0.55)"
  }

  private def sectionColor(label: String): String = label match {
    case "User request"                => DebugTheme.accent
    case "Unravel internal structure"  => DebugTheme.accent
    case "Focus node"                  => DebugTheme.success
    case "SPARQL request"              => "#38bdf8"
    case "Sources"                     => DebugTheme.warn
    case "Children"                    => "#fda4af"
    case "Stack trace"                 => DebugTheme.danger
    case _                             => DebugTheme.text
  }
}