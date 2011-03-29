package com.github.zhongl.mockclients

import scala.actors.Actor
import java.nio.channels.{SocketChannel, SelectionKey}


object Utils {
  def uninterest(ops: Int, key: SelectionKey): Unit = key.interestOps(key.interestOps & ~ops)

  def interest(ops: Int, key: SelectionKey): Unit = key.interestOps(key.interestOps | ops)

  def silent(call: => Unit): Unit = try {call} catch {case _ => Unit}

  def asynchronized(handling: => Unit): Unit = Actor.actor handling

  def times(num: Int)(f: => Any): Unit = num match {
    case i if i > 0 => f; times(i - 1)(f)
    case 0 => Unit
  }

  def socketChannelOf(key: SelectionKey): SocketChannel = key.channel.asInstanceOf[SocketChannel]
}