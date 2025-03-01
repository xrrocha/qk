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

        val objScript = """({
            |deptno: parseInt(param('deptno')),
            |names:  params('name')
            |            .map(name => `'${name.replace("'", "''")}'`)
            |            .join(', ')
            |})""".stripMargin

        val context = Javascript.createContext()
        val eval = context.eval("js", _)

        val reqObj = Javascript.buildReqObj(context, objScript, paramMap)

        assertEquals(
          reqObj.toString(),
          """{deptno: 10, names: "'KING', 'O''HARA'"}"""
        )
        
        val bindings = context.getBindings("js")
        reqObj.getMemberKeys().forEach:mn =>
          bindings.putMember(mn, reqObj.getMember(mn))


        assertEquals(eval("names").asString(), "'KING', 'O''HARA'")

        val sql = """
          |SELECT   *
          |FROM     emp
          |WHERE    deptno = :deptno
          |   OR    ename IN (${names})
          |ORDER BY ename
        """.stripMargin
        val sqlStr = eval(s"`$sql`").asString()

        // TODO Account for "`" quote escaping in sql
        assertEquals("""
          |SELECT   *
          |FROM     emp
          |WHERE    deptno = :deptno
          |   OR    ename IN ('KING', 'O''HARA')
          |ORDER BY ename
          """.stripMargin.trim,
          sqlStr.trim
        )
end JavascriptTest
