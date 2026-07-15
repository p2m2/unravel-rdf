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
trait Quad extends js.Object {
  val subject: Term = js.native
  val predicate: NamedNode = js.native
  val `object`: Term = js.native
  val graph: Term = js.native

  @JSName("equals")
  def equalsQuad(other: Quad): Boolean = js.native
}