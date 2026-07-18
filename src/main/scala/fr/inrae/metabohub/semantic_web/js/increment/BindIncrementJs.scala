// SPDX-FileCopyrightText: 2020-2026 INRAE
// SPDX-License-Identifier: GPL-3.0-or-later

package fr.inrae.metabohub.semantic_web.js.increment

import fr.inrae.metabohub.semantic_web.js.UnravelSessionJs

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel("BindIncrement")
case class BindIncrementJs(swf: UnravelSessionJs, `var` : String) {

  @JSExport
  def subStr(startingLoc : Any,length : Any ) : UnravelSessionJs =
    UnravelSessionJs(swf.config,swf.sw.bind(`var`).subStr(startingLoc, length))

  @JSExport
  def replace(pattern : Any, replacement : Any, flags : Any="") :  UnravelSessionJs =
    UnravelSessionJs(swf.config,swf.sw.bind(`var`).replace(pattern, replacement,flags))

  @JSExport
  def abs() :  UnravelSessionJs = UnravelSessionJs(swf.config,swf.sw.bind(`var`).abs())

  @JSExport
  def round() :  UnravelSessionJs = UnravelSessionJs(swf.config,swf.sw.bind(`var`).round())

  @JSExport
  def ceil() :  UnravelSessionJs = UnravelSessionJs(swf.config,swf.sw.bind(`var`).ceil())

  @JSExport
  def floor() :  UnravelSessionJs = UnravelSessionJs(swf.config,swf.sw.bind(`var`).floor())

  @JSExport
  def rand() :  UnravelSessionJs = UnravelSessionJs(swf.config,swf.sw.bind(`var`).rand())

  @JSExport
  def datatype() :  UnravelSessionJs = UnravelSessionJs(swf.config,swf.sw.bind(`var`).datatype())

  @JSExport
  def str() :  UnravelSessionJs = UnravelSessionJs(swf.config,swf.sw.bind(`var`).str())

  @JSExport
  def strdt(datatype : Any) :  UnravelSessionJs = UnravelSessionJs(swf.config,swf.sw.bind(`var`).strdt(datatype))

}
