package qk

import org.graalvm.polyglot.*

object Javascript:

  def createContext(): Context = Context.create("js")

  def buildReqObj(
      context: Context,
      objScript: String,
      paramMap: Map[String, Seq[String]]
  ): Any =

    val paramsObj = paramMap.toSeq
      .map: p =>
        val name = p._1
        val value = p._2
          .map(escape)
          .map(v => s"'$v'")
          .mkString("[", ", ", "]")
        s"$name: $value"
      .mkString("{\n  ", ",\n  ", "\n}")

    val paramDefs = s"""
      const paramValues = $paramsObj;
      function param(name) { return paramValues[name][0]; }
      function params(name) { return paramValues[name]; }
    """

    context.eval("js", s"$paramDefs\n$objScript")
  end buildReqObj

  val quotes = """[\\'"]""".r
  def escape(s: String) = quotes.replaceAllIn(s, """\\$0""")
end Javascript
