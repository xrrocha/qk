package qk

import Utils.*
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import java.net.URI
import scala.util.Using

class WebServerTest extends munit.FunSuite:
    private val port = 1960
    private val server = WebServer(port)

    override def beforeEach(context: BeforeEach): Unit =
        server.start()

    override def afterEach(context: AfterEach): Unit =
        server.stop()

    test("Runs server"):
        val resourceName = "qk.yml"
        URI(s"http://localhost:$port/$resourceName")
            .toURL()
            .openConnection()
            .getInputStream()
            .let(Using(_)(_.readAllBytes()))
            .also: result =>
                assert(result.isSuccess)
                assertEquals(
                  result.get.length,
                  readResource(resourceName).get.length
                )

    test("Builds param map from query string"):
        val queryString = "deptno=0010&name=KING&name=O'HARA"
        val paramMap = WebServer.paramMapFrom(queryString)

        assertEquals(
          paramMap,
          Map(
            "deptno" -> Seq("0010"),
            "name" -> Seq("KING", "O'HARA")
          )
        )
end WebServerTest
