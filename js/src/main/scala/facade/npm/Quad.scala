/**
 * olivier.filangi@inrae.fr - P2M2 Platform - https://github.com/p2m2
 */
package facade.npm

import scala.scalajs.js
import scala.language.implicitConversions

class Quad(val subject : Term,val predicate : Term,val `object` : Term,val graph : Term = new DefaultGraph()) extends js.Object {
  def ==(quad: Quad): Boolean =
    (subject == quad.subject) && (predicate == quad.predicate) && (`object` == quad.`object`) && (graph == quad.graph)

  def !=(that: Quad): Boolean = ! (==(that))
}

object Quad {

  implicit def quadrupletNameNode2Quad(quad : (Term,Term,Term,Term)) : Quad = DataFactory.quad(quad._1,quad._2,quad._3,quad._4)
  implicit def quadrupletNameNode2Quad(quad : (Term,Term,Term)) : Quad = DataFactory.quad(quad._1,quad._2,quad._3,null)
  implicit def quad2namedNodeQuadruplet(quad : Quad ) : (Term,Term,Term,Term) = (quad.subject,quad.predicate,quad.`object`,quad.graph)

  implicit def quad2String( quad : Quad ) : String =
    "("+ quad.subject + "," + quad.predicate + quad.`object` + "," + quad.graph + ")"
  implicit def term2String( term : Term ) : String = term.value +"#"+term.termType
}

