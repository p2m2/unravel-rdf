// SPDX-FileCopyrightText: 2020-2026 INRAE
// SPDX-License-Identifier: GPL-3.0-or-later

package fr.inrae.metabohub.semantic_web.js.increment

import fr.inrae.metabohub.semantic_web.js.UnravelSessionJs

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel("SWFilterIncrement")
case class FilterIncrementJs(swf: UnravelSessionJs, negation : Boolean = false) {

  @JSExport
  def isLiteral: UnravelSessionJs = negation match {
    case true => UnravelSessionJs(swf.config,swf.sw.filter.not.isLiteral)
    case false => UnravelSessionJs(swf.config,swf.sw.filter.isLiteral)
  }

  @JSExport
  def isUri: UnravelSessionJs = negation match {
    case true => UnravelSessionJs(swf.config,swf.sw.filter.not.isUri)
    case false => UnravelSessionJs(swf.config,swf.sw.filter.isUri)
  }

  @JSExport
  def isBlank: UnravelSessionJs = negation match {
    case true => UnravelSessionJs(swf.config,swf.sw.filter.not.isBlank)
    case false => UnravelSessionJs(swf.config,swf.sw.filter.isUri)
  }

  @JSExport
  def regex( pattern : Any, flags : Any = "") : UnravelSessionJs = negation match {
    case true => UnravelSessionJs(swf.config,swf.sw.filter.not.regex(pattern,flags))
    case false => UnravelSessionJs(swf.config,swf.sw.filter.regex(pattern,flags))
  }

  @JSExport
  def contains(l: Any): UnravelSessionJs = negation match {
    case true => UnravelSessionJs(swf.config,swf.sw.filter.not.contains(l))
    case false => UnravelSessionJs(swf.config,swf.sw.filter.contains(l))
  }

  @JSExport
  def strStarts( string : Any ) : UnravelSessionJs = negation match {
    case true => UnravelSessionJs(swf.config,swf.sw.filter.not.strStarts(string))
    case false => UnravelSessionJs(swf.config,swf.sw.filter.strStarts(string))
  }

  @JSExport
  def strEnds( string : Any ) : UnravelSessionJs = negation match {
    case true => UnravelSessionJs(swf.config,swf.sw.filter.not.strEnds(string))
    case false => UnravelSessionJs(swf.config,swf.sw.filter.strEnds(string))
  }

  @JSExport
  def equal( value : Any ) : UnravelSessionJs = negation match {
    case true => UnravelSessionJs(swf.config,swf.sw.filter.not.equal(value))
    case false => UnravelSessionJs(swf.config,swf.sw.filter.equal(value))
  }

  @JSExport
  def notEqual( value : Any ) : UnravelSessionJs = negation match {
    case true => UnravelSessionJs(swf.config,swf.sw.filter.not.notEqual(value))
    case false => UnravelSessionJs(swf.config,swf.sw.filter.notEqual(value))
  }

  @JSExport
  def inf( value : Any ) : UnravelSessionJs = negation match {
    case true => UnravelSessionJs(swf.config,swf.sw.filter.not.inf(value))
    case false => UnravelSessionJs(swf.config,swf.sw.filter.inf(value))
  }

  @JSExport
  def infEqual( value : Any ) : UnravelSessionJs = negation match {
    case true => UnravelSessionJs(swf.config,swf.sw.filter.not.infEqual(value))
    case false => UnravelSessionJs(swf.config,swf.sw.filter.infEqual(value))
  }

  @JSExport
  def sup( value : Any ) : UnravelSessionJs = negation match {
    case true => UnravelSessionJs(swf.config,swf.sw.filter.not.sup(value))
    case false => UnravelSessionJs(swf.config,swf.sw.filter.sup(value))
  }

  @JSExport
  def supEqual( value : Any ) : UnravelSessionJs = negation match {
    case true => UnravelSessionJs(swf.config,swf.sw.filter.not.supEqual(value))
    case false => UnravelSessionJs(swf.config,swf.sw.filter.supEqual(value))
  }

  @JSExport
  def not: FilterIncrementJs = FilterIncrementJs(swf,!negation)

}
