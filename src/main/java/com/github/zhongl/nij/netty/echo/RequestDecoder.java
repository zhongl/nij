package com.github.zhongl.nij.netty.echo;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;

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

    final Request request = new Request(buffer.readInt(), buffer.readInt());
    buffer.readBytes(new byte[dataLength - 8]);
//    buffer.resetReaderIndex();
    return request;
  }
}

