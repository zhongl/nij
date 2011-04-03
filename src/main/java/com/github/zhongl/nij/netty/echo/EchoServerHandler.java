package com.github.zhongl.nij.netty.echo;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handler implementation for the echo server.
 *
 * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 * @version $Rev: 2121 $, $Date: 2010-02-02 09:38:07 +0900 (Tue, 02 Feb 2010) $
 */
public class EchoServerHandler extends SimpleChannelUpstreamHandler {

  private static final Logger logger = Logger.getLogger(
      EchoServerHandler.class.getName());


  @Override
  public void messageReceived(
      ChannelHandlerContext ctx, MessageEvent e) {
    // Send back the received request to the remote peer.
    Request request = (Request) e.getMessage();
    try {
      TimeUnit.MILLISECONDS.sleep(request.handleMilliseconds);
    } catch (InterruptedException e1) {
      Thread.currentThread().interrupt();
    }

    ChannelBuffer buffer = ChannelBuffers.buffer(request.responseLength);
    for (int i = 0; i < request.responseLength; i++) {
      buffer.writeByte('a');
    }
    e.getChannel().write(buffer);
  }

  @Override
  public void exceptionCaught(
      ChannelHandlerContext ctx, ExceptionEvent e) {
    // Close the connection when an exception is raised.
    logger.log(
        Level.WARNING,
        "Unexpected exception from downstream.",
        e.getCause());
    e.getChannel().close();
  }
}
