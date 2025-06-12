/**
 * olivier.filangi@inrae.fr - P2M2 Platform - https://github.com/p2m2
 */
package facade.npm

import scala.language.implicitConversions
import scala.scalajs.js
import scala.scalajs.js.annotation.JSName


sealed trait Term extends js.Object {
  val termType : String
  val value : String

  @JSName("equals")
  def ==(that: Term): Boolean

}

class NamedNode(val value : String) extends Term {
  val termType : String = "NamedNode"

  @JSName("equals")
  override def ==(that: Term): Boolean = that match {
    case nm : NamedNode => value == nm.value
    case _ => false
  }
  def !=(that: Term): Boolean = ! (==(that))
}

object NamedNode {
  implicit def stringToNamedNode(uri: String): NamedNode = new NamedNode(uri)
}


class BlankNode(val value : String) extends Term {
  val termType : String = "BlankNode"

  @JSName("equals")
  override def ==(that: Term): Boolean = that match {
    case bn : BlankNode => value == bn.value
    case _ => false
  }

  def !=(that: Term): Boolean = ! (==(that))
}

class Literal(val value : String,val language : String,val datatype: NamedNode) extends Term {
  val termType : String = "Literal"

  @JSName("equals")
  override def ==(that: Term): Boolean = that match {
    case lit : Literal => (value == lit.value)&&(language == lit.language)&&(datatype == lit.datatype)
    case _ => false
  }

  def !=(that: Term): Boolean = ! (==(that))

}

class Variable(val value : String) extends Term {
  val termType : String = "Variable"

  @JSName("equals")
  override def ==(that: Term): Boolean = that match {
    case v : Variable => value == v.value
    case _ => false
  }

  def !=(that: Term): Boolean = ! (==(that))
}

class DefaultGraph() extends Term {
  val termType : String = "DefaultGraph"
  val value = ""

  @JSName("equals")
  override def ==(that: Term): Boolean = that match {
    case s : DefaultGraph => true
    case _ => false
  }

  def !=(that: Term): Boolean = ! (==(that))
}