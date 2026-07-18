package fr.inrae.metabohub.semantic_web.node.pm

import fr.inrae.metabohub.semantic_web.UnravelSession
import org.scalajs.dom
import org.scalajs.dom.{document, window}
import org.scalajs.dom.html

import scala.scalajs.js

case class DebugSnapshot(
                          title: String = "Unravel Debug Screen",
                          userRequest: String,
                          focusNode: String,
                          sources: Seq[String],
                          sparqlGet: String,
                          sparqlCurl: String,
                          childrenTree: Option[String] = None,
                          errorTitle: Option[String] = None,
                          errorDetail: Option[String] = None,
                          stackTrace: Option[String] = None
                        )

object DebugTheme {
  val bg = "#0b1020"
  val bgOverlay = "rgba(2, 6, 23, 0.92)"
  val panel = "#111827"
  val panel2 = "#0f172a"
  val border = "#334155"
  val text = "#e2e8f0"
  val muted = "#94a3b8"

  val accent = "#22d3ee"
  val accent2 = "#a78bfa"
  val success = "#84cc16"
  val warn = "#f59e0b"
  val danger = "#fb7185"

  val codeBg = "#020617"
}

object DebugSnapshot {

  def fromSession(session: UnravelSession): DebugSnapshot =
    DebugSnapshot(
      userRequest = SimpleConsole(consoleColor = false).get(session.rootNode),
      focusNode = String.valueOf(session.focusNode),
      sources = session.config.sources.map(_.path),
      sparqlGet = session.sparql_get,
      sparqlCurl = session.sparql_curl,
      childrenTree = extractChildrenTree(session)
    )

  def fromThrowable(session: UnravelSession, t: Throwable): DebugSnapshot =
    fromSession(session).copy(
      errorTitle = Some(t.getClass.getName),
      errorDetail = Option(t.getMessage),
      stackTrace = Option(t.getStackTrace).map(_.mkString("\n"))
    )

  def fromJsError(message: String, stack: Option[String] = None): DebugSnapshot =
    DebugSnapshot(
      userRequest = "",
      focusNode = "",
      sources = Seq.empty,
      sparqlGet = "",
      sparqlCurl = "",
      childrenTree = None,
      errorTitle = Some("JavaScript Runtime Error"),
      errorDetail = Some(message),
      stackTrace = stack
    )

    private def extractChildrenTree(session: UnravelSession): Option[String] = {
      try {
        Option(session.rootNode.children)
          .filter(_.nonEmpty)
          .map(children => QueryPrettyPrinter.renderChildren(children))
      } catch {
        case _: Throwable => None
      }
    }
  }

object DebugTextRenderer {
  def render(s: DebugSnapshot): String =
    "═══════════════════════════════════════════════════════════\n" +
      "  USER REQUEST\n" +
      "═══════════════════════════════════════════════════════════\n" +
      s.userRequest + "\n" +
      "═══════════════════════════════════════════════════════════\n" +
      "  FOCUS NODE\n" +
      "═══════════════════════════════════════════════════════════\n" +
      s.focusNode + "\n" +
      "═══════════════════════════════════════════════════════════\n" +
      "  SOURCE\n" +
      "═══════════════════════════════════════════════════════════\n" +
      s.sources.mkString(",\n") + "\n" +
      s.childrenTree.map { c =>
        "\n═══════════════════════════════════════════════════════════\n" +
          "  CHILDREN\n" +
          "═══════════════════════════════════════════════════════════\n" +
          c + "\n"
      }.getOrElse("") +
      "\n═══════════════════════════════════════════════════════════\n" +
      "  HTTP REQUESTS\n" +
      "═══════════════════════════════════════════════════════════\n" +
      "\n  ┌─ HTTP GET ──────────────────────────────────────────\n" +
      "  │\n" + s.sparqlGet + "\n" +
      "  └─────────────────────────────────────────────────────\n" +
      "\n  ┌─ HTTP CURL ─────────────────────────────────────────\n" +
      "  │\n" + s.sparqlCurl + "\n" +
      "  └─────────────────────────────────────────────────────\n" +
      s.errorTitle.map(t => "\nERROR: " + t + "\n").getOrElse("") +
      s.errorDetail.map(d => d + "\n").getOrElse("") +
      s.stackTrace.map(st => st + "\n").getOrElse("")
}

object DebugOverlay {

  private val overlayId = "unravel-debug-overlay"

  def show(snapshot: DebugSnapshot): Unit = {
    remove()

    val overlay = div("div", overlayId)
    styleOverlay(overlay)

    val panel = div("div")
    stylePanel(panel)

    panel.appendChild(header(snapshot))
    snapshot.errorTitle.foreach(_ => panel.appendChild(errorBox(snapshot)))

    if (snapshot.userRequest.nonEmpty) panel.appendChild(section("User request", snapshot.userRequest, open = true))
    if (snapshot.focusNode.nonEmpty) panel.appendChild(section("Focus node", snapshot.focusNode))
    if (snapshot.sources.nonEmpty) panel.appendChild(section("Sources", snapshot.sources.mkString("\n")))
    snapshot.childrenTree.filter(_.nonEmpty).foreach(v => panel.appendChild(section("Children", v)))
    if (snapshot.sparqlGet.nonEmpty) panel.appendChild(section("HTTP GET", snapshot.sparqlGet))
    if (snapshot.sparqlCurl.nonEmpty) panel.appendChild(section("HTTP CURL", snapshot.sparqlCurl))
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

    val copyBtn = button("Copy report")
    copyBtn.onclick = (_: dom.MouseEvent) => {
      val txt = DebugTextRenderer.render(snapshot)
      window.navigator.clipboard.writeText(txt)
      ()
    }

    val closeBtn = button("Close")
    closeBtn.onclick = (_: dom.MouseEvent) => {
      remove()
      ()
    }

    actions.appendChild(copyBtn)
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
    case "User request" => DebugTheme.accent
    case "Focus node"   => DebugTheme.success
    case "Sources"      => DebugTheme.warn
    case "Children"     => "#fda4af"
    case "HTTP GET"     => "#60a5fa"
    case "HTTP CURL"    => DebugTheme.accent2
    case "Stack trace"  => DebugTheme.danger
    case _              => DebugTheme.text
  }
}

object DebugHooks {

  def install(): Unit = {
    window.onerror = { (message, _, _, _, error) =>
      val stack = Option(error).flatMap(e => Option(e.asInstanceOf[js.Dynamic].stack).map(_.toString))
      DebugOverlay.show(DebugSnapshot.fromJsError(String.valueOf(message), stack))
      false
    }

    window.addEventListener(
      "unhandledrejection",
      (event: dom.Event) => {
        val e = event.asInstanceOf[js.Dynamic]
        val reason = e.reason
        val msg =
          if (js.isUndefined(reason) || reason == null) "Unhandled promise rejection"
          else reason.toString
        val stack =
          if (js.isUndefined(reason) || reason == null) None
          else Option(reason.stack).map(_.toString)
        DebugOverlay.show(DebugSnapshot.fromJsError(msg, stack))
      }
    )
  }
}