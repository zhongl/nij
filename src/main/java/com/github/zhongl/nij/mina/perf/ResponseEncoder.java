package com.github.zhongl.nij.mina.perf;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

public class ResponseEncoder extends ProtocolEncoderAdapter {

  @Override
  public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
    out.write(responseBuffer((Integer) message));
  }

  private IoBuffer responseBuffer(int responseLength) {
    IoBuffer buffer = IoBuffer.allocate(responseLength);
    for (int i = 0; i < responseLength; i++) buffer.put((byte) 'a');
    buffer.flip();
    return buffer;
  }

}
