package com.github.zhongl.nij.nio;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;

public class Server {
  public static void main(String... args) throws Exception {
    final ServerSocketChannel channel = ServerSocketChannel.open();

    final int port = Integer.parseInt(args[0]);
    final int backlog = Integer.parseInt(args[1]);
    final int size = Integer.parseInt(args[2]);
    final SocketAddress address = new InetSocketAddress("localhost", port);
    channel.socket().setReuseAddress(true);
    channel.socket().setReceiveBufferSize(kb(size));
    channel.configureBlocking(false);

    channel.socket().bind(address, backlog);

    Multiplexors.startWith(channel);

    Runtime.getRuntime().addShutdownHook(new Thread("Shutdown-hook") {
      @Override
      public void run() {
        Multiplexors.shutdownAll();
        System.out.println("Stopped.");
      }
    });
    System.out.println("Started at port: " + port + " with backlog: " + backlog + " receive buffer: " + size + "k.");
  }

  private static int kb(int kb) {return kb * 1024;}

}
