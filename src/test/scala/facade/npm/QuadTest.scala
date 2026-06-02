package facade.npm

import utest.{TestSuite, Tests, test}

object QuadTest extends TestSuite {
  private val DF = N3.DataFactory

  val tests: Tests = Tests {
    test("Quad") {
      val q1 = DF.quad(
        DF.namedNode("test1"),
        DF.namedNode("test2"),
        DF.namedNode("test3"),
        DF.namedNode("test4")
      )

      val q2 = DF.quad(
        DF.namedNode("test1"),
        DF.namedNode("test2"),
        DF.namedNode("test3"),
        DF.namedNode("test4")
      )

      val q3 = DF.quad(
        DF.namedNode("test1"),
        DF.namedNode("test2"),
        DF.namedNode("test3")
      )

      assert(q1.equalsQuad(q2))
      assert(!q1.equalsQuad(q3))
    }
  }
}