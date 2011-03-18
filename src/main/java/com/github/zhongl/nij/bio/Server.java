package com.github.zhongl.nij.bio;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.github.zhongl.nij.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server {
  private static volatile boolean running = true;
  private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);
  private static final ExecutorService SERVICE = Executors
      .newFixedThreadPool(Integer.getInteger("thread.pool.size", Runtime.getRuntime().availableProcessors() * 2));
  private static final byte[] BUFFER = new byte[1024];
  private static final byte[] RESPONSE = "HTTP/1.0 200 OK\r\nContent-Length:1\r\n\r\na".getBytes();
  private static final ByteBuffer BUFFER_IN = ByteBuffer.allocateDirect(1024);
  private static final ByteBuffer BUFFER_OUT;

  static {
    BUFFER_OUT = ByteBuffer.allocateDirect(RESPONSE.length);
    BUFFER_OUT.put(RESPONSE);
    BUFFER_OUT.flip();
  }

  public static void main(String... args) throws Exception {
    final String host = args[0];
    final int port = Integer.parseInt(args[1]);
    final int backlog = Integer.parseInt(args[2]);
    final int size = Integer.parseInt(args[3]);
    final String acceptorType = args[4];
    final String handlerType = args[5];
    final SocketAddress address = new InetSocketAddress(host, port);

    Runtime.getRuntime().addShutdownHook(new Thread("shutdow-hook") {
      @Override
      public void run() { running = false; }
    });


    System.out.println("Started at " + host + ":" + port +
        " with backlog: " + backlog + " receive buffer: " + size + "k.");

    final Acceptor acceptor = AcceptorType.valueOf(acceptorType.toUpperCase()).build(address, size, backlog);
    final Handler handler = Handler.valueOf(handlerType.toUpperCase());

    while (running) {
      try { handle(acceptor.accept(), handler); } catch (SocketTimeoutException e) { }
    }
    silentClose(acceptor);
    SERVICE.shutdownNow();
    System.out.println("Stopped.");
  }

  public static void handle(final Socket accept, final Handler handler) {
    SERVICE.execute(new Runnable() {
      @Override
      public void run() {
        try {
          accept.setTcpNoDelay(true);
          accept.setSendBufferSize(1 * 1024);
          handler.readAndWrite(accept);
        } catch (IOException e) {
          LOGGER.error("Unexpected error", e);
        } finally {
          silentClose(accept);
        }
      }
    });
  }

  enum Handler {
    S {
      @Override
      void readAndWrite(Socket socket) throws IOException {
        final InputStream inputStream = new BufferedInputStream(socket.getInputStream());
        final OutputStream outputStream = new BufferedOutputStream(socket.getOutputStream());
        inputStream.read(BUFFER);
        outputStream.write(RESPONSE);
        outputStream.close();
        inputStream.close();
      }
    }, C {
      @Override
      void readAndWrite(Socket socket) throws IOException {
        final ReadableByteChannel readableByteChannel = Channels.newChannel(socket.getInputStream());
        final WritableByteChannel writableByteChannel = Channels.newChannel(socket.getOutputStream());
        readableByteChannel.read(BUFFER_IN.duplicate());
        writableByteChannel.write(BUFFER_OUT.asReadOnlyBuffer());
        socket.shutdownOutput();
      }
    };

    abstract void readAndWrite(Socket socket) throws IOException;
  }

  private static void silentClose(Closeable closeable) { try {closeable.close(); } catch (IOException e) { } }

  private static void silentClose(Socket accept) { try { accept.close(); } catch (IOException e1) { } }

  public enum AcceptorType {
    S {
      @Override
      Acceptor build(SocketAddress address, int size, int backlog) throws IOException {
        return new SocketAcceptor(address, size, backlog);
      }
    }, C {
      @Override
      Acceptor build(SocketAddress address, int size, int backlog) throws IOException {
        return new ChannelAcceptor(address, size, backlog);
      }
    };

    abstract Acceptor build(SocketAddress address, int size, int backlog) throws IOException;
  }

  public interface Acceptor extends Closeable {
    Socket accept() throws IOException;
  }

  private static class ChannelAcceptor implements Acceptor {
    private final Selector selector;
    private final ServerSocketChannel channel;

    public ChannelAcceptor(SocketAddress address, int size, int backlog) throws IOException {
      this.selector = Selector.open();
      this.channel = ServerSocketChannel.open();
      channel.socket().setReceiveBufferSize(Utils.kb(size));
      channel.socket().setReuseAddress(true);
      channel.socket().bind(address, backlog);
      channel.configureBlocking(false);

      channel.register(selector, SelectionKey.OP_ACCEPT);
    }

    @Override
    public Socket accept() throws IOException {
      while (selector.select(500L) == 0) ;
      selector.selectedKeys().clear();
      return channel.accept().socket();
    }

    @Override
    public void close() throws IOException {
      selector.close();
      channel.close();
    }
  }

  private static class SocketAcceptor implements Acceptor {
    private ServerSocket socket;

    public SocketAcceptor(SocketAddress address, int size, int backlog) throws IOException {
      socket = new ServerSocket();
      socket.setReceiveBufferSize(Utils.kb(size));
      socket.setReuseAddress(true);
      socket.setSoTimeout(500);
      socket.bind(address, backlog);
    }

    @Override
    public Socket accept() throws IOException {return socket.accept();}

    @Override
    public void close() throws IOException { socket.close(); }
  }
}
