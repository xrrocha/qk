package qk

import Utils.*
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import java.io.FileInputStream
import java.net.InetSocketAddress
import org.virtuslab.yaml.*
import scala.util.Using

case class WebServer(
    port: Int
) derives YamlCodec:

  private var server = createServer()

  def start() =
    server.start()

  def stop() =
    server.stop(0)
    server = createServer()

  private def createServer() =
    HttpServer
      .create(new InetSocketAddress(port), 0)
      .also: s =>
        s.createContext("/", QKHandler)
        s.setExecutor(null)
end WebServer

object WebServer:
  @main
  def main(configFilename: String) =
    Using(FileInputStream(configFilename)):
      _.readAllBytes()
        .let(String(_, "UTF-8"))
        .let(_.as[WebServer])
        .also: result =>
          result match
            case Left(err) =>
              println(s"Kaput: $err")
              sys.exit(1)
            case Right(webServer) =>
              webServer.start()
              println(
                s"QK webserver listening on ${webServer.port}. Ctrl-C to stop"
              )
              Runtime
                .getRuntime()
                .addShutdownHook:
                  Thread: () =>
                    println("Shutting down...")
                    webServer.stop()

  def paramMapFrom(queryString: String) =
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
  end paramMapFrom

  val symbol = """^\p{Alpha}[\p{Alnum}_]*$""".r
  def isSymbol(s: String) = symbol.matches(s)
end WebServer

object QKHandler extends HttpHandler:
  def handle(exchange: HttpExchange): Unit =
    exchange
      .getRequestURI()
      .getPath()
      .also: path =>
        path
          .substring(1)
          .let(readResource)
          .map: response =>
            path
              .substring(path.lastIndexOf('.') + 1)
              .let: ext =>
                val contentType =
                  MimeTypes(ext).getOrElse("application/octet-stream")
                exchange.getResponseHeaders().set("Content-Type", contentType)
                Using(exchange.getResponseBody()): out =>
                  exchange.sendResponseHeaders(200, response.length)
                  out.write(response)
                  out.flush()
          .getOrElse:
            exchange.getResponseHeaders().set("Content-Type", "text/plain")
            Using(exchange.getResponseBody()): out =>
              val errMsg = "Not found: $path".getBytes()
              exchange.sendResponseHeaders(404, errMsg.length)
              out.write(errMsg)
              out.flush()
  end handle
end QKHandler
