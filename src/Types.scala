package qk

import com.sun.net.httpserver.HttpExchange

type ByteArray = Array[Byte]
type Extension = String
type Script = String
type Request = HttpExchange
type Handler = Request => ByteArray
type Compiler = (Script) => Handler
type Payload = ByteArray | Handler
