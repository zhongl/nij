package com.github.zhongl.mockclients

import java.net.InetSocketAddress
import java.nio.channels.SelectionKey

import com.github.zhongl.mockclients.Utils.{silent => silentCall}
import com.github.zhongl.mockclients.Utils._
import java.nio.ByteBuffer
import java.util.concurrent.CountDownLatch
import java.util.Arrays
import actors.threadpool.{LinkedBlockingQueue, AtomicInteger}
import actors.Actor

object Main {

  def main(args: Array[String]) = {
    val Array(host, Num(port), Num(connections)) = args
    val target = new InetSocketAddress(host, port)
    val completed = new CountDownLatch(connections)
//    val success = new AtomicInteger
    val requests = new LinkedBlockingQueue[Request]

    Actor.actor {
      requests.put(new Request(512, 100, 1024))
      Unit
    }

    val engine = new MockClientsEngine(connections, 1, requests, target, completed, 500)

    Runtime.getRuntime.addShutdownHook(new Thread() {override def run = {engine.stop}})

    engine.start
    println("go")

    completed.await

    engine.stop

    println("Completed : " + connections)
//    println("Success : " + success.get)
  }

}

object Num {
  def unapply(s: String): Option[Int] = try {Some(s.toInt)} catch {case _ => None}
}
