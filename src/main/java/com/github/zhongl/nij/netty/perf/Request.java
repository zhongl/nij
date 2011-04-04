package com.github.zhongl.nij.netty.perf;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl</a> */
public class Request {
  public final int handleMilliseconds;
  public final int responseLength;

  public Request(int handleMilliseconds, int responseLength) {
    this.handleMilliseconds = handleMilliseconds;
    this.responseLength = responseLength;
  }
}
