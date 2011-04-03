package com.github.zhongl.mockclients

import java.nio.channels.SelectionKey
import java.nio.channels.Selector.{open => newSelector}
import scala.actors.Actor
import scala.collection.JavaConversions._
import com.github.zhongl.mockclients.Utils.{silent => silentCall}
import java.util.concurrent.atomic.AtomicInteger

/**
 * {@link EventPoller} is a abstract class wrap a {@link Selector} to watching channels.
 * It could be extended to a acceptor( accept connection) or proccessor (handle read and write) or dispatcher (dispatch io event).
 *
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl</a>
 */
abstract class EventPoller(val timeout: Long) extends Logging {
  protected val selector = newSelector
  protected val worker = new Worker

  private val wakeupCalls = new AtomicInteger()

  final def start: Unit = worker.start ! Poll

  final def stop: Unit = worker ! Exit(None)

  /* To avoid wakeup overload, only first call can do wakeup the selector, and others wait until checking wakeupCalls. */
  protected final def wakeup = if (wakeupCalls.incrementAndGet == 1) selector.wakeup

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

  /* To wakeup selector only if more than once called wakeup, or else some wakeup events may be ignored. */
  private def checkAndResetWakeupCalls: Unit = if (wakeupCalls.getAndSet(0) > 1) selector.wakeup

  private def hasSelectedKeys: Boolean = {
    checkAndResetWakeupCalls
    selector.select(timeout) > 0
  }

  private def poll: Unit = {
    try {
      if (hasSelectedKeys) handleSelectedKeys
      worker ! Poll
    } catch {case t => worker ! Exit(Some(t))}
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
          case Exit(cause) =>
            if (selector.isOpen) silentCall {selector.close};
            if (cause.isEmpty) log.info("EventPoller exit normally.")
            else log.error("EventPoller exit cause by ", cause.get)
            exit
          case extra: Command if extraExecute.isDefinedAt(extra) => extraExecute(extra)
          case illegal => log.error("Illegal command {}", illegal)
        }
      }
    }
  }

}

abstract class Command

case class Poll extends Command

case class Exit(throwable: Option[Throwable]) extends Command
