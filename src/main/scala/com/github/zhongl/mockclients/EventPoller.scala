package com.github.zhongl.mockclients

import java.nio.channels.SelectionKey
import java.nio.channels.Selector.{open => newSelector}
import scala.actors.Actor
import scala.collection.JavaConversions._
import com.github.zhongl.mockclients.Utils.silent

/**
 *
 */
abstract class EventPoller(val timeout: Long) extends Logging {
  protected val selector = newSelector
  protected val worker = new Worker

  final def start: Unit = worker.start ! Poll

  final def stop: Unit = worker ! Exit

  protected def doHandle: PartialFunction[SelectionKey, Unit]

  protected def extraExecute: PartialFunction[Command, Unit] = {
    case unknown: Command => log.error("Unknown command {}", unknown)
  }

  private def handle(selectedKey: SelectionKey): Unit = {
    if (doHandle.isDefinedAt(selectedKey)) doHandle(selectedKey)
    else logErrorAndCloseChannelOf(selectedKey)
  }

  private def logErrorAndCloseChannelOf(key: SelectionKey): Unit = {
    log.error("Close {}, because can't handle invalid key", selectedKey.channel)
    silent {selectedKey.channel.close}
  }

  private def poll: Unit = {
    if (selector.select(timeout) > 0) handleSelectedKeys
    self ! Poll
  }

  private def handleSelectedKeys: Unit = {
    selector.selectedKeys.foreach {selectedKey => handle(selectedKey)}
    selector.selectedKeys.clear
  }

  private class Worker extends Actor {
    def act() = {
      loop {
        react {
          case Poll => poll
          case Exit => silent {selector.close}; exit
          case unknown: Command => extraExecute(unknown)
          case illegal => log.error("Illegal command {}", illegal)
        }
      }
    }
  }

  abstract class Command

  case class Poll extends Command

  case class Exit extends Command

}