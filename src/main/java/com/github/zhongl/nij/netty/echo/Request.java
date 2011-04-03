package com.github.zhongl.nij.netty.echo;

public class Request {
  public final int handleMilliseconds;
  public final int responseLength;

  public Request(int handleMilliseconds, int responseLength) {
    this.handleMilliseconds = handleMilliseconds;
    this.responseLength = responseLength;
  }
}
