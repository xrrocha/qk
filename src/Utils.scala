package qk

import Utils.*
import scala.util.{Try, Using}

object Utils:

  def extension(filename: String): String =
    filename.substring(filename.lastIndexOf('.') + 1)

  implicit class KLike[T](t: T):
    def let[R](f: T => R): R = f(t)
    def also(f: T => Unit): T = { f(t); t }

  def time[A](action: => A): (A, Long) =
    val startTime = System.currentTimeMillis()
    (action, System.currentTimeMillis() - startTime)

  def normalizeSpace(s: String): String =
    s.trim().split("\\s+").mkString(" ")

  def readResource(resourceName: String): Try[Array[Byte]] =
    val is =
      Thread
        .currentThread()
        .getContextClassLoader()
        .getResourceAsStream(resourceName)
    require(is != null, s"No such resource: $resourceName")
    Using(is): is =>
      is.readAllBytes()
end Utils
