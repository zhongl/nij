package com.github.zhongl.nij.mina.perf;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Thread.currentThread;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl</a> */
public class PerfServerHandler extends IoHandlerAdapter {
  private static final Logger logger = Logger.getLogger(PerfServerHandler.class.getName());

  @Override
  public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
    logger.log(Level.WARNING, "Unexpected exception from downstream.", cause);
    session.close(true);
  }

  @Override
  public void messageReceived(IoSession session, Object message) throws Exception {
    Request request = (Request) message;
    sleep(request.handleMilliseconds);
    session.write(request.responseLength);
  }


  private void sleep(int milliseconds) {
    try { MILLISECONDS.sleep(milliseconds); } catch (InterruptedException e) { currentThread().interrupt(); }
  }

}
