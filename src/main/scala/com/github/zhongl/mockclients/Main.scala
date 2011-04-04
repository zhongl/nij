package com.github.zhongl.mockclients

import java.net.InetSocketAddress
import java.nio.channels.SelectionKey

import com.github.zhongl.mockclients.Utils._
import java.util.concurrent.CountDownLatch
import actors.threadpool.{LinkedBlockingQueue}
import actors.Actor

object Main extends Application {

  val requestTimes = 10
  val connections = 1
  val requests = new LinkedBlockingQueue[Request]
  val remote = new InetSocketAddress("localhost", 8080)
  val completed = new CountDownLatch(requestTimes)

  var engines: List[MockClientsEngine] = Nil


  times(1) {
    engines ::= new MockClientsEngine(connections, 10, requests, remote, completed, 500)
  }

  Runtime.getRuntime.addShutdownHook(new Thread() {override def run = {engines foreach {_.stop}}})

  println("Test started.")

  engines foreach {_.start}

  times(requestTimes) {
    requests.put(new Request(512, 10, 1024))
  }

  completed.await

  engines foreach {_.stop}

  println("Test completed.")
}

