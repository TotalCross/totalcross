package jdkcompat.io;

import java.io.IOException;
import java.io.Writer;

public class StringWriter4D extends Writer {
  private StringBuffer strBuffer;

  public StringWriter4D() {
    strBuffer = new StringBuffer();
  }

  public StringWriter4D(int initialSize) {
    strBuffer = new StringBuffer(initialSize);
  }

  @Override
  public void close() {
  }

  @Override
  public void flush() {
  }

  @Override
  public void write(char[] buff, int offset, int length) throws IOException {
    strBuffer.append(buff, offset, length);
  }

  @Override
  public void write(int charEquiv) throws IOException {
    char c = (char) (0xffff & charEquiv);
    strBuffer.append(c);
  }

  @Override
  public void write(char[] buff) throws IOException {
    strBuffer.append(buff);
  }

  @Override
  public void write(String str) throws IOException {
    strBuffer.append(str);
  }

  @Override
  public void write(String str, int offset, int length) throws IOException {
    strBuffer.append(str, offset, offset + length);
  }

}
