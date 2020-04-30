// Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>   
// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import totalcross.sys.Convert;

/**
 * Stream is the base class for all stream-based I/O classes.
 */
public abstract class Stream extends Connection {
  protected static byte skipBuffer[] = new byte[128];

  /**
   * Reads bytes from the stream. Returns the number of bytes actually read or -1 if the end of the stream was reached.
   * (if applicable to the stream)
   * 
   * @param buf
   *           the byte array to read data into
   * @param start
   *           the start position in the array
   * @param count
   *           the number of bytes to read
   * @throws totalcross.io.IOException
   */
  public abstract int readBytes(byte buf[], int start, int count) throws totalcross.io.IOException;

  /**
   * Writes bytes to the stream. Returns the number of bytes actually written or throws an <code>IOException</code> if an error prevented the write
   * operation from occurring.
   * 
   * @param buf
   *           the byte array to write data from
   * @param start
   *           the start position in the byte array
   * @param count
   *           the number of bytes to write
   * @return the number of bytes actually written
   * @throws totalcross.io.IOException
   */
  public abstract int writeBytes(byte buf[], int start, int count) throws totalcross.io.IOException;

  /**
   * Writes bytes to the stream. Returns the number of bytes actually written or throws an <code>IOException</code> if an error prevented the write
   * operation from occurring.
   * 
   * @param buf
   *           the byte array to write data from
   * @return the number of bytes actually written
   * @throws totalcross.io.IOException
   */
  public int writeBytes(byte buf[]) throws totalcross.io.IOException {
    if (buf.length == 0) {
      return 0; // nothing to write, just return 0.
    }
    return writeBytes(buf, 0, buf.length);
  }

  /**
   * Writes the string to the stream as a byte array. Returns the number of bytes actually written or throws an <code>IOException</code> if an error prevented the write
   * operation from occurring.<br>
   * The String is written as-is, nothing is placed before or after it.
   * 
   * @param string
   *           the string whose bytes will be written
   * @return the number of bytes actually written
   * @throws totalcross.io.IOException
   */
  public int writeBytes(String string) throws totalcross.io.IOException {
    byte buf[] = string.getBytes(); // throws NPE if null.
    if (buf.length == 0) {
      return 0; // nothing to write, just return 0.
    }
    return writeBytes(buf, 0, buf.length);
  }

  /**
   * Writes the given StringBuffer as a byte array, retrieved using Convert.getBytes.<br>
   * The String is written as-is, nothing is placed before or after it.
   * 
   * @param sb
   *           the StringBuffer to get the bytes from
   * @return the number of bytes actually written
   * @throws totalcross.io.IOException
   * @since TotalCross 1.23
   */
  final public int writeBytes(StringBuffer sb) throws totalcross.io.IOException // guich@tc123_44
  {
    byte[] b = Convert.getBytes(sb); // throws NPE if null.
    if (b.length == 0) {
      return 0; // nothing to write, just return 0.
    }
    return writeBytes(b, 0, b.length);
  }

  /**
   * Writes a single byte to the stream.
   *
   * @param b
   *           the byte to write (only least significant byte is written)
   * @return the number of bytes actually written
   * @throws totalcross.io.IOException
   * @since TotalCross 4.1.1
   */
  public int write(int b) throws totalcross.io.IOException {
    return writeBytes(new byte[] { (byte) b }, 0, 1);
  }

  /**
   * Skips over and discards n bytes of data from this stream. The skip method may, for a variety of reasons, end up
   * skipping over some smaller number of bytes, possibly 0. This may result from any of a number of conditions;
   * reaching end of file before n bytes have been skipped is only one possibility. The actual number of bytes skipped
   * is returned. If n is negative, no bytes are skipped, but this may vary among some implementations of skipBytes in
   * classes that inherit them (like File and PDBFile). <br>
   * <br>
   * The skip method of this class uses a static byte array, repeatedly reading into it until n bytes have been read or
   * the end of the stream has been reached. Subclasses are encouraged to provide a more efficient implementation of
   * this method. For instance, the implementation may depend on the ability to seek.
   * 
   * @param n
   *           the number of bytes to be skipped.
   * @return the actual number of bytes skipped.
   * @throws totalcross.io.IOException
   *            if the stream does not support skip, or if some other I/O error occurs.
   */
  public int skipBytes(int n) throws totalcross.io.IOException {
    int readBytesRet;
    int bytesSkipped = 0;

    while (n > 0) {
      int c = n > skipBuffer.length ? skipBuffer.length : n;
      readBytesRet = readBytes(skipBuffer, 0, c);
      if (readBytesRet <= 0) {
        break;
      }
      bytesSkipped += readBytesRet;
      n -= c;
    }

    return bytesSkipped;
  }

