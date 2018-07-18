package jdkcompat.io;

import java.io.IOException;
import java.io.OutputStream;

import org.bouncycastle.util.Arrays;

import totalcross.sys.Vm;

public class ByteArrayOutputStream4D extends OutputStream {
  protected byte[] buf;
  protected int count;

  public ByteArrayOutputStream4D() {
    this(32);

  }

  public ByteArrayOutputStream4D(int size) {
    this.buf = new byte[size];
    this.count = 0;
  }

  @Override
  public void write(int b) throws IOException {
    if (count == buf.length) {
      byte[] buf2 = new byte[buf.length + Math.max(buf.length / 10, 32)];
      Vm.arrayCopy(buf, 0, buf2, 0, count);
      buf = buf2;
    }
    buf[count++] = (byte) b;
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    if (count + len >= buf.length) {
      byte[] buf2 = new byte[buf.length + len + Math.max(buf.length / 10, 32)];
      Vm.arrayCopy(buf, 0, buf2, 0, count);
      buf = buf2;
    }
    Vm.arrayCopy(b, off, buf, count, len);
    count += len;
  }
  
  public void reset() {
    count = 0;
  }
  
  public byte[] toByteArray() {
    return Arrays.copyOf(buf, count);
  }
}
