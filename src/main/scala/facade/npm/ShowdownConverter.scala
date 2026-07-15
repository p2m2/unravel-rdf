// SPDX-FileCopyrightText: 2020-2026 INRAE
// SPDX-License-Identifier: GPL-3.0-or-later

/**
 * olivier.filangi@inrae.fr - P2M2 Platform
 * Project: https://forge.inrae.fr/p2m2/discovery
 */

package facade.npm

import scala.scalajs.js
import scala.scalajs.js.annotation._

@js.native
@JSImport("showdown", "Converter")
class ShowdownConverter(options: js.UndefOr[js.Object] = js.undefined) extends js.Object {
  def makeHtml(text: String): String = js.native
}