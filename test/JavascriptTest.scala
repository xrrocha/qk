package qk

import org.graalvm.polyglot.*

class JavascriptTest extends munit.FunSuite:
  test("Creates request object from query string"):

    val queryString = "deptno=0010&name=KING&name=O'HARA"
    val paramMap = WebServer.paramsFrom(queryString)

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
  
