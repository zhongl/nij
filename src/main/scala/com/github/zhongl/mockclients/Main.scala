package com.github.zhongl.mockclients

import java.nio.channels.SocketChannel.{open => newSocketChannel}
import java.nio.channels.Selector.{open => newSelector}
import java.nio.channels.{SelectionKey}
import java.net.InetSocketAddress


object Main {
  def main(args:Array[String]) ={
    val sockets = 100
    val host = "localhost"
    val port = 10001
    val target = new InetSocketAddress(host, port)

    val cores: Int = Runtime.getRuntime.availableProcessors
    val selectors = (0 until cores).map(newSelector)

    val channel = newSocketChannel
    channel.configureBlocking(false)
    channel.register(selectors(0),SelectionKey.OP_CONNECT)
    channel.connect(target)
  }

}


class EventPoller {
  val TIMEOUT = 500
  val selector = newSelector

  def start:Nothing = poll(0)

  def poll(selected:Int):Nothing = selected match {
    case 0 => poll(selector.select(TIMEOUT))
    case _ => triggerEvents ; poll(0)
  }

  def triggerEvents:Nothing = {
    selector.selectedKeys
  }

}