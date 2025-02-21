package qk

import org.graalvm.polyglot.*
import org.graalvm.polyglot.proxy.*

object Javascript:
  val symbol = """^\p{Alpha}[\p{Alnum}_]*$""".r
  val quotes = """[\\'"]""".r

  @main
  def doit() =
    val queryString = "p1=abc&p1=def&p2=o'hara"
    val paramMap = queryString
      .split("&")
      .toList
      .map: p =>
        val List(name, value) = p.split("=").toList
        name -> quotes.replaceAllIn(value, """\\$0""")
      .groupBy(p => p._1)
      .view
      .mapValues(vs => vs.map(_._2))
      .toSeq
      .map: p =>
        val (name, value) = p
        s"${name}: ${value.map(v => s"'$v'").mkString("[", ", ", "]")}"
      .mkString("{\n  ", ",\n  ", "\n}")
    println(paramMap)
    val jsCode = s"""
      const paramValues = $paramMap;
      function param(name) { return paramValues[name][0]; }
      function params(name) { return paramValues[name]; }
    """

    val context = Context.create("js")

    val value = context.eval("js", jsCode)

    println(context.eval("js", """
      console.log(param('p1'));
      console.log(params('p1'));
      console.log(param('p2'));
      console.log(params('p2'));
      "That's all I hafta say..."
    """))
end Javascript
