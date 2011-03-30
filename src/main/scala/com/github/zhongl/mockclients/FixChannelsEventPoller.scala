package com.github.zhongl.mockclients

import java.nio.channels.SocketChannel.{open => newSocketChannel}
import java.nio.channels.SelectionKey
import java.net.InetSocketAddress
import com.github.zhongl.mockclients.Utils.{times, asynchronized}

class FixChannelsEventPoller(num: Int, remote: InetSocketAddress, val handler: KeyHandler, timeout: Long)
    extends EventPoller(timeout) {

  times(num) {
    val channel = newSocketChannel
    channel.configureBlocking(false)
    channel.register(selector, SelectionKey.OP_CONNECT)
    channel.connect(remote)
    log.info("connect to " + remote)
  }

  protected def doHandle = {
    case Connectable(key) => asynchronized {handler.handleConnectable(key)}
    case Writable(key) => asynchronized {handler.handleWritable(key)}
    case Readable(key) => asynchronized {handler.handleReadable(key)}
  }
}