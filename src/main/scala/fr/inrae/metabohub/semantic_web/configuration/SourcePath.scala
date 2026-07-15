// SPDX-FileCopyrightText: 2020-2026 INRAE
// SPDX-License-Identifier: GPL-3.0-or-later

package fr.inrae.metabohub.semantic_web.configuration
import fr.inrae.metabohub.semantic_web.exception.SWStatementConfigurationException


object SourcePath extends Enumeration {
  type SourcePath = Value
  val LocalFile,          /* local file */
  UrlPath,            /* url access  : file url, endpoint */
  Content = Value     /* online definition */

  implicit val rw: OptionPickler.ReadWriter[SourcePath] = OptionPickler.readwriter[String].bimap[SourcePath](
    x => x.toString ,
    {
      case "LocalFile" => LocalFile
      case "UrlPath" => UrlPath
      case "Content" => Content
      case str => throw SWStatementConfigurationException(s"Unknown SourcePath $str")
    }
  )
}