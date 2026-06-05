package fr.inrae.metabohub.semantic_web

import fr.inrae.metabohub.data.DataTestFactory
import fr.inrae.metabohub.semantic_web.node.{Node, Root}
import fr.inrae.metabohub.semantic_web.rdf.URI
import fr.inrae.metabohub.semantic_web.configuration._
import utest._

import scala.language.postfixOps

object UnravelSessionDecorationTest extends TestSuite {

  val config: UnravelConfig = DataTestFactory.getConfigVirtuoso1()

  override def utestAfterAll(): Unit = {
    DataTestFactory.deleteVirtuoso1(this.getClass.getSimpleName)
  }

  def startRequest: UnravelSession =
    UnravelSession(config)
      .graph(DataTestFactory.graph1(this.getClass.getSimpleName))
      .something("h1")

  def tests = Tests {

    test("setDecoratingAttribute Root") {
      assert(
        startRequest
          .root
          .setDecoration("someKey","someValue")
          .browse(
            (n : Node,deep: Integer)=> {
              n.decorations
            }
          ).filter(_.nonEmpty) == List(Map("someKey"->"someValue")))

    }

    test("setDecoratingAttribute Root with children") {
      val m =
        startRequest
          .root.setDecoration("someKey","someValue")
          .from("h1", h1 => h1.setDecoration("someKey2","someValue2"))
          .root.setDecoration("someKey3","someValue3")
          .browse(
            (n : Node,deep: Integer) => n match {
              case _ : Root => {
                "root" -> n.decorations
              }
              case _ => {
                n.idRef -> n.decorations
              }
            }
          ).toMap

      assert( m("root") == Map("someKey"->"someValue","someKey3"->"someValue3") )
      assert( m("h1") == Map("someKey2"->"someValue2") )

    }

    test("setDecoratingAttribute basic") {

      assert(startRequest
        .setDecoration("someKey","someValue")
        .browse(
          (n : Node,deep: Integer)=> {
            n.decorations
          }
        ).filter(_.nonEmpty) == List(Map("someKey"->"someValue")))

    }

    test("setDecoratingAttribute") {
     val m =
        startRequest
          .from("h1",h1 =>
            h1.setDecoration("someKey","someValue")
            .isObjectOf(URI("http://s2"),"s2"))
          .from("s2", s2 =>
            s2.setDecoration("someKey2","someValue2")
            .isObjectOf(URI("http://s3"),"s3"))
          .from("s3", s3 => s3.setDecoration("someKey3","someValue3"))
          .browse(
            (n : Node,deep: Integer)=> {
              n.idRef -> n.decorations
            }
          ).toMap

      assert( m("h1") == Map("someKey"->"someValue") )
      assert( m("s2") == Map("someKey2"->"someValue2") )
      assert( m("s3") == Map("someKey3"->"someValue3") )
     }

    test("setDecoratingAttribute/getDecoration") {
      (startRequest
        .setDecoration("someKey","someValue")
        .getDecoration("someKey") == "someValue")
    }

    test("setDecoratingAttribute/getDecoration 2") {
      (startRequest
        .getDecoration("someKey") == "")
    }

    test("setDecoratingAttribute/getDecoration 3") {
      startRequest
        .from("h1", h1 =>
          h1.setDecoration("someKey", "someValue")
            .isObjectOf("http://some", s => {
              assert(s.getDecoration("someKey") == "")
              s
            }))
    }

    test("setDecoratingAttribute/getDecoration 4") {
      (startRequest
        .from("h1",h1 =>
          h1.isObjectOf("http://some","something",
              s=>s.setDecoration("someKey","someValue")))
        .from("h1",h1 => h1.isObjectOf("http://some"))
        .from("something", s=> {assert(s.getDecoration("someKey") == "someValue");s}))

    }

    test("setDecoratingAttribute/getDecoration using datatype") {
      assert(UnravelSession(config)
        .setDecoration("someKeyRoot","someValueRoot")
        .something("start")
        .datatype("http://something","d")
        .root
        .getDecoration("someKeyRoot") == "someValueRoot")
    }
  }
}
