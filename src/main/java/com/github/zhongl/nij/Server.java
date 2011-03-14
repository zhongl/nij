package com.github.zhongl.nij;

import java.net.ServerSocket;
import java.net.Socket;

public class Server {
  private static volatile boolean running = true;

  public static void main(String... args) throws Exception {
    final int port = Integer.parseInt(args[0]);
    final int backlog = Integer.parseInt(args[1]);

    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        running = false;
      }
    });

    ServerSocket server = new ServerSocket(port, backlog);
    server.setSoTimeout(500);
    System.out.println("Server accepting at port " + server.getLocalPort() + " with backlog " + backlog);
    for (; ;) {
      Socket client = server.accept();
      if (!running) break;
      if (client != null) client.close();
    }
    server.close();
  }
}
