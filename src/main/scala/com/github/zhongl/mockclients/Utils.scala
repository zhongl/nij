package com.github.zhongl.mockclients

import scala.actors.Actor
import java.nio.channels.{SelectionKey, SocketChannel}
import java.nio.channels.SelectionKey._

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl</a>
 */
object Utils {
  def interestAccept(key: SelectionKey): Unit = interest(OP_ACCEPT, key)

  def interestConnect(key: SelectionKey): Unit = interest(OP_CONNECT, key)

  def interestRead(key: SelectionKey): Unit = interest(OP_READ, key)

  def interestWrite(key: SelectionKey): Unit = interest(OP_WRITE, key)

  def uninterestAccept(key: SelectionKey): Unit = uninterest(OP_ACCEPT, key)

  def uninterestConnect(key: SelectionKey): Unit = uninterest(OP_CONNECT, key)

  def uninterestRead(key: SelectionKey): Unit = uninterest(OP_READ, key)

  def uninterestWrite(key: SelectionKey): Unit = uninterest(OP_WRITE, key)

  def silent(call: => Unit): Unit = try {call} catch {case _ => Unit}

  def asynchronized(call: => Unit): Unit = Actor.actor {call}

  def times(num: Int)(call: => Any): Unit = num match {
    case i if i > 0 => call; times(i - 1)(call)
    case 0 => Unit
  }

  def socketChannelOf(key: SelectionKey): SocketChannel = key.channel.asInstanceOf[SocketChannel]

  private def interest(ops: Int, key: SelectionKey): Unit = key.interestOps(key.interestOps | ops)

  private def uninterest(ops: Int, key: SelectionKey): Unit = key.interestOps(key.interestOps & ~ops)
}