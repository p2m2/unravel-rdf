package fr.inrae.metabohub.semantic_web.node.pm

import fr.inrae.metabohub.semantic_web.node._
import fr.inrae.metabohub.semantic_web.rdf.QueryVariable

object NodeVisitor  {

  def getNodeWithRef(ref : String, n: Node ) : Array[RdfNode] = n match {
            case node : RdfNode  if (node.reference() == ref) => Array[RdfNode](node)
            case _   => n.children.toArray.flatMap( child => getNodeWithRef( ref, child ))
  }

  /**
   * Get All ancestors croissant order
   * @param childRef
   * @return
   */
  def getAncestorsRef( childRef: String, n : Node ) : Seq[String] =  n match {
    case node : RdfNode  if (node.reference() == childRef) => Seq(childRef)
    case node   => n.children.flatMap( child => {
      getAncestorsRef( childRef, child ) match {
        case listAncestor if listAncestor.length>0 => Seq(node.idRef) ++ listAncestor
        case _ => Seq()
      }
    })
  }

  /**
   * Give all variables using in the query
   * @param n
   * @return
   */
  def getAllAncestorsRef(n: Node): Seq[String] = n match {
    case node: Root =>
      Seq(node.reference()) ++
        node.children.flatMap(getAllAncestorsRef) ++
        node.lBindNode.flatMap(getAllAncestorsRef(_))
    case node: SubjectOf => node.objectTerm match {
      case q: QueryVariable => node.propertyTerm match {
        case q2 : QueryVariable =>
          Seq(q.name,q2.name) ++ node.children.flatMap(getAllAncestorsRef)
        case _ => Seq(q.name) ++ node.children.flatMap(getAllAncestorsRef)
      }
      case _ => node.propertyTerm match {
        case q2 : QueryVariable =>
          Seq(q2.name) ++ node.children.flatMap(getAllAncestorsRef)
        case _ => Seq(node.reference()) ++ node.children.flatMap(getAllAncestorsRef)
      }
    }
    case node: ObjectOf => node.subjectTerm match {
      case q: QueryVariable => node.propertyTerm match {
        case q2 : QueryVariable =>
          Seq(q.name,q2.name) ++ node.children.flatMap(getAllAncestorsRef)
        case _ => Seq(q.name) ++ node.children.flatMap(getAllAncestorsRef)
      }
      case _ => node.propertyTerm match {
        case q2 : QueryVariable =>
          Seq(q2.name) ++ node.children.flatMap(getAllAncestorsRef)
        case _ => Seq(node.reference()) ++ node.children.flatMap(getAllAncestorsRef)
      }
    }
    case node: RdfNode =>
      Seq(node.reference()) ++ node.children.flatMap(getAllAncestorsRef)
    case node: Bind =>
      Seq(node.reference()) ++ node.children.flatMap(getAllAncestorsRef)
    case node: FilterNode =>
      Seq(node.reference()) ++ node.children.flatMap(getAllAncestorsRef)
    case _ =>
      Seq()
  }

  /**
   * Apply a Visitor on the Node and the children element recursively
   * @param n
   * @param deep
   * @param visitor
   * @tparam A
   * @return
   */

  def map[A](  n : Root, deep : Integer , visitor : (Node,Integer) => A )  : Seq[A] =
    Seq(visitor(n,deep)) ++:
      n.children.flatMap( nc => NodeVisitor.map(nc, deep+1, visitor) ) ++:
      n.lBindNode.flatMap( nc => NodeVisitor.map(nc, deep+1, visitor) ) ++:
      n.lDatatypeNode.flatMap( nc => NodeVisitor.map(nc, deep+1, visitor) ) ++:
      n.lSolutionSequenceModifierNode.flatMap( nc => NodeVisitor.map(nc, deep+1, visitor) ) ++:
      n.lSourcesNodes.flatMap( nc => NodeVisitor.map(nc, deep+1, visitor) )

  def map[A](  n : Node, deep : Integer , visitor : (Node,Integer) => A )  : Seq[A] =
    Seq(visitor(n,deep)) ++: n.children.flatMap( nc => NodeVisitor.map(nc, deep+1, visitor) )

}
