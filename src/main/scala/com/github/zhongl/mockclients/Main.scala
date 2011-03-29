package com.github.zhongl.mockclients

import java.net.InetSocketAddress
import java.nio.channels.SelectionKey

import com.github.zhongl.mockclients.Utils.{interest, socketChannelOf}
import java.nio.ByteBuffer
import scala.actors.threadpool.AtomicInteger
import java.util.concurrent.CountDownLatch

object Main {

  def main(args: Array[String]) = {
    val Array(host, Num(port), Num(connections)) = args
    val target = new InetSocketAddress(host, port)
    val completed = new CountDownLatch(connections)
    val success = new AtomicInteger()

    val content = ByteBuffer.wrap("hi".getBytes)

    val handler = new KeyHandler {
      override def handleWritable(key: SelectionKey) = {
        socketChannelOf(key).write(content)
        interest(SelectionKey.OP_READ, key)
        key.attach(ByteBuffer.allocate(2))
      }

      override def handleReadable(key: SelectionKey) = {
        val buf = key.attachment.asInstanceOf[ByteBuffer]
        socketChannelOf(key).read(buf)
        if (buf.hasRemaining) interest(SelectionKey.OP_READ, key)
        else {
          completed.countDown
          if (buf == content) success.incrementAndGet
        }
      }

      override def handleConnectable(key: SelectionKey) = {
        interest(SelectionKey.OP_WRITE, key)
        val socket = socketChannelOf(key).socket
        socket.setKeepAlive(false)
        socket.setTcpNoDelay(true)
        socket.setSoLinger(true, 0)
      }
    }

    val eventPoller = new FixChannelsEventPoller(connections, target, handler, 500)

    Runtime.getRuntime.addShutdownHook(new Thread() {override def run = {eventPoller.stop}})

    eventPoller.start

    completed.await

    eventPoller.stop

    println("Completed : " + completed.getCount)
    println("Success : " + success.get)
  }

}

object Num {
  def unapply(s: String): Option[Int] = try {Some(s.toInt)} catch {case _ => None}
}
