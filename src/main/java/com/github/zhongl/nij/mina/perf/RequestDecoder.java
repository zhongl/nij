package com.github.zhongl.nij.mina.perf;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl</a> */
public class RequestDecoder extends CumulativeProtocolDecoder {

  @Override
  protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
    if (in.remaining() < 4) return false;
    final int len = in.getInt();
    if (in.remaining() < len) return false;
    final int handleMilliseconds = in.getInt();
    final int responseLength = in.getInt();
    in.get(new byte[len - 8]);
    out.write(new Request(handleMilliseconds, responseLength));
    return true;
  }
}

