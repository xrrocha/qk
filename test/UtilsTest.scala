package qk

import Utils.*
import Database.executeQuery
import scala.util.{Failure, Success, Try, Using}
import java.io.FileNotFoundException

class UtilsTest extends munit.FunSuite:

    test("Extracts filename extension"):
        assertEquals(extension("pgm.c"), "c")
        assertEquals(extension("Utils.scala"), "scala")
        assertEquals(extension("noExtensionLikeAtAll"), "")

    test("Computes execution time"):
        val waitTime = 1000L
        val (_, elapsedTime) = time(Thread.sleep(waitTime))
        assert(elapsedTime - waitTime <= 10L)

    test("Normalizes space"):
        assertEquals(
          normalizeSpace("\tsome     wildly \t different    spacing  \t"),
          normalizeSpace("some wildly different spacing")
        )

    test("Opens existing resource"):
        val result = openResource("index.html")
        assert(result.get != null)

    test("Fails to open non-existing resource w/FNFE"):
        val result = openResource("non-existent.nothing")
        assert(result.isFailure)
        result.failed.foreach: t =>
            assert(t.isInstanceOf[FileNotFoundException])

    test("Reads existing resource completely and correctly"):
        val content = String(readResource("testing.123").get, "UTF-8")
        assertEquals("testing one two three", normalizeSpace(content))

end UtilsTest
