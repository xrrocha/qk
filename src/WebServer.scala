package qk

object WebServer:
  def paramsFrom(queryString: String) =
    queryString
      .split("&")
      .toSeq
      .map: p =>
        val Array(name, value) = p.split("=")
        name -> value
      .filter(p => isSymbol(p._1))
      .groupBy(p => p._1)
      .view
      .mapValues(vs => vs.map(_._2))
      .toMap
  end paramsFrom

  val symbol = """^\p{Alpha}[\p{Alnum}_]*$""".r
  def isSymbol(s: String) = symbol.matches(s)
end WebServer
