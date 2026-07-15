// SPDX-FileCopyrightText: 2020-2026 INRAE
// SPDX-License-Identifier: GPL-3.0-or-later

/**
 * olivier.filangi@inrae.fr - P2M2 Platform
 * Project: https://forge.inrae.fr/p2m2/discovery
 */

package facade.npm

import scala.scalajs.js
import scala.scalajs.js.annotation.JSName

@js.native
trait Term extends js.Object {
  val termType: String = js.native
  val value: String = js.native

  @JSName("equals")
  def equalsTerm(other: Term): Boolean = js.native
}

@js.native
trait NamedNode extends Term

@js.native
trait BlankNode extends Term

@js.native
trait Literal extends Term {
  val language: String = js.native
  val datatype: NamedNode = js.native
}

@js.native
trait Variable extends Term

@js.native
trait DefaultGraph extends Term