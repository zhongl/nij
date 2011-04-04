package com.github.zhongl.nij.netty.perf;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl</a> */
public class RequestDecoder extends FrameDecoder {

  @Override
  protected Object decode(
      ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) throws Exception {
    // Wait until the length prefix is available.
    if (buffer.readableBytes() < 4) {
      return null;
    }

    buffer.markReaderIndex();

    // Wait until the whole data is available.
    int dataLength = buffer.readInt();
    if (buffer.readableBytes() < dataLength) {
      buffer.resetReaderIndex();
      return null;
    }

    final int handleMilliseconds = buffer.readInt();
    final int responseLength = buffer.readInt();
    buffer.readBytes(new byte[dataLength - 8]);
    return new Request(handleMilliseconds, responseLength);
  }
}

