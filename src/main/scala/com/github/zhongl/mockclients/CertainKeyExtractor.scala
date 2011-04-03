package com.github.zhongl.mockclients

import java.nio.channels.SelectionKey
import com.github.zhongl.mockclients.Utils._

/**
 * {@link CertainKeyExtractor} for key state pattern matching.
 *
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
  def uninterest(key: SelectionKey) = uninterestAccept(key)

  def isInterest(key: SelectionKey) = key.isAcceptable
}

object Connectable extends CertainKeyExtractor {
  def uninterest(key: SelectionKey) = uninterestConnect(key)

  def isInterest(key: SelectionKey) = key.isConnectable && Utils.socketChannelOf(key).finishConnect
}

object Readable extends CertainKeyExtractor {
  def uninterest(key: SelectionKey) = uninterestRead(key)

  def isInterest(key: SelectionKey) = key.isReadable
}

object Writable extends CertainKeyExtractor {
  def uninterest(key: SelectionKey) = uninterestWrite(key)

  def isInterest(key: SelectionKey) = key.isWritable
}
