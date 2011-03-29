package com.github.zhongl.nij.btrace;

public class Probe {
  public static void main(String... args) throws Exception {
    A a = new A();
    for (int i = 0; i < 1000; i++) {
      Thread.sleep(1000);
      a.readAndWrite("fuck");
//      readAndWrite("fuck", i);
    }
  }

  static class A {
    public void readAndWrite(String s) {System.out.println(s);}
  }

  public static void readAndWrite(String s, int i) {System.out.println(s);}
}
