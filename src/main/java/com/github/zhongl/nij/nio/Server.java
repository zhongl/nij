package com.github.zhongl.nij.nio;

import static java.nio.channels.SelectionKey.*;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.*;
import java.util.Iterator;

public class Server {
  public static void main(String... args) throws Exception {
    final Selector selector = Selector.open();
    final ServerSocketChannel channel = ServerSocketChannel.open();


    final int port = Integer.parseInt(args[0]);
    final int backlog = Integer.parseInt(args[1]);
    final int size = Integer.parseInt(args[2]);
    final SocketAddress address = new InetSocketAddress("localhost", port);
    channel.socket().setReuseAddress(true);
    channel.socket().setReceiveBufferSize(kb(size));
    channel.configureBlocking(false);

    channel.socket().bind(address, backlog);

    new Multiplexor(channel).start();
  }

  private static class Multiplexor extends Thread {
    private static final int THRESHOLD = 256;
    private final Selector selector;
    private volatile boolean running = true;

    public Multiplexor(ServerSocketChannel serverSocketChannel) throws IOException {
      this(Selector.open());
      serverSocketChannel.register(selector, OP_WRITE);
    }

    private Multiplexor(Selector selector) {
      setName(getClass().getSimpleName());
      this.selector = selector;
      setDaemon(false);
    }

    public void shutdown() {
      selector.wakeup();
      running = false;
    }

    @Override
    public void run() {
      while (running)
        try { doRun(); } catch (Exception e) { /* TODO log error*/ }

      for (SelectionKey key : selector.keys()) silentClose(key.channel());
      silentClose(selector);
    }

    private void doRun() throws Exception {
      final int selected = selector.select(500L);
      int registered = selector.keys().size();

      if (registered == 0 && thenShutdown()) return;

      if (selected > 0) {
        final Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
        while (iterator.hasNext()) {
          final SelectionKey key = iterator.next();
          iterator.remove();
          if (key.isAcceptable()) registered = accept(key, registered);
          if (key.isReadable()) read(key);
          if (key.isWritable()) write(key);
        }
      }
    }

    private boolean thenShutdown() { return !(running = false); }

    private void write(SelectionKey key) {
      //TODO
    }

    private void read(SelectionKey key) {
      //TODO
    }

    private int accept(SelectionKey key, int size) {
      final ServerSocketChannel channel = (ServerSocketChannel) key.channel();
      SocketChannel nChannel = null;
      try {
        nChannel = channel.accept();
        if (nChannel == null) return size;
        nChannel.configureBlocking(false);
        nChannel.register(selector, OP_ACCEPT);
      } catch (IOException e) {
        // TODO logError(channel, e);
        silentClose(nChannel);
        return size;
      }

      return (++size >= THRESHOLD) ? splitToANewMultiplexor(size) : size; // Too many keys for the selector
    }

    private int splitToANewMultiplexor(int size) {
      Selector nSelector = null;

      try { nSelector = Selector.open(); } catch (IOException e) { return size; } // Maybe too many open files.

      final int split = size / 2;
      int i = 0;

      for (SelectionKey key : selector.keys()) {
        if (i == split) break;

        final SelectableChannel channel = key.channel();
        if (channel instanceof ServerSocketChannel) continue;

        key.cancel();

        try {
          channel.register(nSelector, OP_READ);
          i++;
        } catch (ClosedChannelException e) {
          // TODO logError(channel, e);
        }
      }

      new Multiplexor(nSelector).start();
      return split + (size % 2);
    }
  }

  public static void silentClose(Selector selector) {
    if (selector == null) return;
    try { selector.close(); } catch (IOException e) { }
  }

  public static void silentClose(Closeable closeable) {
    // TODO shutdown output if closeable is socket channel
    if (closeable == null) return;
    try { closeable.close(); } catch (IOException e) { }
  }

  public static int kb(int kb) {return kb * 1024;}
}
