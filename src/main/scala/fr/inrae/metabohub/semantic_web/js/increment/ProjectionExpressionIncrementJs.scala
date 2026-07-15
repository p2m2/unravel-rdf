// SPDX-FileCopyrightText: 2020-2026 INRAE
// SPDX-License-Identifier: GPL-3.0-or-later

package fr.inrae.metabohub.semantic_web.js.increment

import fr.inrae.metabohub.semantic_web.js.UnravelQueryJs

import scala.scalajs._
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel("ProjectionExpressionIncrement")
case class ProjectionExpressionIncrementJs(swf: UnravelQueryJs, `var` : String) {
  @JSExport
  def count(ref: js.Array[String], distinct: Boolean=false) : UnravelQueryJs = UnravelQueryJs(swf.transaction.aggregate(`var`).count(ref.toSeq,distinct))
}
