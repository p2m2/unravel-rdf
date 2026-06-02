package facade.npm

import utest._

import scala.language.implicitConversions
import scala.scalajs.js
import scala.scalajs.js.JSON

object RdfXmlN3ParserTest extends TestSuite {
  val tests = Tests {

    test("Manually write strings to the parser") {

      val myParser = new RdfXmlParser()

      val onData: js.Function1[js.Any, Unit] = (chunk: js.Any) => {
        println(" -- data --")
        val data = chunk.asInstanceOf[Quad]
        println(JSON.stringify(data.graph))
        println(JSON.stringify(data))
      }

      val onError: js.Function1[js.Any, Unit] = (elt: js.Any) => {
        val error = elt.toString
        println(error)
      }

      val onEnd: js.Function1[js.Any, Unit] = (_: js.Any) => {
        println("All triples were parsed!")
      }

      myParser
        .on("data", onData)
        .on("error", onError)
        .on("end", onEnd)

      myParser.write("""<?xml version="1.0"?>""")
      myParser.write(
        """<rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
          |         xmlns:ex="http://example.org/stuff/1.0/"
          |         xml:base="http://example.org/triples/">""".stripMargin
      )
      myParser.write("""<rdf:Description rdf:about="http://www.w3.org/TR/rdf-syntax-grammar">""")
      myParser.write("""<ex:prop />""")
      myParser.write("""</rdf:Description>""")
      myParser.write("""</rdf:RDF>""")
      myParser.end()
    }

    test("Import streams") {
      val myParser = new RdfXmlParser()
      val myTextStream =
        Fs.createReadStream("./js/target/scala-2.13/test-classes/animals.rdf")

      val onData: js.Function1[js.Any, Unit] = (chunk: js.Any) => {
        val data = chunk.asInstanceOf[Quad]
        println(" -- data --")
        println(JSON.stringify(data.graph))
        println(JSON.stringify(data))
      }

      val onError: js.Function1[js.Any, Unit] = (elt: js.Any) => {
        val error = elt.toString
        println(error)
      }

      val onEnd: js.Function1[js.Any, Unit] = (_: js.Any) => {
        println("All triples were parsed!")
      }

      myParser
        .`import`(myTextStream)
        .on("data", onData)
        .on("error", onError)
        .on("end", onEnd)
    }
  }
}