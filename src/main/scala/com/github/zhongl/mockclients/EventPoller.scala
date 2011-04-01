package com.github.zhongl.mockclients

import java.nio.channels.SelectionKey
import java.nio.channels.Selector.{open => newSelector}
import scala.actors.Actor
import scala.collection.JavaConversions._
import com.github.zhongl.mockclients.Utils.{silent => silentCall}
import java.util.concurrent.atomic.AtomicBoolean

/**
 * {@link EventPoller} is a abstract class wrap a {@link Selector} to watching channels.
 * It could be extended to a acceptor( accept connection) or proccessor (handle read and write) or dispatcher (dispatch io event).
 *
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl</a>
 */
abstract class EventPoller(val timeout: Long) extends Logging {
  protected val selector = newSelector
  protected val worker = new Worker
  
  private val inSelecting = new AtomicBoolean(false)

  final def start: Unit = worker.start ! Poll

  final def stop: Unit = worker ! Exit

  protected def wakeup = if (inSelecting.compareAndSet(true, false)) selector.wakeup

  protected def doHandle: PartialFunction[SelectionKey, Unit]

  protected def extraExecute: PartialFunction[Command, Unit] = {
    case unknown: Command => log.error("Unknown command {}", unknown)
  }

  private def handle(selectedKey: SelectionKey): Unit = {
    if (doHandle.isDefinedAt(selectedKey)) doHandle(selectedKey)
    else logErrorAndCloseChannelOf(selectedKey)
  }

  private def logErrorAndCloseChannelOf(key: SelectionKey): Unit = {
    log.error("Close {}, because can't handle invalid key", key.channel)
    silentCall {key.channel.close}
  }

  private def hasSelectedKeys: Boolean = {
    inSelecting.set(true)
    try {
      selector.select(timeout) > 0
    } finally {
      inSelecting.set(false)
    }
  }

  private def poll: Unit = {
    if (hasSelectedKeys) handleSelectedKeys
    worker ! Poll
  }

  private def handleSelectedKeys: Unit = {
    selector.selectedKeys.foreach {selectedKey => handle(selectedKey)}
    selector.selectedKeys.clear
  }

  protected class Worker extends Actor {
    final def act() = {
      loop {
        react {
          case Poll => poll
          case Exit => silentCall {selector.close}; exit
          case unknown: Command => extraExecute(unknown)
          case illegal => log.error("Illegal command {}", illegal)
        }
      }
    }
  }

}

abstract class Command

case class Poll extends Command

case class Exit extends Command
