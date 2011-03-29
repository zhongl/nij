package com.github.zhongl.nij.btrace;

import static com.sun.btrace.BTraceUtils.*;

import com.sun.btrace.AnyType;
import com.sun.btrace.annotations.*;

@BTrace
public class Tracer {
  @OnMethod(clazz = "/.*/", method = "readAndWrite", location = @Location(Kind.ENTRY))
  public static void readAndWriteEnter(AnyType... arg) {
    println("begin");
  }

  @OnMethod(clazz = "/.*/", method = "readAndWrite", location = @Location(Kind.RETURN))
  public static void readAndWriteExit(AnyType... arg) {
    println("end");
  }
}
