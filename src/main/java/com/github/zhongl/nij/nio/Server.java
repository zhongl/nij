package com.github.zhongl.nij.nio;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;

import com.github.zhongl.nij.util.Utils;

public class Server {
  public static void main(String... args) throws Exception {
    final ServerSocketChannel channel = ServerSocketChannel.open();

    final String host = args[0];
    final int port = Integer.parseInt(args[1]);
    final int backlog = Integer.parseInt(args[2]);
    final int size = Integer.parseInt(args[3]);
    final int num = Integer.parseInt(args[4]);
    final SocketAddress address = new InetSocketAddress(host, port);
    channel.socket().setReuseAddress(true);
    channel.socket().setReceiveBufferSize(Utils.kb(size));
    channel.configureBlocking(false);

    channel.socket().bind(address, backlog);

    for(int i = 0; i < num;i++) Multiplexors.startWith(channel);

    Runtime.getRuntime().addShutdownHook(new Thread("Shutdown-hook") {
      @Override
      public void run() {
        Multiplexors.shutdownAll();
        System.out.println("Stopped.");
      }
    });

    System.out.println("Started at " + host + ":" + port +
        " with backlog: " + backlog +
        " receive buffer: " + size + "k.");
  }

}
