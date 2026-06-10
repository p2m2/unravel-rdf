package fr.inrae.metabohub.semantic_web.node.pm

import fr.inrae.metabohub.semantic_web.node._
import fr.inrae.metabohub.semantic_web.rdf.QueryVariable

object NodeVisitor  {

  def getNodeWithVariableRef(ref: String, n: Node): Array[Node] = n match {
    case r:Root if r.idRef == ref => Array[Node](r)
    case s: SubjectOf =>
      (s.propertyTerm, s.objectTerm) match {
        case (q: QueryVariable, _) if q.name == ref => Array[Node](SubjectOf
        (
          idRef = q.name,propertyTerm = s.propertyTerm, objectTerm = s.objectTerm, children = s.children,
          decorations = s.decorations
        ))
        case (_, q: QueryVariable) if q.name == ref => Array[Node](SubjectOf
        (
          idRef = q.name,propertyTerm = s.propertyTerm, objectTerm = s.objectTerm, children = s.children,
          decorations = s.decorations
        ))
        case _ => n.children.toArray.flatMap(child => getNodeWithVariableRef(ref, child))
      }

    case o: ObjectOf =>
      (o.propertyTerm, o.subjectTerm) match {
        case (q: QueryVariable, _) if q.name == ref => Array[Node](ObjectOf
        (
          idRef = q.name,propertyTerm = o.propertyTerm, subjectTerm = o.subjectTerm, children = o.children,
          decorations = o.decorations
        ))
        case (_, q: QueryVariable) if q.name == ref => Array[Node](ObjectOf
        (
          idRef = q.name,propertyTerm = o.propertyTerm, subjectTerm = o.subjectTerm, children = o.children,
          decorations = o.decorations
        ))
        case _ => n.children.toArray.flatMap(child => getNodeWithVariableRef(ref, child))
      }

    case node: Node if node.reference() == ref => Array[Node](node)
    case root: Root =>
      root.children.toArray.flatMap(child => getNodeWithVariableRef(ref, child)) ++
        root.lBindNode.toArray.flatMap(child => getNodeWithVariableRef(ref, child)) ++
        root.lSourcesNodes.toArray.flatMap(child => getNodeWithVariableRef(ref, child)) ++
        root.lSolutionSequenceModifierNode.toArray.flatMap(child => getNodeWithVariableRef(ref, child))
    case _ => println(s"visite:${n.idRef}"); n.children.toArray.flatMap(child => getNodeWithVariableRef(ref, child))
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
