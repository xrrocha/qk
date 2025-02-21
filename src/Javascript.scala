package qk

import org.graalvm.polyglot.*
import org.graalvm.polyglot.proxy.*

object Javascript:
  val symbol = """^\p{Alpha}[\p{Alnum}_]*$""".r
  val quotes = """[\\'"]""".r

  @main
  def doit() =
    val queryString = "deptno=10&name=KING&name=O'HARA"
    val paramMap = queryString
      .split("&")
      .toList
      .map: p =>
        val Array(name, value) = p.split("=")
        name -> quotes.replaceAllIn(value, """\\$0""")
      .filter(p => symbol.matches(p._1))
      .groupBy(p => p._1)
      .view
      .mapValues(vs => vs.map(_._2))
      .toSeq
      .map: p =>
        val (name, value) = p
        s"${name}: ${value.map(v => s"'$v'").mkString("[", ", ", "]")}"
      .mkString("{\n  ", ",\n  ", "\n}")
    val jsCode = s"""
      const paramValues = $paramMap;
      function param(name) { return paramValues[name][0]; }
      function params(name) { return paramValues[name]; }
    """

    val context = Context.create("js")
    val value = context.eval("js", jsCode)

    println(context.eval("js", """
      ({
        deptno: parseInt(param('deptno')),
        names:  params('name').join(', ')
      })
    """))
end Javascript
