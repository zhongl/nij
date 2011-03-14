package com.github.zhongl.nij;

import java.net.*;


public class Server {
  private static volatile boolean running = true;
  private static int port;
  private static int backlog;

  public static void main(String... args) throws Exception {
    try {
      port = Integer.parseInt(args[0]);
      backlog = Integer.parseInt(args[1]);
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("Usage : <port> <backlog>");
      System.exit(-1);
    }

    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        running = false;
      }
    });

    ServerSocket server = new ServerSocket(port, backlog);
    server.setSoTimeout(500);
    System.out.println("Server accepting at port " + server.getLocalPort() + " with backlog " + backlog);
    while (running) {
      try {
        Socket client = server.accept();
        if (client != null) client.close();
      } catch (SocketTimeoutException e) { }
    }
    server.close();
  }
}
