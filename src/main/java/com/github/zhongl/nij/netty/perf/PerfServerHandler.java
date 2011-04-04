package com.github.zhongl.nij.netty.perf;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Thread.currentThread;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl</a> */
public class PerfServerHandler extends SimpleChannelUpstreamHandler {

  private static final Logger logger = Logger.getLogger(PerfServerHandler.class.getName());

  @Override
  public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
    Request request = (Request) e.getMessage();
    sleep(request.handleMilliseconds);
    e.getChannel().write(responseBuffer(request.responseLength));
  }

  private ChannelBuffer responseBuffer(int responseLength) {
    final ChannelBuffer buffer = ChannelBuffers.buffer(responseLength);
    for (int i = 0; i < responseLength; i++) buffer.writeByte('a');
    return buffer;
  }

  private void sleep(int milliseconds) {
    try { MILLISECONDS.sleep(milliseconds); } catch (InterruptedException e) { currentThread().interrupt(); }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
    logger.log(Level.WARNING, "Unexpected exception from downstream.", e.getCause());
    e.getChannel().close();
  }
}
