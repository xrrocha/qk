package qk

import Utils.*
import collection.mutable.Map as MMap
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import java.io.FileInputStream
import java.net.InetSocketAddress
import org.virtuslab.yaml.*
import scala.util.{Failure, Success, Using}
import scala.annotation.threadUnsafe

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
                s.setExecutor(null)
                s.createContext("/", this)

    override def handle(exchange: HttpExchange): Unit =
        val path =
            exchange
                .getRequestURI()
                .getPath()
                .substring(1)
                .let: value =>
                    if value == "" then indexFile
                    else value

        // TODO Yield 404 on non-existent indexfile
        if (path == indexFile && !resources.contains(indexFile)) then
            require(
              getResource(indexFile).isDefined,
              s"Can't read index file '$indexFile'"
            )

        val ext = extension(path)

        // TODO Actually handle *.qk!

        val (bytes, mimeType, httpCode) =
            getResource(path) match
                case Some(bytes) =>
                    val mimeType = MimeTypes(ext)
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

    def qk[A](path: String, compile: (String) => A): A =
        ???

    private val resources = MMap[String, Array[Byte]]()

    private def getResource(
        resourceName: String
    ): Option[Array[Byte]] =
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

    // TODO
    def log(msg: String) = println(msg)
end WebServer

object WebServer:
    val defaultIndex = "index.html"

    val defaultExtensions = Set("qk", "yml", "yaml")

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
