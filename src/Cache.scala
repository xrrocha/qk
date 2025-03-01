package qk

import Utils.*
import java.io.IOException
import scala.collection.mutable.Map as MMap
import scala.util.{Success, Failure, Try}

case class Cache(compilers: Map[Extension, Compiler]):
    private val cache = MMap[String, Payload]()

    def get(path: String): Try[Payload] =
        def buildPayload =
            readResource(path)
                .flatMap: bytes =>
                    Try:
                        compilers.get(extension(path)) match
                            case None => bytes
                            case Some(compiler) =>
                                compiler(String(bytes, "UTF-8"))
                .get
        Try(cache.getOrElseUpdate(path, buildPayload))
    end get
end Cache
