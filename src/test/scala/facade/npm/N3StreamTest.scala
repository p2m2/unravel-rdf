// SPDX-FileCopyrightText: 2020-2026 INRAE
// SPDX-License-Identifier: GPL-3.0-or-later

package facade.npm

import scala.scalajs.js
import scala.scalajs.js.annotation._
import utest._

@js.native
@JSImport("fs", JSImport.Namespace)
object Fs extends js.Object {
  def createReadStream(path: String): JsReadableStream = js.native
}

object N3StreamTest extends TestSuite {

  val tests = Tests {
    val stdout = js.Dynamic.global.process.stdout.asInstanceOf[js.Any]

    test("stream parser") {
      val streamParser = new N3StreamParser(null)
      val inputStream = Fs.createReadStream("src/test/resources/example.ttl")
      val streamWriter = new N3StreamWriter(
        N3Options(prefixes = Map("c" -> "http://example.org/cartoons#"))
      )

      inputStream.pipe(streamParser)
      streamParser.pipe(streamWriter)
      streamWriter.pipe(stdout)
    }
  }
}