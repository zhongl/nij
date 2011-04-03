package com.github.zhongl.mockclients

import java.nio.channels.SelectionKey

/**
 * {@link KeyHandler} define four method to handle different state key.
 *
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl</a>
 */
trait KeyHandler {
  def handleAcceptable(key: SelectionKey): Unit

  def handleConnectable(key: SelectionKey): Unit

  def handleReadable(key: SelectionKey): Unit

  def handleWritable(key: SelectionKey): Unit
}