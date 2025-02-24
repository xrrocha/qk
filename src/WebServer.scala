package qk

import Utils.*
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import java.io.FileInputStream
import java.net.InetSocketAddress
import org.virtuslab.yaml.*
import scala.util.{Failure, Success, Using}

case class WebServer(
    port: Int,
    indexFile: String = defaultIndex
) extends HttpHandler
    derives YamlCodec:

  private var server = createServer()

  private val indexFiles = Set("", indexFile)

  def start() =
    server.start()

  def stop() =
    server.stop(0)
    server = createServer()

  private def createServer() =
    HttpServer
      .create(new InetSocketAddress(port), 0)
      .also: s =>
        s.createContext("/", this)
        s.setExecutor(null)

  override def handle(exchange: HttpExchange): Unit =
    val path =
      exchange
        .getRequestURI()
        .getPath()
        .substring(1)
        .let: value =>
          if value == "" then indexFile
          else value

    val (bytes, mimeType, httpCode) =
      getResource(path) match
        case Some(bytes) =>
          val mimeType = MimeTypes(extension(path))
            .getOrElse("application/octet-stream")
          (bytes, mimeType, 200)
        case None =>
          val errMsg = s"Not found: $path".getBytes()
          (errMsg, "application/octet-stream", 404)

    Using(exchange.getResponseBody()): out =>
      exchange.sendResponseHeaders(httpCode, bytes.length)
      out.write(bytes)
      out.flush()
  end handle

  private val resources = collection.mutable
    .Map[String, Array[Byte]]()
    .also: _ =>
      require(
        getResource(indexFile).isDefined,
        s"Can't read index file: $indexFile"
      )

  private def getResource(resourceName: String): Option[Array[Byte]] =
    resources
      .get(resourceName)
      .orElse:
        readResource(resourceName) match
          case Success(bytes) =>
            resources(resourceName) = bytes
            Some(bytes)
          case Failure(err) =>
            log(s"Error getting resource '$resourceName': $err")
            None
  end getResource

  def extension(filename: String): String =
    filename.substring(filename.lastIndexOf('.') + 1)

  // TODO
  def log(msg: String) = println(msg)
end WebServer

object WebServer:
  val defaultIndex = "index.html"

  @main
  def main(configFilename: String) =
    Using(FileInputStream(configFilename)):
      _.readAllBytes()
        .let(String(_, "UTF-8"))
        .let(_.as[WebServer])
        .also(println)
        .also: result =>
          result match
            case Left(err) =>
              println(s"Error launching webserver: $err")
              sys.exit(1)
            case Right(webServer) =>
              webServer.start()
              println(
                s"QK listening on port ${webServer.port}. Ctrl-C to stop"
              )
              Runtime
                .getRuntime()
                .addShutdownHook:
                  Thread: () =>
                    println("Shutting down...")
                    webServer.stop()

  val defaultExtensions = Set("qk", "yml", "yaml")

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