  /**
   * @deprecated use {@link #asInputStream()} instead
   */
  @Deprecated
  public InputStream wrapInputStream() {
    return asInputStream();
  }

  public InputStream asInputStream() {
    return new WrapInputStream(this);
  }

  public OutputStream asOutputStream() {
    return new WrapOutputStream(this);
  }

  /**
   * @deprecated use {@link #asStream(InputStream)} instead
   */
  @Deprecated()
  public static Stream wrapInputStreamToStream(InputStream inputStream) {
    return asStream(inputStream);
  }

  public static Stream asStream(InputStream inputStream) {
    if (inputStream instanceof WrapInputStream) {
      return ((WrapInputStream) inputStream).stream;
    }
    return new WrapFromInputStream(inputStream);
  }

  public static Stream asStream(OutputStream outputStream) {
    if (outputStream instanceof WrapOutputStream) {
      return ((WrapOutputStream) outputStream).stream;
    }
    return new WrapFromOutputStream(outputStream);
  }
}

class WrapInputStream extends InputStream {
  Stream stream;

  WrapInputStream(Stream stream) {
    this.stream = stream;
  }

  @Override
  public int read() throws IOException {
    byte[] b = new byte[1];
    if (read(b) == -1) {
      return -1;
    }

    return b[0] & 0xFF;
  }

  @Override
  public int read(byte b[], int off, int len) throws IOException {
    return stream.readBytes(b, off, len);
  }

  @Override
  public void close() throws IOException {
    stream.close();
  }
}

class WrapOutputStream extends OutputStream {
  Stream stream;

  WrapOutputStream(Stream stream) {
    this.stream = stream;
  }

  @Override
  public void write(int b) throws IOException {
    write(new byte[] { (byte) b });
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    do {
      int delta;
      delta = stream.writeBytes(b, off, len);
      off += delta;
    } while (off < len);
  }
  
  @Override
  public void close() throws IOException {
    stream.close();
  }
}

class WrapFromInputStream extends Stream {
  InputStream inputStream;

  WrapFromInputStream(InputStream inputStream) {
    this.inputStream = inputStream;
  }

  @Override
  public int readBytes(byte[] buf, int start, int count) throws totalcross.io.IOException {
    try {
      return inputStream.read(buf, start, count);
    } catch (IOException e) {
      throw new totalcross.io.IOException(e);
    }
  }

  @Override
  public int writeBytes(byte[] buf, int start, int count) throws totalcross.io.IOException {
    throw new totalcross.io.IOException("Can't write to a wrapped input stream");
  }

  @Override
  public int writeBytes(byte[] buf) throws totalcross.io.IOException {
    throw new totalcross.io.IOException("Can't write to a wrapped input stream");
  }

  @Override
  public int writeBytes(String string) throws totalcross.io.IOException {
    throw new totalcross.io.IOException("Can't write to a wrapped input stream");
  }

  @Override
  public int skipBytes(int n) throws totalcross.io.IOException {
    try {
      return (int) inputStream.skip(n);
    } catch (IOException e) {
      throw new totalcross.io.IOException(e);
    }
  }

  @Override
  public InputStream asInputStream() {
    return inputStream;
  }

  @Override
  public void close() throws totalcross.io.IOException {
    try {
      inputStream.close();
    } catch (IOException e) {
      throw new totalcross.io.IOException(e);
    }
  }

}

class WrapFromOutputStream extends Stream {
  OutputStream outputStream;

  WrapFromOutputStream(OutputStream outputStream) {
    this.outputStream = outputStream;
  }

  @Override
  public int writeBytes(byte[] buf, int start, int count) throws totalcross.io.IOException {
    try {
      outputStream.write(buf, start, count);
      return count - start;
    } catch (IOException e) {
      throw new totalcross.io.IOException(e);
    }
  }

  @Override
  public int readBytes(byte[] buf, int start, int count) throws totalcross.io.IOException {
    throw new totalcross.io.IOException("Can't read from a wrapped output stream");
  }

  @Override
  public int skipBytes(int n) throws totalcross.io.IOException {
    throw new totalcross.io.IOException("Can't skip in a wrapped output stream");
  }

  @Override
  public OutputStream asOutputStream() {
    return outputStream;
  }

  @Override
  public void close() throws totalcross.io.IOException {
    try {
      outputStream.close();
    } catch (IOException e) {
      throw new totalcross.io.IOException(e);
    }
  }

}
