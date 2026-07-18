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
                          errorTitle: Option[String] = None,
                          errorDetail: Option[String] = None,
                          stackTrace: Option[String] = None
                        )

object DebugSnapshot {
  def fromSession(session: UnravelSession): DebugSnapshot =
    DebugSnapshot(
      userRequest = SimpleConsole(consoleColor = false).get(session.rootNode),
      focusNode = String.valueOf(session.focusNode),
      sources = session.config.sources.map(_.path),
      sparqlGet = session.sparql_get,
      sparqlCurl = session.sparql_curl
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
      errorTitle = Some("JavaScript Runtime Error"),
      errorDetail = Some(message),
      stackTrace = stack
    )
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

    val title = document.createElement("h1").asInstanceOf[html.Heading]
    title.textContent = snapshot.title
    title.style.margin = "0"
    title.style.fontSize = "22px"
    title.style.color = "#fff5f5"

    val actions = div("div")
    actions.style.display = "flex"
    box.style.setProperty("gap", "16px")

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
    box.style.background = "#4a1f24"
    box.style.border = "1px solid #8f3b46"
    box.style.borderRadius = "8px"
    box.style.padding = "14px"
    box.style.marginBottom = "18px"

    val title = document.createElement("div").asInstanceOf[html.Div]
    title.textContent = snapshot.errorTitle.getOrElse("Error")
    title.style.fontWeight = "700"
    title.style.marginBottom = "6px"
    title.style.color = "#ffd7d7"

    box.appendChild(title)

    snapshot.errorDetail.foreach { d =>
      val msg = document.createElement("div").asInstanceOf[html.Div]
      msg.textContent = d
      msg.style.color = "#ffeaea"
      box.appendChild(msg)
    }

    box
  }

  private def section(label: String, content: String, open: Boolean = false): html.Element = {
    val details = document.createElement("details").asInstanceOf[html.Element]
    if (open) details.setAttribute("open", "true")
    details.style.marginBottom = "14px"
    details.style.background = "#1d2128"
    details.style.border = "1px solid #303744"
    details.style.borderRadius = "8px"

    val summary = document.createElement("summary").asInstanceOf[html.Element]
    summary.textContent = label
    summary.style.cursor = "pointer"
    summary.style.padding = "12px 14px"
    summary.style.fontWeight = "700"
    summary.style.color = "#f3c6cb"

    val pre = document.createElement("pre").asInstanceOf[html.Pre]
    pre.textContent = content
    pre.style.margin = "0"
    pre.style.padding = "0 14px 14px 14px"
    pre.style.whiteSpace = "pre-wrap"
    pre.style.wordBreak = "break-word"
    pre.style.overflowX = "auto"
    pre.style.color = "#d8dee9"

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
    b.style.border = "1px solid #5a6475"
    b.style.borderRadius = "6px"
    b.style.background = "#2a303b"
    b.style.color = "#f1f5f9"
    b.style.cursor = "pointer"
    b
  }

  private def styleOverlay(el: html.Div): Unit = {
    el.style.position = "fixed"
    el.style.top = "0"
    el.style.right = "0"
    el.style.bottom = "0"
    el.style.left = "0"
    el.style.zIndex = "2147483647"
    el.style.background = "rgba(10, 12, 16, 0.92)"
    el.style.padding = "24px"
    el.style.overflow = "auto"
    el.style.fontFamily = "ui-monospace, SFMono-Regular, Menlo, Consolas, monospace"
  }

  private def stylePanel(el: html.Div): Unit = {
    el.style.maxWidth = "1200px"
    el.style.margin = "0 auto"
    el.style.background = "#151922"
    el.style.border = "1px solid #2f3745"
    el.style.borderRadius = "12px"
    el.style.padding = "20px"
    el.style.boxShadow = "0 20px 80px rgba(0,0,0,0.45)"
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
        val msg = if (js.isUndefined(reason) || reason == null) "Unhandled promise rejection" else reason.toString
        val stack =
          if (js.isUndefined(reason) || reason == null) None
          else Option(reason.stack).map(_.toString)
        DebugOverlay.show(DebugSnapshot.fromJsError(msg, stack))
      }
    )
  }
}