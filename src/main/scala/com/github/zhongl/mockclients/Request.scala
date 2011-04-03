package com.github.zhongl.mockclients

import java.nio.ByteBuffer

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl</a>
 */
class Request(dataLength: Int, handleMilliseconds: Int, val responseLength: Int) {

  private val repr = dataLength + "-" + handleMilliseconds + "-" + responseLength

  private val buffer = {
    val direct = ByteBuffer.allocateDirect(dataLength + 12)
    direct.putInt(dataLength + 8)
    direct.putInt(handleMilliseconds)
    direct.putInt(responseLength)
    Utils.times(dataLength) {direct.put('a'.asInstanceOf[Byte])}
    direct.flip
    direct
  }

  def byteBuffer = buffer.asReadOnlyBuffer

  override def toString: String = repr

}

