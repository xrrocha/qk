package qk

import Utils.*
import com.sun.net.httpserver.HttpExchange
import java.io.IOException
import scala.collection.mutable.Map as MMap
import scala.util.{Success, Failure, Try}

type SQL = String
type Request = HttpExchange
type ByteArray = Array[Byte]
type Compiler = (String) => Handler
type Payload = ByteArray | Handler
type Handler = (Request, SQL) => Array[Byte]

case class Cache(compilers: Map[String, Compiler]):
  private val cache = MMap[String, Payload]()

  def get(path: String): Try[Payload] =
    Try:
      cache.getOrElseUpdate(path,
        readResource(path) match
          case Success(bytes) =>
            compilers.get(extension(path)) match
              case None =>
                bytes
              case Some(compiler) =>
                Try(compiler(String(bytes, "UTF-8"))) match
                  case Success(handler) =>
                    handler
                  case Failure(err) =>
                    throw IllegalArgumentException(
                      s"Can't compile '$path': ${err.getMessage()}", err)
          case Failure(err) =>
            throw IOException(s"Can't read '$path': ${err.getMessage()}", err)
    )
  end get

  // TODO Add true logging
  def warning(msg: String) = println(msg)
end Cache
