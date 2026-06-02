package facade.npm

import utest._
import scala.scalajs.js

object N3StoreTest extends TestSuite {
  private val DF = N3.DataFactory

  val tests = Tests {

    val s1 = "http://ex.org/Mickey"
    val p1 = "http://ex.org/type"
    val o1 = "http://ex.org/Mouse"

    test("Storing") {
      val store = new N3Store()

      store.addQuad(
        DF.namedNode("http://ex.org/Pluto"),
        DF.namedNode("http://ex.org/type"),
        DF.namedNode("http://ex.org/Dog")
      )

      store.addQuad(
        DF.namedNode(s1),
        DF.namedNode(p1),
        DF.namedNode(o1)
      )

      store.addQuad(
        DF.namedNode(s1),
        DF.namedNode(p1),
        DF.namedNode("http://ex.org/Person")
      )

      val mickey = store.getQuads(DF.namedNode(s1), null, null)

      assert(mickey.length == 2)
      assert(mickey(0).subject.value == s1)
      assert(mickey(0).predicate.value == p1)
      assert(mickey(0).`object`.value == o1)

      val count = store.countQuads(DF.namedNode(s1), null, null)
      assert(count == 2)

      store.forEach(
        (quad: Quad) => println("forEach -> " + quad.subject.value),
        DF.namedNode(s1), null, null
      )

      assert(
        store.every(
          (quad: Quad) => true,
          DF.namedNode(s1), null, null
        )
      )

      assert(
        !store.every(
          (quad: Quad) => false,
          DF.namedNode(s1), null, null
        )
      )

      assert(
        store.some(
          (quad: Quad) => true,
          DF.namedNode(s1), null, null
        )
      )

      assert(
        !store.some(
          (quad: Quad) => false,
          DF.namedNode(s1), null, null
        )
      )

      store.`match`(DF.namedNode(s1), null, null)
        .on("data", js.Any.fromFunction1 { (chunk: Any) =>
          val src = chunk.asInstanceOf[Quad]
          println("====== on =========")
          println(src.subject.value)
        }.asInstanceOf[js.Function])
        .on("end", js.Any.fromFunction1 { (_: Any) =>
          println("All writes are now complete.")
        }.asInstanceOf[js.Function])
        .on("error", js.Any.fromFunction1 { (elt: Any) =>
          println("an error occurs : " + elt.toString)
        }.asInstanceOf[js.Function])
        .on("prefix", js.Any.fromFunction1 { args: Any =>
          val arr = args.asInstanceOf[js.Array[Any]]
          val prefix = arr(0).asInstanceOf[String]
          val iri = arr(1).asInstanceOf[String]
          println(s"prefix $prefix = $iri")
        }.asInstanceOf[js.Function])
    }

    test("Storing 2") {
      val q1 = DF.quad(
        DF.namedNode("http://ex.org/Pluto"),
        DF.namedNode("http://ex.org/type"),
        DF.namedNode("http://ex.org/Dog")
      )

      val q2 = DF.quad(
        DF.namedNode("http://ex.org/Mickey"),
        DF.namedNode("http://ex.org/type"),
        DF.namedNode("http://ex.org/Mouse")
      )

      val q3 = DF.quad(
        DF.namedNode("http://ex.org/Donald"),
        DF.namedNode("http://ex.org/type"),
        DF.namedNode("http://ex.org/Duck")
      )

      val store = new N3Store()
      store.addQuad(q1)
      store.addQuad(q2)
      store.addQuad(q3)

      assert(store.countQuads(null, null, null) == 3)
    }
  }
}