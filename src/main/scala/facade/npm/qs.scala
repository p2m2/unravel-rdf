// SPDX-FileCopyrightText: 2020-2026 INRAE
// SPDX-License-Identifier: GPL-3.0-or-later

package facade.npm

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("qs", JSImport.Default)
object qs extends js.Object {
  def stringify(data: js.Dictionary[String]): String = js.native
}
