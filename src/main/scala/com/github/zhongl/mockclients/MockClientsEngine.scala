package com.github.zhongl.mockclients

import java.nio.channels.SocketChannel.{open => newSocketChannel}
import java.net.InetSocketAddress
import java.nio.channels.{SelectionKey}
import java.nio.ByteBuffer
import actors.threadpool.{TimeUnit, BlockingQueue}
import scala.collection.JavaConversions._

import CommunicationStatistic._
import com.github.zhongl.mockclients.Utils.{times => build, silent => silentCall}
import com.github.zhongl.mockclients.Utils._
import java.util.concurrent.CountDownLatch

/**
 * {@link MockClientsEngine} .
 *
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl</a>
 */
class MockClientsEngine(clients: Int,
                        val requestTimesPerClient: Int,
                        val requests: BlockingQueue[Request],
                        val remote: InetSocketAddress,
                        val latch: CountDownLatch,
                        timeout: Long)
    extends EventPoller(timeout) {

  build(clients) {new Connection}

  override protected def dispose = {
    selector.keys foreach {socketChannelOf(_).close}
    super.dispose
  }

  protected def doHandle = {
    case Connectable(key) =>
      statisticOf(socketChannelOf(key)).accepted
      asynchronized {operateByStateOf(key) {handlerOf(key).handleConnectable(key)}}
    case Writable(key) =>
      asynchronized {
        operateByStateOf(key) {
          handlerOf(key).handleWritable(key)
          if (key.isValid && isWriteOver(key)) statisticOf(socketChannelOf(key)).request
        }
      }
    case Readable(key) =>
      statisticOf(socketChannelOf(key)).responsed
      asynchronized {operateByStateOf(key) {handlerOf(key).handleReadable(key)}}
  }

  private def isWriteOver(key: SelectionKey): Boolean = (key.interestOps & SelectionKey.OP_READ) != 0

  private def operateByStateOf(key: SelectionKey)(closure: => Unit) = {
    val before = key.interestOps
    closure
    if (!socketChannelOf(key).isOpen) {
      if (latch.getCount > 0) new Connection
    } else if (key.interestOps != before) wakeup
  }

  private def handlerOf(key: SelectionKey): KeyHandler = key.attachment.asInstanceOf[KeyHandler]

  class Connection extends KeyHandler {
    @volatile var requestTimes = 0
    @volatile var currentRequest: Request = null
    @volatile var currentRequestByteBuffer: ByteBuffer = null
    @volatile var read = 0

    val channel = newSocketChannel
    val readBuffer = ByteBuffer.allocateDirect(2048)

    try {
      channel.configureBlocking(false)
      channel.register(selector, SelectionKey.OP_CONNECT, this)
      channel.connect(remote)
      statisticOf(channel).connect
      log.info("connect to " + remote)
    } catch {
      case e =>
        log.error("Unexpected error.", e)
        silentCall {channel.close}
    }

    def handleWritable(key: SelectionKey): Unit = {
      if (currentRequest == null) {
        currentRequest = requests.poll(500L, TimeUnit.MILLISECONDS)
        if (currentRequest != null)
          currentRequestByteBuffer = currentRequest.byteBuffer
        else {
          socketChannelOf(key).close
          return
        }
      }

      channel.write(currentRequestByteBuffer)

      if (currentRequestByteBuffer.hasRemaining) interestWrite(key) else interestRead(key)
    }

    def handleReadable(key: SelectionKey) {
      read += channel.read(readBuffer)
      readBuffer.clear

      if (read >= currentRequest.responseLength) {
        currentRequest = null
        requestTimes += 1
        latch.countDown
        if (requestTimes == requestTimesPerClient) socketChannelOf(key).close
        else interestWrite(key)
      } else interestRead(key)

    }

    def handleConnectable(key: SelectionKey) = {
      val socket = socketChannelOf(key).socket
      socket.setTcpNoDelay(true)
      interestWrite(key)
    }

    def handleAcceptable(key: SelectionKey) = throw new UnsupportedOperationException
  }

}
