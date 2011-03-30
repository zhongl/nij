package com.github.zhongl.mockclients

import java.nio.channels.SelectionKey

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl</a>
 */
abstract class CertainKeyExtractor {
  def unapply(key: SelectionKey): Option[SelectionKey] = {
    if (key.isValid && isInterest(key)) {
      uninterest(key)
      Some(key)
    } else None
  }

  def isInterest(key: SelectionKey): Boolean

  def uninterest(key: SelectionKey): Unit
}

object Acceptable extends CertainKeyExtractor {
  def uninterest(key: SelectionKey) = Utils.uninterest(SelectionKey.OP_ACCEPT, key)

  def isInterest(key: SelectionKey) = key.isAcceptable
}

object Connectable extends CertainKeyExtractor {
  def uninterest(key: SelectionKey) = Utils.uninterest(SelectionKey.OP_CONNECT, key)

  def isInterest(key: SelectionKey) = key.isConnectable && Utils.socketChannelOf(key).finishConnect
}

object Readable extends CertainKeyExtractor {
  def uninterest(key: SelectionKey) = Utils.uninterest(SelectionKey.OP_READ, key)

  def isInterest(key: SelectionKey) = key.isReadable
}

object Writable extends CertainKeyExtractor {
  def uninterest(key: SelectionKey) = Utils.uninterest(SelectionKey.OP_WRITE, key)

  def isInterest(key: SelectionKey) = key.isWritable
}
