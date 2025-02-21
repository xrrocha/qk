package qk

import org.graalvm.polyglot.*
import org.graalvm.polyglot.proxy.*

object Javascript:
  @main
  def main() =
    val queryString = "deptno=10&name=KING&name=O'HARA"
    val paramJsObj = paramsFrom(queryString)
      .toSeq
      .map: p =>
        val (name, value) = p
        s"${name}: ${value.map(v => s"'$v'").mkString("[", ", ", "]")}"
      .mkString("{\n  ", ",\n  ", "\n}")
    val jsCode = s"""
      const paramValues = $paramJsObj;
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

  def paramsFrom(queryString: String) =
    queryString
      .split("&")
      .toSeq
      .map: p =>
        val Array(name, value) = p.split("=")
        name -> escape(value)
      .filter(p => isSymbol(p._1))
      .groupBy(p => p._1)
      .view
      .mapValues(vs => vs.map(_._2))
      .toMap

  val symbol = """^\p{Alpha}[\p{Alnum}_]*$""".r
  def isSymbol(s: String) = symbol.matches(s)

  val quotes = """[\\'"]""".r
  def escape(s: String) = quotes.replaceAllIn(s, """\\$0""")
end Javascript
