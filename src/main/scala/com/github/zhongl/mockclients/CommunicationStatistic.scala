package com.github.zhongl.mockclients

import java.nio.channels.SocketChannel
import java.util.concurrent.ConcurrentHashMap
import java.lang.System.{nanoTime => now}


object CommunicationStatistic {
  private val statistics = new ConcurrentHashMap[SocketChannel, CommunicationStatistic]

  def statisticOf(channel: SocketChannel) = {
    val exist = statistics.get(channel)
    if (exist == null) {
      val n = new CommunicationStatistic(channel)
      val o = statistics.putIfAbsent(channel, n)
      if (o == null) n else o
    } else exist
  }

  def clear = statistics.clear
}

class CommunicationStatistic(val channel: SocketChannel) extends Logging {
  @volatile var timestamp = 0L

  def connect = timestamp = now

  def accepted = log.info("{} connection elapse : {} ns", channel, now - timestamp)

  def request = timestamp = System.nanoTime

  def responsed = log.info("{} get response elapse : {} ns", channel, now - timestamp)
}