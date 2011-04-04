package com.github.zhongl.nij.netty.perf;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl</a> */
public class PerfServer {

  public static void main(String[] args) throws Exception {
    final String host = args[0];
    final int port = Integer.parseInt(args[1]);

    final ServerBootstrap bootstrap = new ServerBootstrap(
        new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));

    bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
      public ChannelPipeline getPipeline() throws Exception {
        return Channels.pipeline(new RequestDecoder(), new PerfServerHandler());
      }
    });

    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        bootstrap.releaseExternalResources();
      }
    });

    bootstrap.bind(new InetSocketAddress(host, port));
    System.out.println("Netty PerfServer started");
  }
}
