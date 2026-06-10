package fr.inrae.metabohub.semantic_web

import fr.inrae.metabohub.semantic_web.configuration._
import fr.inrae.metabohub.semantic_web.exception._
import fr.inrae.metabohub.semantic_web.node._
import fr.inrae.metabohub.semantic_web.node.pm.{NodeVisitor, RemoveNode}
import fr.inrae.metabohub.semantic_web.rdf._
import wvlet.log.Logger
import wvlet.log.Logger.rootLogger._

import java.util.UUID.randomUUID
import scala.concurrent.Future
import io.lemonlabs.uri.{QueryString, Url}

object UnravelSession {

  private val version: String = UnravelSessionVersionAtBuildTime.version

  implicit val rw: OptionPickler.ReadWriter[UnravelSession] = OptionPickler.macroRW

  info(" --------------------------------------------------")
  info(" ---- Discovery :" + UnravelSession.version + "         -----------")
  info(" --------------------------------------------------")

}

case class UnravelSession(
                           config: UnravelConfig = UnravelConfig.init(),
                           rootNode: Root = Root(),
                           fn: Option[String] = None) {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  val focusNode: String = fn match {
    case Some(v) => v
    case None => rootNode.reference()
  }

  case class FilterIncrement(negation: Boolean = false) {

    private def manageFilter(n: Node, forward: Boolean = false): UnravelSession = focusManagement(n)

    def isLiteral: UnravelSession = manageFilter(
      fr.inrae.metabohub.semantic_web.node.isLiteral(this.negation, getUniqueRef()))

    def isUri: UnravelSession = manageFilter(
      fr.inrae.metabohub.semantic_web.node.isURI(this.negation, getUniqueRef()))

    def isBlank: UnravelSession = manageFilter(
      fr.inrae.metabohub.semantic_web.node.isBlank(this.negation, getUniqueRef()))

    /* strings */
    def regex(pattern: SparqlDefinition, flags: SparqlDefinition = ""): UnravelSession =
      manageFilter(Regex(pattern, flags, this.negation, getUniqueRef()))

    def contains(string: SparqlDefinition): UnravelSession = manageFilter(Contains(string, this.negation, getUniqueRef()))

    def strStarts(string: SparqlDefinition): UnravelSession = manageFilter(StrStarts(string, this.negation, getUniqueRef()))

    def strEnds(string: SparqlDefinition): UnravelSession = manageFilter(StrEnds(string, this.negation, getUniqueRef()))

    /* numeric */
    def equal(value: SparqlDefinition): UnravelSession = manageFilter(Equal(value, this.negation, getUniqueRef()))

    def notEqual(value: SparqlDefinition): UnravelSession = manageFilter(NotEqual(value, this.negation, getUniqueRef()))

    def inf(value: SparqlDefinition): UnravelSession = manageFilter(Inf(value, this.negation, getUniqueRef()))

    def infEqual(value: SparqlDefinition): UnravelSession = manageFilter(InfEqual(value, this.negation, getUniqueRef()))

    def sup(value: SparqlDefinition): UnravelSession = manageFilter(Sup(value, this.negation, getUniqueRef()))

    def supEqual(value: SparqlDefinition): UnravelSession = manageFilter(SupEqual(value, this.negation, getUniqueRef()))

    def not: FilterIncrement = {
      FilterIncrement(true)
    }
  }

  def filter: FilterIncrement = FilterIncrement()

  case class BindIncrement(`var`: String, f: UnravelSession => UnravelSession = identity) {
    def manage(n: ExpressionNode, forward: Boolean = true): UnravelSession = {
      val unravelSess = focusManagement(Bind(n, `var`))
      UnravelSession(unravelSess.config, unravelSess.rootNode, Some(`var`))
    }

    /* primary expression */

    /* String fun */
    def subStr(startingLoc: SparqlDefinition, length: SparqlDefinition): UnravelSession = manage(SubStr(startingLoc, length, getUniqueRef()))

    def replace(pattern: SparqlDefinition, replacement: SparqlDefinition, flags: SparqlDefinition = ""): UnravelSession =
      manage(Replace(pattern, replacement, flags, getUniqueRef()))

    /* Numeric  fun */
    def abs(): UnravelSession = manage(Abs(getUniqueRef()))

    def round(): UnravelSession = manage(Round(getUniqueRef()))

    def ceil(): UnravelSession = manage(Ceil(getUniqueRef()))

    def floor(): UnravelSession = manage(Floor(getUniqueRef()))

    def rand(): UnravelSession = manage(Rand(getUniqueRef()))

    /* uri fun */
    def datatype(): UnravelSession = manage(Datatype(getUniqueRef()))

    def str(): UnravelSession = manage(Str(QueryVariable(`var`), getUniqueRef()))
  }

  def bind(`var`: String, f: UnravelSession => UnravelSession = identity): BindIncrement = BindIncrement(`var`, f)

  //private val logger = Logger.of[UnravelSession]
  // Set the root logger's log level
  Logger.setDefaultLogLevel(config.settings._logLevel)

  /* set focus on root */
  def root: UnravelSession = UnravelSession(config, rootNode, Some(rootNode.reference()))

  def finder: UnravelSessionHelper = UnravelSessionHelper(this)

  /* configuration */
  def setConfig(newConfig: UnravelConfig): UnravelSession =
    UnravelSession(newConfig, rootNode, Some(focusNode))

  def getConfig: UnravelConfig = config

  /* get current focus */
  def current(): String = focusNode

  def focus(ref: String): UnravelSession = {
    if (ref == focusNode) {
      UnravelSession(config, rootNode, fn)
    } else if (ref == rootNode.idRef) {
      root
    } else {
      pm.NodeVisitor.getNodeWithVariableRef(ref, rootNode).lastOption match {
        case Some(node) => UnravelSession(config, rootNode, Some(node.reference()))
        case None => throw UnravelException(s"$ref does not exist.")
      }
    }
  }

  private def positionOn(ref: String): UnravelSession = {
    if (ref == root.rootNode.idRef)
      UnravelSession(config, rootNode, Some(root.rootNode.idRef))
    else {
      val node = pm.NodeVisitor.getNodeWithVariableRef(ref, rootNode).lastOption
        .getOrElse(throw UnravelException(s"$ref does not exist."))
      UnravelSession(config, rootNode, Some(node.reference()))
    }
  }

  def from(ref: String, f: UnravelSession => UnravelSession = identity): UnravelSession =
    f(positionOn(ref)).copy(fn = this.fn)

  def from[A](ref: String, f: UnravelSession => Future[A]): Future[A] =
    f(positionOn(ref))

  def refExist(ref: String): UnravelSession = {

    pm.NodeVisitor.getNodeWithVariableRef(ref, rootNode).lastOption match {
      case Some(_) => UnravelSession(config, rootNode, Some(focusNode))
      case None => throw UnravelException(s"$ref does not exist.")
    }
  }

  def prefix(short: String, long: IRI): UnravelSession = UnravelSession(config, rootNode.addPrefix(short, long), Some(focusNode))

  def directive(directive: String): UnravelSession = UnravelSession(config, rootNode.addDirective(directive), Some(focusNode))

  def prefixes(lPrefixes: Map[String, IRI]): UnravelSession =
    (lPrefixes map { case (key, value) => prefix(key, value)
    }).toSeq match {
      case l if l.nonEmpty => l.last
      case _ => this
    }

  def getPrefix(short: String): IRI = rootNode.getPrefix(short)

  def getPrefixes(): Map[String, IRI] = rootNode.getPrefixes

  def graph(graph: IRI): UnravelSession = UnravelSession(config, rootNode.addDefaultGraph(graph), Some(focusNode))

  def namedGraph(graph: IRI): UnravelSession = UnravelSession(config, rootNode.addNamedGraph(graph), Some(focusNode))

  def focusManagement(n: Node): UnravelSession = {
    // get all node
    println(s"focus management current:$focusNode")
    val current =  pm.NodeVisitor.getNodeWithVariableRef(focusNode, rootNode).lastOption
      .getOrElse(throw UnravelException(s"$focusNode does not exist.\n${pm.SimpleConsole().get(rootNode)}"))
    println(s"find :${current.idRef}")
    if (current.accept(n)) {
      val newRootNode = rootNode.addChildren(focusNode, n)
      /* with lambdas enclosure the focus is "this".focusNode */
      UnravelSession(config, newRootNode, Some(this.focusNode))
    } else {
      throw UnravelException(
        s"""
           |Invalid semantic structure.
           |
           |Attempted to add:
           |  $n
           |
           |Current focus:
           |  ${this.focusNode} // type=${current.getClass.getSimpleName}
           |
           |The current focus does not accept nodes of type '${n.getClass.getSimpleName}'.
           |
           |This usually means that:
           |  - the focus is positioned on the wrong node,
           |  - an intermediate node is missing,
           |  - or the current branch must be closed before adding this node.
           |
           |Full focus path:
           |  $current
           |""".stripMargin)
    }
  }

  def getUniqueRef(baseNameVar: String = ""): String = {
    baseNameVar + (baseNameVar match {
      case "object" => rootNode.getChild(ObjectOf("", URI(""), QueryVariable(""))).length
      case "subject" => rootNode.getChild(SubjectOf("", URI(""), QueryVariable(""))).length
      case "something" => rootNode.getChild(Something("")).length
      case "datatype" => rootNode.getChild(DatatypeNode("", SubjectOf("", URI(""), QueryVariable("")), "")).length
      case _ => randomUUID.toString
    }).toString
  }

  /* start a request with a variable */
  def something(ref: String = getUniqueRef("something"), f: UnravelSession => UnravelSession): UnravelSession = {
    debug(" -- something -- ")
    val focus_current = focusNode
    val withSomething = root.focusManagement(Something(ref))
    f(withSomething.copy(fn = Some(ref))).copy(fn = Some(focus_current))
  }

  def something(): UnravelSession = something(getUniqueRef("something"), identity);

  def something(ref: String): UnravelSession = something(ref, identity);

  def something(f: UnravelSession => UnravelSession): UnravelSession = something(getUniqueRef("something"), f);

  /* create node which focus is the subject : ?focusId <uri> ?target */
  private def _isSubjectOf(
                           propertyTerm: SparqlDefinition,
                           ref: String,
                           objectTerm: SparqlDefinition,
                           f: UnravelSession => UnravelSession
                         ): UnravelSession = {

    val focus_current = focusNode
    val inner = focusManagement(SubjectOf(ref, propertyTerm, objectTerm))

    f(inner.copy(fn = Some(ref))).copy(fn = Some(focus_current))
  }

  // Méthode principale avec paramètre nommé
  def isSubjectOf(
                   propertyTerm: SparqlDefinition,
                   objectTermSparql: SparqlDefinition = null,
                   apply: UnravelSession => UnravelSession = identity
                 ): UnravelSession = {

   if (objectTermSparql != null) {
      // Cas SparqlDefinition
      objectTermSparql match {
        case QueryVariable(id) =>
          _isSubjectOf(propertyTerm, id, objectTermSparql, apply)
        case _ =>
          _isSubjectOf(propertyTerm, getUniqueRef("object"), objectTermSparql, apply)
      }
    } else {
      val id = getUniqueRef("object")
      val objectTermVar = QueryVariable(id)
      _isSubjectOf(propertyTerm, id, objectTermVar, apply)
    }
  }

  //---------------------------------------------------------------------------------------------------------


  /* create node which focus is the object : ?target <uri> ?focusId */
  private def _isObjectOf(
                  propertyTerm: SparqlDefinition,
                  ref: String,
                  subjectTerm: SparqlDefinition,
                  f: UnravelSession => UnravelSession
                ): UnravelSession = {
    val focus_current = focusNode
    val inner = focusManagement(ObjectOf(ref, propertyTerm, subjectTerm))

    f(inner.copy(fn = Some(ref))).copy(fn = Some(focus_current))
  }

  def isObjectOf(
                  propertyTerm: SparqlDefinition,
                  subjectTermSparql: SparqlDefinition = null,
                  apply: UnravelSession => UnravelSession = identity
                 ): UnravelSession = {

    if (subjectTermSparql != null) {
      // Cas SparqlDefinition
      subjectTermSparql match {
        case QueryVariable(id) =>
          _isObjectOf(propertyTerm, id, subjectTermSparql, apply)
        case _ =>
          _isObjectOf(propertyTerm, getUniqueRef("subject"), subjectTermSparql, apply)
      }
    } else {
      val id = getUniqueRef("subject")
      val subjectTermSparql = QueryVariable(id)
      _isObjectOf(propertyTerm, id, subjectTermSparql, apply)
    }
  }

  /*
  create node which focus is typed with <uri>:
  ?focusId a <uri>
  */
  def isA(term: SparqlDefinition): UnravelSession =
    isSubjectOf(URI("rdf:type"), term)

  /*
  Get attribute value of an object.
  return Sw with the old focus
  Attribute value is optional
  */

  def datatype(uri: URI, ref: String): UnravelSession = {

    val focus_current = focusNode

    val withDatatype = root.focusManagement(
      DatatypeNode(
        focus_current, // parent = le SubjectOf déjà créé
        SubjectOf(ref, uri, QueryVariable(ref)), // le SubjectOf que l’on veut typer
        ref
      )
    )

    // 4️⃣  Retourner une nouvelle session dont le focus est ce Something.
    withDatatype.copy(fn = Some(focus_current))
  }

  def set(term: SparqlDefinition): UnravelSession =
    focusManagement(Value(term))

  def setList(terms: Seq[SparqlDefinition]): UnravelSession = focusManagement(ListValues(terms))


  def remove(focus: String): UnravelSession =
    UnravelSession(config, RemoveNode.run(rootNode, focus),
      Some(
        NodeVisitor.getAncestorsRef(focusNode, rootNode) match {
          case l if l.length > 1 => l(l.length - 2)
          case _ => rootNode.idRef
        }))

  def getSerializedString: String = OptionPickler.write(this)

  def setSerializedString(query: String): UnravelSession = OptionPickler.read[UnravelSession](query)


  def console: UnravelSession = {
    debug(" -- console -- ")

    println(
      "═══════════════════════════════════════════════════════════\n" +
        "  USER REQUEST\n" +
        "═══════════════════════════════════════════════════════════\n" +
        pm.SimpleConsole().get(rootNode) + "\n" +

        "═══════════════════════════════════════════════════════════\n" +
        "  FOCUS NODE\n" +
        "═══════════════════════════════════════════════════════════\n" +
        focusNode + "\n" +

        "═══════════════════════════════════════════════════════════\n" +
        "  SOURCE\n" +
        "═══════════════════════════════════════════════════════════\n" +
        config.sources.map(v => v.path).mkString(",\n") + "\n" +

        "\n═══════════════════════════════════════════════════════════\n" +
        "  HTTP REQUESTS\n" +
        "═══════════════════════════════════════════════════════════\n" +

        "\n  ┌─ HTTP GET ──────────────────────────────────────────\n" +
        "  │\n" +
        sparql_get + "\n" +
        "  └─────────────────────────────────────────────────────\n" +

        "\n  ┌─ HTTP CURL ─────────────────────────────────────────\n" +
        "  │\n" +
        sparql_curl + "\n" +
        "  └─────────────────────────────────────────────────────\n" +

        "\n═══════════════════════════════════════════════════════════\n" +
        "  END OF CONSOLE\n" +
        "═══════════════════════════════════════════════════════════\n"
    )

    this
  }

  def sparql: String = SparqlQueryBuilder.selectQueryString(rootNode).trim

  def sparql_get: String =
    (config.sources.length match {
      case 1 => config.sources.head.path
      case _ => ""
    }) + Url(path = "", query = QueryString.fromPairs(
      "query" -> sparql,
      "format" -> "json")
    )

  def sparql_curl: String =
    "curl -H \"Accept: application/json\" -G " + (config.sources.length match {
      case 1 => config.sources.head.path
      case _ => ""
    }) + " --data-urlencode query='" + sparql + "'"

  /**
   * Discovery request
   *
   */
  def transaction = UnravelQuery(this)

  /**
   * Return solutions as Future corresponding with the current Node request.
   *
   * @param lRef   : selected variables
   * @param limit  : upper bound on the number of solutions returned
   * @param offset : solution are generated after this offset
   * @return
   */
  def select(lRef: Seq[String] = List("*"), limit: Int = 0, offset: Int = 0): UnravelQuery =
    transaction
      .limit(limit)
      .offset(offset)
      .projection(lRef)

  /**
   * Give an iterable object to browse and obtain all solution performed by a select.
   *
   * @param lRef
   * @return iterable on select function
   */
  def selectByPage(lRef: Seq[String] = List("*")): Future[(Int, Seq[UnravelQuery])] = {
    // remove datatype node ref
    val lDatatypeRef = rootNode.lDatatypeNode.map(ldn => ldn.idRef)
    UnravelSessionHelper(this).count(lRef.filter(!lDatatypeRef.contains(_))).map {
      case nSolutions if nSolutions == 0 => (nSolutions, Seq())
      case nSolutions => val nit: Int = (nSolutions + config.settings.pageSize - 1) / config.settings.pageSize
        (nSolutions, (0 to nit).map(p => {
          val limit = config.settings.pageSize
          val offset = p * config.settings.pageSize
          select(lRef, limit, offset)
        }))
    }
  }


  /**
   * Give an iterable object to browse and obtain all solution performed by a select distinct.
   *
   * @param lRef : selected variables
   * @return iterable on select function
   */
  def selectDistinctByPage(lRef: Seq[String] = List("*")): Future[(Int, Seq[UnravelQuery])] = {
    val lDatatypeRef = rootNode.lDatatypeNode.map(ldn => ldn.idRef)

    UnravelSessionHelper(this).count(lRef.filter(!lDatatypeRef.contains(_)), distinct = true).map {
      case nSolutions if nSolutions == 0 => (nSolutions, Seq())
      case nSolutions =>
        val nit: Int = (nSolutions + config.settings.pageSize - 1) / config.settings.pageSize
        (nSolutions, (0 to nit).map(p => {
          val limit = config.settings.pageSize
          val offset = p * config.settings.pageSize
          select(lRef, limit, offset).distinct
        }))
    }
  }


  def browse[A](visitor: (Node, Integer) => A): Seq[A] = NodeVisitor.map(rootNode, 0, visitor)

  def setDecoration(key: String, value: String): UnravelSession = {
    val v = rootNode
      .getChild[Node](rootNode.asInstanceOf[Node])
      .filter(_.idRef == focusNode)
      .lastOption match {
      case Some(n) if n.isInstanceOf[Root] =>
        UnravelSession(config, rootNode.addDecoratingAttribute(key, value).asInstanceOf[Root], Some(rootNode.reference()))
      case Some(n) =>
        val sw = remove(focusNode)
        sw.focusManagement(n.addDecoratingAttribute(key, value))
      case None => throw UnravelException(s"Can not reach current node -- $focusNode --]")
    }
    UnravelSession(v.config, v.rootNode, Some(this.focusNode))
  }

  def getDecoration(key: String): String = {
    rootNode
      .getChild[Node](rootNode.asInstanceOf[Node])
      .filter(_.idRef == focusNode)
      .lastOption match {
      case Some(n) =>
        n.decorations.getOrElse(key, "")
      case None => ""
    }
  }
}
