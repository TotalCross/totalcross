// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.sys;

import java.io.IOException;
import java.io.OutputStream;

/** Internal byte bridge used by the Java standard output streams. */
public final class VmStandardOutputStream extends OutputStream {
  public static final int OUT = 0;
  public static final int ERR = 1;

  private final int stream;
  private final byte[] singleByte = new byte[1];
  private boolean closed;

  public VmStandardOutputStream(int stream) {
    if (stream != OUT && stream != ERR) {
      throw new IllegalArgumentException("Unknown standard stream: " + stream);
    }
    this.stream = stream;
  }

  @Override
  public void write(int value) throws IOException {
    singleByte[0] = (byte) value;
    write(singleByte, 0, 1);
  }

  @Override
  public void write(byte[] bytes, int offset, int length) throws IOException {
    checkWriteRange(bytes, offset, length);
    ensureOpen();
    if (length > 0 && !standardWrite(stream, bytes, offset, length)) {
      throw new IOException("Unable to write standard stream");
    }
  }

  /** Flushes pending text without requesting durable file synchronization. */
  public void logicalFlush() throws IOException {
    ensureOpen();
    if (!standardFlush(stream, false)) {
      throw new IOException("Unable to flush standard stream");
    }
  }

  @Override
  public void flush() throws IOException {
    ensureOpen();
    if (!standardFlush(stream, true)) {
      throw new IOException("Unable to flush standard stream");
    }
  }

  @Override
  public void close() throws IOException {
    if (closed) {
      return;
    }
    closed = true;
    if (!standardClose(stream)) {
      throw new IOException("Unable to close standard stream");
    }
  }

  private void ensureOpen() throws IOException {
    if (closed) {
      throw new IOException("Stream closed");
    }
  }

  private static void checkWriteRange(byte[] bytes, int offset, int length) {
    if (bytes == null) {
      throw new NullPointerException("bytes");
    }
    if ((offset | length) < 0 || offset > bytes.length - length) {
      throw new IndexOutOfBoundsException();
    }
  }

  private static native boolean standardWrite(int stream, byte[] bytes, int offset, int length);

  private static native boolean standardFlush(int stream, boolean durable);

  private static native boolean standardClose(int stream);
}
