package fr.inrae.metabohub.semantic_web.driver

import facade.npm._
import fr.inrae.metabohub.semantic_web.configuration.SourcePath
import fr.inrae.metabohub.semantic_web.configuration.SourcePath.SourcePath
import fr.inrae.metabohub.semantic_web.driver.ComunicaRequestDriver.SourceComunica
import fr.inrae.metabohub.semantic_web.exception.UnravelException
import fr.inrae.metabohub.semantic_web.sparql.QueryResult

import scala.concurrent.{Future, Promise}
import scala.scalajs.js
import scala.scalajs.js.|
import scala.util.{Failure, Success, Try}

object ComunicaRequestDriver {
  implicit val ec: scala.concurrent.ExecutionContext =
    scala.scalajs.concurrent.JSExecutionContext.queue

  type SourceComunica = String | SourceDefinitionNewQueryEngine | N3Store

  def sourceFromUrl(url: String, mimetype: String): SourceDefinitionNewQueryEngine = {
    SourceDefinitionNewQueryEngine(
      `type` = mimetype match {
        case "application/sparql-query" => SourceType.sparql
        case "hypermedia"               => SourceType.hypermedia
        case _                          => SourceType.file
      },
      url
    )
  }

  def sourceFromContentN3Parser(content: String, mimetype: String): Future[N3Store] = {
    val store = new N3Store()
    val p = Promise[N3Store]()

    val format = mimetype match {
      case "text/turtle"           => N3FormatOption.Turtle
      case "text/n3"               => N3FormatOption.N3
      case "application/n-triples" => N3FormatOption.N3
      case _             => throw UnravelException(s"$mimetype format is not managed")
    }

    println("==== sourceFromContentN3Parser ====")
    println(s"mimetype = $mimetype")
    println(s"format   = $format")
    println("content:")
    println(content)
    println("===================================")

    var quadCount = 0

    val parser = new N3Parser(
      N3Options(
        baseIRI = "http://com.github.p2m2.discovery/",
        format = format
      )
    )

    parser.parse(
      content,
      (
        error: js.UndefOr[js.Error],
        quad: js.UndefOr[Quad],
        prefixes: js.UndefOr[js.Dictionary[String]]
      ) => {
        if (!js.isUndefined(error) && error != null) {
          val e = error.get
          val msg =
            if (js.isUndefined(e.message) || e.message == null) e.toString
            else e.message.toString

          println("==== N3 PARSER ERROR ====")
          println(msg)
          println("=========================")

          p.tryFailure(UnravelException(msg))
        } else if (js.isUndefined(quad) || quad == null) {
          println("==== N3 PARSER END ====")
          println(s"quadCount = $quadCount")
          println(s"store.countQuads(null, null, null) = ${store.countQuads(null, null, null)}")

          val all = store.getQuads(null, null, null)
          var i = 0
          while (i < all.length) {
            val q = all(i)
            println(s"STORE[$i] s=${q.subject.value} p=${q.predicate.value} o=${q.`object`.value}")
            i += 1
          }
          println("=======================")

          p.trySuccess(store)
        } else {
          val q = quad.get
          quadCount += 1

          println(s"PARSED[$quadCount] s=${q.subject.value} p=${q.predicate.value} o=${q.`object`.value}")
          store.addQuad(q)
        }
      }
    )

    p.future
  }

  def sourceFromContentRdfXml(content: String): Future[N3Store] = {
    val store = new N3Store()
    val p = Promise[N3Store]()

    val parser = new RdfXmlParser(
      RdfXmlParserOptions(baseIRI = "http://com.github.p2m2.discovery/")
    )

    parser
      .on("data", (chunk: js.Any) => {
        val quad = chunk.asInstanceOf[Quad]
        store.addQuad(quad)
      })
      .on("error", (elt: js.Any) => {
        val error = elt.asInstanceOf[String]
        p.tryFailure(UnravelException(error))
      })
      .on("end", (_: js.Any) => {
        p.trySuccess(store)
      })

    parser.write(content)
    parser.end()

    p.future
  }

