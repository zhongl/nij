package com.github.zhongl.mockclients

import java.nio.channels.SocketChannel.{open => newSocketChannel}
import java.net.InetSocketAddress
import com.github.zhongl.mockclients.Utils.{times => build, silent => silentCall}
import com.github.zhongl.mockclients.Utils._
import java.nio.channels.{SelectionKey}
import CommunicationStatistic._

/**
 * {@link MockClientsEngine} .
 *
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl</a>
 */
class MockClientsEngine(clients: Int, remote: InetSocketAddress, val handler: KeyHandler, timeout: Long)
    extends EventPoller(timeout) {

  build(clients) {
    val channel = newSocketChannel
    try {
      channel.configureBlocking(false)
      channel.register(selector, SelectionKey.OP_CONNECT)
      channel.connect(remote)
      statisticOf(channel).connect
      log.info("connect to " + remote)
    } catch {
      case e =>
        log.error("Unexpected error.", e)
        silentCall {channel.close}
    }
  }

  private def isWriteOver(key: SelectionKey): Boolean = {
    (key.interestOps & SelectionKey.OP_READ) == SelectionKey.OP_READ
  }

  private def intelligentWakeupSelectingByInterestOpsOf(key: SelectionKey)(closure: => Unit) = {
    val before = key.interestOps
    closure
    val after = key.interestOps
    if (after != before) wakeup
  }

  protected def doHandle = {
    case Connectable(key) =>
      statisticOf(socketChannelOf(key)).accepted
      asynchronized {
        intelligentWakeupSelectingByInterestOpsOf(key) {
          handler.handleConnectable(key)
        }
      }
    case Writable(key) =>
      asynchronized {
        intelligentWakeupSelectingByInterestOpsOf(key) {
          handler.handleWritable(key)
          if (isWriteOver(key)) statisticOf(socketChannelOf(key)).request
        }
      }
    case Readable(key) =>
      statisticOf(socketChannelOf(key)).responsed
      asynchronized {handler.handleReadable(key)}
  }
}
