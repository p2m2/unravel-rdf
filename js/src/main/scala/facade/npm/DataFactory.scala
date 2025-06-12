/**
 * olivier.filangi@inrae.fr - P2M2 Platform - https://github.com/p2m2
 */
package facade.npm

case object DataFactory {
  def namedNode( value : String ) : NamedNode = new NamedNode(value)
  def blankNode( value : String = "" ) : BlankNode = new BlankNode(value)
  def literal( value:String, language:String ) : Literal = new Literal(value,language,"http://www.w3.org/1999/02/22-rdf-syntax-ns#langString")
  def literal( value:String, datatype:NamedNode = "http://www.w3.org/2001/XMLSchema#string" ) : Literal = new Literal(value,"",datatype)
  def variable( value : String ) : Variable = new Variable(value)
  def defaultGraph( ) : DefaultGraph = new DefaultGraph()
  def quad( subject:Term, predicate:Term, `object`:Term, graph:Term =  defaultGraph( ) ): Quad =
    new Quad(subject,predicate,`object`,graph)
}
