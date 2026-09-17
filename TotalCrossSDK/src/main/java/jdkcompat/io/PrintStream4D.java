// Copyright (C) 2017-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package jdkcompat.io;

import java.io.Closeable;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import totalcross.sys.VmStandardOutputStream;
import totalcross.util.concurrent.Lock;

public class PrintStream4D extends FilterOutputStream implements Appendable, Closeable {
  private final boolean autoFlush;
  protected final Lock lock = new Lock();
  private boolean errorFound;
  private boolean closed;

  public PrintStream4D(OutputStream out) {
    this(out, false);
  }

  public PrintStream4D(OutputStream out, boolean autoFlush) {
    super(out);
    this.autoFlush = autoFlush;
  }

  @Override
  public void write(int b) {
    synchronized (lock) {
      if (!ensureOpen()) {
        return;
      }
      try {
        out.write(b);
        if (autoFlush && (b & 0xff) == '\n') {
          logicalFlush();
        }
      } catch (IOException e) {
        setError();
      }
    }
  }

  @Override
  public void write(byte[] bytes) {
    write(bytes, 0, bytes.length);
  }

  @Override
  public void write(byte[] bytes, int offset, int length) {
    checkWriteRange(bytes, offset, length);
    synchronized (lock) {
      if (!ensureOpen()) {
        return;
      }
      try {
        out.write(bytes, offset, length);
        if (autoFlush) {
          logicalFlush();
        }
      } catch (IOException e) {
        setError();
      }
    }
  }

  public void writeBytes(byte[] bytes) {
    write(bytes, 0, bytes.length);
  }

  public void print(boolean value) {
    print(String.valueOf(value));
  }

  public void print(char value) {
    print(String.valueOf(value));
  }

  public void print(int value) {
    print(String.valueOf(value));
  }

  public void print(long value) {
    print(String.valueOf(value));
  }

  public void print(float value) {
    print(String.valueOf(value));
  }

  public void print(double value) {
    print(String.valueOf(value));
  }

  public void print(char[] value) {
    print(value == null ? null : new String(value));
  }

  public void print(String value) {
    writeString(value);
  }

  public void print(Object value) {
    print(String.valueOf(value));
  }

  public void println() {
    printlnValue(null, false);
  }

  public void println(boolean value) {
    printlnValue(String.valueOf(value), true);
  }

  public void println(char value) {
    printlnValue(String.valueOf(value), true);
  }

  public void println(int value) {
    printlnValue(String.valueOf(value), true);
  }

  public void println(long value) {
    printlnValue(String.valueOf(value), true);
  }

  public void println(float value) {
    printlnValue(String.valueOf(value), true);
  }

  public void println(double value) {
    printlnValue(String.valueOf(value), true);
  }

  public void println(char[] value) {
    printlnValue(value == null ? null : new String(value), true);
  }

  public void println(String value) {
    printlnValue(value, true);
  }

  public void println(Object value) {
    printlnValue(String.valueOf(value), true);
  }

  @Override
  public PrintStream4D append(char value) {
    print(value);
    return this;
  }

  @Override
  public PrintStream4D append(CharSequence value) {
    print(String.valueOf(value));
    return this;
  }

  @Override
  public PrintStream4D append(CharSequence value, int start, int end) {
    String text = String.valueOf(value);
    print(text.substring(start, end));
    return this;
  }

  @Override
  public void flush() {
    synchronized (lock) {
      if (closed) {
        return;
      }
      try {
        out.flush();
      } catch (IOException e) {
        setError();
      }
    }
  }

  @Override
  public void close() {
    synchronized (lock) {
      if (closed) {
        return;
      }
      closed = true;
      try {
        out.flush();
      } catch (IOException e) {
        setError();
      }
      try {
        out.close();
      } catch (IOException e) {
        setError();
      }
    }
  }

  public boolean checkError() {
    flush();
    synchronized (lock) {
      return errorFound;
    }
  }

  protected void clearError() {
    synchronized (lock) {
      errorFound = false;
    }
  }

  protected void setError() {
    errorFound = true;
  }

  private void writeString(String value) {
    byte[] bytes = String.valueOf(value).getBytes(StandardCharsets.UTF_8);
    synchronized (lock) {
      if (!ensureOpen()) {
        return;
      }
      try {
        out.write(bytes, 0, bytes.length);
        if (autoFlush && containsNewline(value)) {
          logicalFlush();
        }
      } catch (IOException e) {
        setError();
      }
    }
  }

  private void printlnValue(String value, boolean hasValue) {
    synchronized (lock) {
      if (!ensureOpen()) {
        return;
      }
      try {
        if (hasValue) {
          byte[] bytes = String.valueOf(value).getBytes(StandardCharsets.UTF_8);
          out.write(bytes, 0, bytes.length);
        }
        out.write('\n');
        if (autoFlush) {
          logicalFlush();
        }
      } catch (IOException e) {
        setError();
      }
    }
  }

  private boolean ensureOpen() {
    if (closed) {
      errorFound = true;
      return false;
    }
    return true;
  }

  private void logicalFlush() throws IOException {
    if (out instanceof VmStandardOutputStream) {
      ((VmStandardOutputStream) out).logicalFlush();
    } else {
      out.flush();
    }
  }

  private static boolean containsNewline(String value) {
    return value != null && value.indexOf('\n') >= 0;
  }

  private static void checkWriteRange(byte[] bytes, int offset, int length) {
    if (bytes == null) {
      throw new NullPointerException("bytes");
    }
    if ((offset | length) < 0 || offset > bytes.length - length) {
      throw new IndexOutOfBoundsException();
    }
  }
}