  def sourceFromContent(content: String, mimetype: String): Future[N3Store] = {
    mimetype match {
      case "text/rdf-xml" => sourceFromContentRdfXml(content)
      case _              => sourceFromContentN3Parser(content, mimetype)
    }
  }

  def requestOnSWDBWithSources(
    query: String,
    sources: List[SourceComunica]
  ): Future[QueryResult] = {
    Try {
      new QueryEngine()
        .queryBindings(
          query,
          QueryEngineOptions(
            sources = sources,
            lenient = false
          )
        )
        .toFuture
        .flatMap { bindingsStream =>
          val p = Promise[QueryResult]()
          val vars = scala.collection.mutable.LinkedHashSet[String]()
          val rows = scala.collection.mutable.ArrayBuffer[ujson.Value]()

          bindingsStream
            .on(
              "data",
              js.Any.fromFunction1 { (bindingAny: Any) =>
                val row = ujson.Obj()

                val iterable =
                  bindingAny.asInstanceOf[js.Iterable[js.Array[js.Any]]]

                val arr =
                  js.Array.from(iterable).asInstanceOf[js.Array[js.Array[js.Any]]]

                var j = 0
                while (j < arr.length) {
                  val pair = arr(j)
                  val variable = pair(0).asInstanceOf[js.Dynamic]
                  val term = pair(1).asInstanceOf[js.Dynamic]

                  val varName =
                    if (!js.isUndefined(variable.value) && variable.value != null)
                      variable.value.toString
                    else
                      variable.toString.replace("?", "")

                  vars += varName

                  val termType = term.termType.toString
                  val termObj = ujson.Obj(
                    "type" -> (termType match {
                      case "NamedNode" => "uri"
                      case "BlankNode" => "bnode"
                      case "Literal"   => "literal"
                      case other       => other
                    }),
                    "value" -> term.value.toString
                  )

                  if (termType == "Literal") {
                    if (
                      !js.isUndefined(term.language) &&
                      term.language != null &&
                      term.language.toString.nonEmpty
                    ) {
                      termObj("xml:lang") = term.language.toString
                    } else if (
                      !js.isUndefined(term.datatype) &&
                      term.datatype != null &&
                      !js.isUndefined(term.datatype.value)
                    ) {
                      termObj("datatype") = term.datatype.value.toString
                    }
                  }

                  row(varName) = termObj
                  j += 1
                }

                rows += row
              }
            )
            .on(
              "error",
              js.Any.fromFunction1 { (err: Any) =>
                p.tryFailure(UnravelException(err.toString))
              }
            )
            .on(
              "end",
              js.Any.fromFunction1 { (_: Any) =>
                val json = ujson.Obj(
                  "head" -> ujson.Obj(
                    "vars" -> ujson.Arr.from(vars.toSeq.map(ujson.Str(_)))
                  ),
                  "results" -> ujson.Obj(
                    "bindings" -> ujson.Arr.from(rows)
                  )
                )

                p.trySuccess(QueryResult(json.render()))
              }
            )

          p.future
        }
    } match {
      case Success(result) => result
      case Failure(e)      => throw UnravelException(e.toString)
    }
  }
}

case class ComunicaRequestDriver(
  idName: String,
  path: String,
  sourcePath: SourcePath,
  mimetype: String,
  login: Option[String] = None,
  password: Option[String] = None
) extends RequestDriver {

  def requestOnSWDB(query: String): Future[QueryResult] = {
    (sourcePath match {
      case SourcePath.UrlPath =>
        Future {
          ComunicaRequestDriver.sourceFromUrl(path, mimetype)
        }

      case SourcePath.Content =>
        ComunicaRequestDriver.sourceFromContent(path, mimetype)
    }).asInstanceOf[Future[SourceComunica]]
      .flatMap { source =>
        ComunicaRequestDriver.requestOnSWDBWithSources(query, List(source))
      }
  }
}