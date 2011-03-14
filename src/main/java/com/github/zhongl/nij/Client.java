package com.github.zhongl.nij;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Client {
  public static void main(String... args) throws Exception {
    final String host = args[0];
    final int port = Integer.parseInt(args[1]);
    final int thread = Integer.parseInt(args[2]);

    final AtomicLong elapse = new AtomicLong();
    final AtomicInteger failed = new AtomicInteger();

    final Thread[] threads = new Thread[thread];
    long creation = 0L;
    for (int i = 0; i < threads.length; i++) {
      final long begin = System.nanoTime();
      threads[i] = new Thread(new Connector(host, port, elapse, failed));
      creation += System.nanoTime() - begin;
      threads[i].start();
    }

    for (Thread t : threads) {
      t.join();
    }

    System.out.println("Ran  thread   : " + thread);
    System.out.println("Fail thread   : " + failed);
    System.out.println("Avg  creation : " + creation / thread);
    System.out.println("Avg  elapse   : " + elapse.get() / (thread - failed.get()));
  }

  static class Connector implements Runnable {
    private final String host;
    private final int port;
    private final AtomicLong elapse;
    private final AtomicInteger failed;

    public Connector(final String host, final int port, final AtomicLong elapse, final AtomicInteger failed) {
      this.host = host;
      this.port = port;
      this.elapse = elapse;
      this.failed = failed;
    }

    public void run() {
      try {
        final long begin = System.nanoTime();
        Socket socket = new Socket(host, port);
        elapse.addAndGet(System.nanoTime() - begin);
        socket.close();
      } catch (IOException e) {
        failed.getAndIncrement();
      }
    }
  }
}
