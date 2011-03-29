package com.github.zhongl.mockclients

import java.nio.channels.SelectionKey


trait KeyHandler {
  def handleAcceptable(key: SelectionKey): Unit = {}

  def handleConnectable(key: SelectionKey): Unit = {}

  def handleReadable(key: SelectionKey): Unit = {}

  def handleWritable(key: SelectionKey): Unit = {}
}