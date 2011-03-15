package com.github.zhongl.nij.nio;

import static java.nio.channels.SelectionKey.*;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.*;
import java.util.*;

public final class Multiplexors {
  private static final Set<Multiplexor> set = Collections.synchronizedSet(new HashSet<Multiplexor>());

  public static void startWith(ServerSocketChannel serverSocketChannel) throws IOException {
    new Multiplexor(serverSocketChannel).start();
  }

  public static void shutdownAll() {
    for (Multiplexor multiplexor : set)
      multiplexor.shutdown();
  }

  private static void silentClose(Selector selector) {
    if (selector == null) return;
    try { selector.close(); } catch (IOException e) { }
  }

  private static void silentClose(Closeable closeable) {
    // TODO shutdown output if closeable is socket channel
    if (closeable == null) return;
    try { closeable.close(); } catch (IOException e) { }
  }

  /** {@link Multiplexor} */
  private static class Multiplexor extends Thread {
    private final Selector selector;
    private volatile boolean running = true;

    private Multiplexor(Selector selector) {
      setName(getClass().getSimpleName());
      this.selector = selector;
      setDaemon(false);
      set.add(this);
    }

    private Multiplexor(ServerSocketChannel serverSocketChannel) throws IOException {
      this(Selector.open());
      serverSocketChannel.register(selector, OP_WRITE);
    }

    private void shutdown() {
      selector.wakeup();
      running = false;
    }

    @Override
    public void run() {
      while (running)
        try { doRun(); } catch (Exception e) { /* TODO log error*/ }

      for (SelectionKey key : selector.keys()) silentClose(key.channel());
      silentClose(selector);
      set.remove(this);
    }

    private void doRun() throws Exception {
      final int selected = selector.select(500L);
      final int registered = selector.keys().size();

      if (registered == 0 && thenShutdown()) return;

      if (selected > 0) {
        final Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
        while (iterator.hasNext()) {
          final SelectionKey key = iterator.next();
          iterator.remove();
          if (key.isAcceptable()) accept(key);
          if (key.isReadable()) read(key);
          if (key.isWritable()) write(key);
        }
      }
    }

    private boolean thenShutdown() { return !(running = false); }

    private void write(SelectionKey key) {
      //TODO submit a write event.
    }

    private void read(SelectionKey key) {
      //TODO submit a read event.
    }

    // TODO Too many keys may slow down selecting.
    // TODO limit max acceptance.
    private void accept(SelectionKey key) {
      final ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
      SocketChannel channel = null;
      try {
        channel = serverSocketChannel.accept();
        if (channel == null) return;
//        nChannel.socket().setTcpNoDelay(true);
//        nChannel.socket().setSoLinger(false, 0);
        channel.socket().setSendBufferSize(64 * 1024);
        channel.configureBlocking(false);
        channel.register(selector, OP_READ | OP_WRITE);
      } catch (IOException e) {
        // TODO logError(channel, e);
        silentClose(channel);
      }

    }

  }
}