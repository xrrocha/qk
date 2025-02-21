package qk

object WebServer:
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
end WebServer
