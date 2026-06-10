package fr.inrae.metabohub.semantic_web.view

import fr.inrae.metabohub.semantic_web.node.pm.{NodeVisitor, SimpleConsole}
import fr.inrae.metabohub.semantic_web.{UnravelSession, UnravelSessionVersionAtBuildTime}
import org.scalajs.dom.document

import scala.scalajs.js
import scala.scalajs.js.Dynamic
import facade.npm._

case class HtmlView(sw: UnravelSession, regex : String = "") {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  private val limitValues = 10

  private val waitingForFuture = "[X]"

  /* count de la solution */
  var count: String = waitingForFuture
  var classes: String = waitingForFuture
  private var subjectOfObjectProperties: String = waitingForFuture
  private var subjectOfDatatypeProperties: String = waitingForFuture
  private var objectOfProperties: String = waitingForFuture
  var values: String = waitingForFuture
/*
  sw.finder.count().map(c => {
    count = c.toString
    update()
  })
*/
  sw.finder.classes(regex).map(lUris => {
    classes = " - " + lUris.mkString("\n - ")
    update()
  })

  sw.finder.objectProperties(regex).map(lUris => {
    subjectOfObjectProperties = " - " + lUris.mkString("\n - ")
    update()
  })
  sw.finder.datatypeProperties(regex).map(lUris => {
    subjectOfDatatypeProperties = " - " + lUris.mkString("\n - ")
    update()
  })

  sw.finder.subjectProperties(regex).map(lUris => {
    objectOfProperties = " - " + lUris.mkString("\n - ")
    update()
  })

  (if (regex.trim != "" ) {
    sw.filter.regex(regex)
  } else {
    sw
  })
    .select(Seq(sw.current())).limit(limitValues).commit().raw.map(resultsJson => {
    values = " - " + resultsJson("results")("bindings").arr.mkString("\n - ")
    update()
  })


  private val css = ""

  def text =
    s"""
## [Discovery](https://github.com/p2m2/Discovery)
 - build    : ${UnravelSessionVersionAtBuildTime.version}

### New step on focus

 - **target node**      : ${NodeVisitor.getNodeWithVariableRef(sw.focusNode, sw.rootNode).mkString(",")}
 - **regex**            : $regex
 - **Number of values** : **$count**

#### Values ${limitValues.toString} .set(`value`) .setList(`value1`,`value1`,..)
$values

#### Classes .isA("`uri`")

$classes

#### Forward property  .isSubjectOf("`uri`","my_reference_var")

$subjectOfObjectProperties

#### Datatype property .datatype("`uri`","my_reference_var")

$subjectOfDatatypeProperties

#### Backward property .isObjectOf("`uri`","my_reference_var")

$objectOfProperties

### configuration

${sw.config}


### Request
```
${SimpleConsole(consoleColor=false,displayRootStyle=false).get(sw.rootNode)}
```
- [Help](https://p2m2.github.io/discovery/user_docs.html)
- [declare an issue ?] (https://github.com/p2m2/discovery/issues/new)

"""


  private val options: js.Object with Dynamic = Dynamic.literal(
    "ghCodeBlocks" -> true,
    "tables" -> true,
    "strikethrough" -> false
  )

  def update(): Any = {
    val converter = new ShowdownConverter(options)
    val html: String = converter.makeHtml(css + text)
    document.querySelector("html").innerHTML = html
  }

  update()
}
