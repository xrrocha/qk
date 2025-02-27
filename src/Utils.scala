package qk

import Utils.*
import java.io.{FileNotFoundException, InputStream}
import scala.util.{Try, Using}

object Utils:

    def extension(filename: String): String =
        filename
            .lastIndexOf('.')
            .let: pos =>
                if pos < 0 then ""
                else filename.substring(pos + 1)

    def normalizeSpace(s: String): String =
        s.trim().split("\\s+").mkString(" ")

    def time[A](action: => A): (A, Long) =
        val startTime = System.currentTimeMillis()
        (action, System.currentTimeMillis() - startTime)

    def readResource(resourceName: String): Try[Array[Byte]] =
        openResource(resourceName).flatMap(Using(_)(_.readAllBytes()))
    end readResource

    def openResource(resourceName: String): Try[InputStream] =
        Try:
            val is = Thread
                .currentThread()
                .getContextClassLoader()
                .getResourceAsStream(resourceName)
            if is != null then is
            else throw FileNotFoundException(s"No such resource: $resourceName")
    end openResource

    implicit class KLike[T](t: T):
        def let[R](f: T => R): R = f(t)
        def also(f: T => Unit): T = { f(t); t }
end Utils
