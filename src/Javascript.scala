package qk

import org.graalvm.polyglot.*
import org.graalvm.polyglot.proxy.*

object Javascript:
  @main
  def main() =
    val queryString = "deptno=0010&name=KING&name=O'HARA"
    val paramMap = paramsFrom(queryString)

    val objScript = s"""({
        deptno: parseInt(param('deptno')),
        names:  params('name').join(', ')
    })"""

    val context = Context.create("js")
    val result = buildReqObj(context, objScript, paramMap)
    assert("""{deptno: 10, names: "KING, O'HARA"}""" == result.toString())

  def buildReqObj(
    context: Context,
    objScript: String,
    paramMap: Map[String, Seq[String]]
  ): Any =

    val paramsObj = paramMap
      .toSeq
      .map: p =>
        val (name, value) = p
        s"${name}: ${value.map(v => s"'$v'").mkString("[", ", ", "]")}"
      .mkString("{\n  ", ",\n  ", "\n}")

    val paramDefs = s"""
      const paramValues = $paramsObj;
      function param(name) { return paramValues[name][0]; }
      function params(name) { return paramValues[name]; }
    """

    context.eval("js", s"$paramDefs\n$objScript")
  end buildReqObj

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
  end paramsFrom

  val symbol = """^\p{Alpha}[\p{Alnum}_]*$""".r
  def isSymbol(s: String) = symbol.matches(s)

  val quotes = """[\\'"]""".r
  def escape(s: String) = quotes.replaceAllIn(s, """\\$0""")
end Javascript
