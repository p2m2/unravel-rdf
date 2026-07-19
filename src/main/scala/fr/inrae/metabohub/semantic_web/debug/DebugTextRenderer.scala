package fr.inrae.metabohub.semantic_web.node.pm

object DebugTextRenderer {

  private val LineWidth = 78
  private val Fill = "_"

  private def section(title: String): String = {
    val cleanTitle = s" $title "
    val remaining = math.max(0, LineWidth - cleanTitle.length)
    val left = remaining / 2
    val right = remaining - left
    (Fill * left) + cleanTitle + (Fill * right)
  }

  private def block(title: String, content: String): Option[String] =
    Option(content)
      .map(_.trim)
      .filter(_.nonEmpty)
      .map(value => s"${section(title)}\n\n$value")

  def render(snapshot: DebugSnapshot): String = {
    val parts = Seq(
      Some(section(snapshot.title)),

      block("Generated SPARQL query", snapshot.sparql),

      if (snapshot.sources.nonEmpty)
        Some(
          s"""${section("Sources")}\n
             |${snapshot.sources.map(s => s"- $s").mkString("\n")}""".stripMargin
        )
      else None,

      if (snapshot.endpointLinks.nonEmpty)
        Some(
          s"""${section("Generated test SPARQL requests")}\n
             |${snapshot.endpointLinks.map { l =>
            s"- ${l.label}\n  SPARQL: ${l.endpointUrl}\n  YASGUI: ${l.yasguiUrl}"
          }.mkString("\n")}""".stripMargin
        )
      else None,

      snapshot.errorTitle.map { t =>
        val detail = snapshot.errorDetail.map(d => s"\n$d").getOrElse("")
        s"${section("Error")}\n$t$detail"
      },

      snapshot.stackTrace.map(st => s"${section("Stack trace")}\n$st")
    ).flatten

    parts.mkString("\n\n") + "\n"
  }
}