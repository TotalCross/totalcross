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

/** 
 * DataStreamLE can be attached to any Stream such as a
 * PortConnector, PDBFile, or ByteArrayStream, which lets you read and write
 * standard Java data types like int, double, and String in a simple
 * manner. Here's an example:
 *
 * <pre>
 * PortConnector port = new PortConnector(9600, 0);
 * DataStream ds = new DataStream(port);
 * ds.writeString(&quot;Hello&quot;);
 * int status = ds.readUnsignedByte();
 * if (status == 1)
 * {
 *    ds.writeString(&quot;Pi&quot;);
 *    ds.writeDouble(3.14);
 * }
 * port.close();
 * </pre>
 *
 * <br>
 * <b>Important!</b>: All methods
 * read and write numeric data in the little endian format (Intel format).
 * For more information, see <a target=_blank
 * href='http://www.webopedia.com/TERM/l/little_endian.html'>this</a>.
 * @see DataStream
 */
public class DataStreamLE extends DataStream {
  /**
   * Constructs a new DataStreamLE which sits upon the given stream using litle
   * endian notation for multibyte values.
   *
   * @param stream the base stream
   */
  public DataStreamLE(Stream stream) {
    super(stream);
  }

  @Override
  public int readInt() throws EOFException, totalcross.io.IOException {
    byte[] b = buffer;
    readBytesInternal(b, 0, 4, true);
    return (((b[3] & 0xFF) << 24) | ((b[2] & 0xFF) << 16) | ((b[1] & 0xFF) << 8) | (b[0] & 0xFF));
  }

  @Override
  public short readShort() throws EOFException, totalcross.io.IOException {
    byte[] b = buffer;
    readBytesInternal(b, 0, 2, true);
    return (short) (((b[1] & 0xFF) << 8) | (b[0] & 0xFF));
  }

  @Override
  public long readLong() throws EOFException, totalcross.io.IOException {
    long l1 = (long) readInt() & 0xFFFFFFFFL;
    long l2 = (long) readInt() & 0xFFFFFFFFL;
    return (l2 << 32) | l1;
  }

  @Override
  public int readUnsignedShort() throws EOFException, totalcross.io.IOException {
    byte[] b = buffer;
    readBytesInternal(b, 0, 2, true);
    return (((b[1] & 0xFF) << 8) | (b[0] & 0xFF));
  }

  @Override
  public int writeInt(int i) throws totalcross.io.IOException {
    byte[] b = buffer;
    b[0] = (byte) i;
    i >>= 8; // guich@300_40
    b[1] = (byte) i;
    i >>= 8;
    b[2] = (byte) i;
    i >>= 8;
    b[3] = (byte) i;
    return writeBytesInternal(b, 0, 4);
  }

  @Override
  public int writeShort(int i) throws totalcross.io.IOException {
    byte[] b = buffer;
    b[0] = (byte) i;
    i >>= 8; // guich@300_40
    b[1] = (byte) i;
    return writeBytesInternal(b, 0, 2);
  }

  @Override
  public int writeLong(long l) throws totalcross.io.IOException {
    return writeInt((int) l) + writeInt((int) (l >> 32));
  }

  @Override
  public char readChar() throws EOFException, totalcross.io.IOException // guich@421_31
  {
    byte[] b = buffer;
    readBytesInternal(b, 0, 2, true);
    return (char) (((b[1] & 0xFF) << 8) | (b[0] & 0xFF));
  }

  @Override
  public int writeChar(char c) throws totalcross.io.IOException // guich@421_31
  {
    byte[] b = buffer;
    b[0] = (byte) c;
    c >>= 8;
    b[1] = (byte) c;
    return writeBytesInternal(b, 0, 2);
  }

  /** 
   * Reads a char array with the given length.
   * 
   * @param chars An already created chars array.
   * @param len The array length.
   * @throws EOFException
   * @throws totalcross.io.IOException
   * @since TotalCross 1.01 
   */
  @Override
  public void readChars(char[] chars, int len) throws EOFException, totalcross.io.IOException {
    byte[] bytes = buffer;
    int buflen = bytes.length / 2, start = 0;
    while (len > 0) {
      int avail = (len > buflen ? buflen : len) * 2;
      readBytesInternal(bytes, 0, avail, true);
      for (int i = 0; i < avail; i += 2) {
        chars[start++] = (char) (((bytes[i + 1] & 0xFF) << 8) | (bytes[i] & 0xFF));
      }
      len -= avail / 2;
    }
  }

  @Override
  protected char[] readChars(int len) throws EOFException, totalcross.io.IOException {
    char[] chars = new char[len];
    byte[] bytes = buffer;
    int buflen = bytes.length / 2, start = 0;
    while (len > 0) {
      int avail = (len > buflen ? buflen : len) * 2;
      readBytesInternal(bytes, 0, avail, true);
      for (int i = 0; i < avail; i += 2) {
        chars[start++] = (char) (((bytes[i + 1] & 0xFF) << 8) | (bytes[i] & 0xFF));
      }
      len -= avail / 2;
    }
    return chars;
  }

  @Override
  public int writeChars(char[] chars, int start, int len, int lenSize) throws totalcross.io.IOException {
    int n = 0, c;
    if (len < 0) {
      len = chars == null ? 0 : chars.length;
    }
    len -= start;
    if (len < 0) {
      len = 0; // eisvogel@421_70
    }
    if (lenSize == 2) {
      if (len > 65535) {
        throw new IOException("String size " + len + " is too big to use with writeChars!");
      }
      n = writeShort(len);
    } else if (lenSize == 4) {
      n = writeInt(len);
    } else if (lenSize == 1) {
      if (len > 255) {
        throw new IOException("String size " + len + " is too big to use with writeChars!");
      }
      n = writeByte(len);
    }
    byte[] bytes = buffer;
    int buflen = bytes.length / 2;
    while (len > 0) {
      int avail = (len > buflen ? buflen : len) * 2;
      for (int i = 0; i < avail; i += 2) {
        c = chars[start++];
        bytes[i] = (byte) c;
        bytes[i + 1] = (byte) (c >> 8);
      }
      n += writeBytesInternal(bytes, 0, avail);
      len -= avail / 2;
    }
    return n;
  }

  @Override
  public int writeChars(String s, int len) throws totalcross.io.IOException {
    int c;
    int start = 0;
    int n = writeShort(len);
    byte[] bytes = buffer;
    int buflen = bytes.length / 2;
    while (len > 0) {
      int avail = (len > buflen ? buflen : len) * 2;
      for (int i = 0; i < avail; i += 2) {
        c = s.charAt(start++);
        bytes[i] = (byte) c;
        bytes[i + 1] = (byte) (c >> 8);
      }
      n += writeBytesInternal(bytes, 0, avail);
      len -= avail / 2;
    }
    return n;
  }
}
