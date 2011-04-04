package com.github.zhongl.nij.mina.perf;

import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import java.net.InetSocketAddress;

public class PerfServer {
  public static void main(String[] args) throws Exception {
    final String host = args[0];
    final int port = Integer.parseInt(args[1]);
    final NioSocketAcceptor acceptor = new NioSocketAcceptor();

    acceptor.getFilterChain().addLast("request-decoder", new ProtocolCodecFilter(new ResponseEncoder(), new RequestDecoder()));
    acceptor.setHandler(new PerfServerHandler());

    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() { acceptor.unbind(); }
    });

    acceptor.bind(new InetSocketAddress(host, port));
    System.out.println("Mina PerfServer started");
  }
}
