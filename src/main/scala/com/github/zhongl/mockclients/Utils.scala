package com.github.zhongl.mockclients

import scala.actors.Actor
import java.nio.channels.{SocketChannel, SelectionKey}

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl</a>
 */
object Utils {
  def uninterest(ops: Int, key: SelectionKey): Unit = key.interestOps(key.interestOps & ~ops)

  def interest(ops: Int, key: SelectionKey): Unit = key.interestOps(key.interestOps | ops)

  def silent(call: => Unit): Unit = try {call} catch {case _ => Unit}

  def asynchronized(call: => Unit): Unit = Actor.actor {call}

  def times(num: Int)(call: => Any): Unit = num match {
    case i if i > 0 => call; times(i - 1)(call)
    case 0 => Unit
  }

  def socketChannelOf(key: SelectionKey): SocketChannel = key.channel.asInstanceOf[SocketChannel]
}