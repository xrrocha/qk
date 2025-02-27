package qk

import org.graalvm.polyglot.*

class JavascriptTest extends munit.FunSuite:
    test("Creates request object from query string"):

        val paramMap = Map(
          "deptno" -> Seq("0010"),
          "name" -> Seq("KING", "O'HARA")
        )

        val objScript = s"""({
        deptno: parseInt(param('deptno')),
        names:  params('name').join(', ')
    })"""

        val context = Javascript.createContext()
        val result = Javascript.buildReqObj(context, objScript, paramMap)

        assertEquals(
          result.toString(),
          """{deptno: 10, names: "KING, O'HARA"}"""
        )
