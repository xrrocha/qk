package qk

class WebServerTest extends munit.FunSuite:
  test("Builds param map from query string"):
    val queryString = "deptno=0010&name=KING&name=O'HARA"
    val paramMap = WebServer.paramMapFrom(queryString)

    assertEquals(
      paramMap,
      Map(
        "deptno" -> Seq("0010"),
        "name"   -> Seq("KING", "O'HARA")
      )
    )
