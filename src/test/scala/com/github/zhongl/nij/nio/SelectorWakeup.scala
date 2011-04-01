package com.github.zhongl.nij.nio

import java.nio.channels.Selector
import System.{currentTimeMillis => now}
import com.github.zhongl.mockclients.Utils._

object SelectorWakeup extends Application{
  val selector = Selector.open
  selector.wakeup
  selector.wakeup
  times(2){
    val begin = now
    selector.select(1000L)
    val after = now
    println(after - begin)
  }
  selector.close
}