package qk

import Javascript.*
import org.graalvm.polyglot.*
import scala.jdk.CollectionConverters.*

class JavascriptTest extends munit.FunSuite:
    test("Creates request object from query string"):

        val paramMap = Map(
          "deptno" -> Seq("0010"),
          "name" -> Seq("KING", "O'HARA")
        )

        val objScript = s"""({
            |deptno: parseInt(param('deptno')),
            |names:  params('name').join(', ')
            |})""".stripMargin

        val context = Javascript.createContext()
        val reqObj = Javascript.buildReqObj(context, objScript, paramMap)

        assertEquals(
          reqObj.toString(),
          """{deptno: 10, names: "KING, O'HARA"}"""
        )
        
        val bindings = context.getBindings("js")
        reqObj.getMemberKeys().forEach:mn =>
          bindings.putMember(mn, reqObj.getMember(mn))

        assertEquals(context.eval("js", "deptno").asInt(), 10)
        assertEquals(context.eval("js", "names").asString(), "KING, O'HARA")

        val sql = """
          |SELECT   *
          |FROM     emp
          |WHERE    deptno = :deptno
          |   OR    ename IN (${names})
          |ORDER BY ename
        """.stripMargin
        // TODO Account for quote escaping
        println(escapeQuote(context.eval("js", s"`$sql`").asString()))
end JavascriptTest
