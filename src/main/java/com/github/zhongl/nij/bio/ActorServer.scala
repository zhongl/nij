package com.github.zhongl.nij.bio

import java.net._
import actors._
import java.nio.channels._
import java.nio._


object ActorServer {
  val RESPONSE: Array[Byte] = "HTTP/1.0 200 OK\r\nContent-Length:1\r\n\r\na".getBytes
  val BUFFER_IN: ByteBuffer = ByteBuffer.allocateDirect(1024)
  val BUFFER_OUT: ByteBuffer = null

  def main(args: Seq[String]) {
    val (host, port, backlog, size) = args
    val socket: ServerSocket = new ServerSocket()
    socket.setReuseAddress(true);
    socket.setReceiveBufferSize(size.toInt * 1024)
    socket.setSoTimeout(500)
    socket.bind(new InetSocketAddress(host, port.toInt), backlog.toInt)

    while (true) {
      try {
        val accept: Socket = socket.accept()
        Actor.actor {
          accept.setKeepAlive(false)
          accept.setSendBufferSize(size * 1024)
          accept.setSoLinger(true, 1)
          accept.setTcpNoDelay(true)
          val readableByteChannel: ReadableByteChannel = Channels.newChannel(accept.getInputStream)
          val writableByteChannel: WritableByteChannel = Channels.newChannel(accept.getOutputStream)
          readableByteChannel.read(BUFFER_IN.duplicate)
          writableByteChannel.write(BUFFER_OUT.asReadOnlyBuffer)
          accept.shutdownOutput
          accept.close
        }
      } catch {
        case e: SocketTimeoutException => Unit
      }
    }
  }


}