// Copyright (C) 2003 Rob Nielsen
// Copyright (C) 2003-2013 SuperWaba Ltda.
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

import totalcross.sys.Convert;
import totalcross.sys.Vm;

/** 
 * DataStream can be attached to any Stream such as a
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
 * read and write numeric data in the big endian format (Java format).
 * For more information, see <a target=_blank
 * href='http://www.webopedia.com/TERM/b/big_endian.html'>this</a>.
 * @see DataStreamLE
 */
public class DataStream extends Stream {
  /** The underlying stream, from where bytes are read and written. */
  protected Stream stream;

  /** a four byte array for reading and writing numbers */
  protected byte[] buffer = new byte[256]; // starts with 256 bytes since readSmallString uses it

  static protected byte zeros[] = new byte[128]; // tc100

  /** Message thrown when an end of stream is reached when reading. */
  public static final String EOSMessage = "End of stream";

  private final boolean ensureWrite;

  /**
   * Constructs a new DataStream which sits upon the given stream using big
   * endian notation for multibyte values.
   *
   * @param stream the base stream, from where bytes are read and written.
   */
  public DataStream(Stream stream) {
    this(stream, false);
  }

  /**
   * Constructs a new DataStream which sits upon the given stream using big endian notation for multibyte values.
   * 
   * @param stream
   *           the base stream, from where bytes are read and written.
   * @param ensureWrite
   *           if true, write operations are blocked until all the requested data is written to the underlying stream.
   */
  public DataStream(Stream stream, boolean ensureWrite) {
    if (stream == null) {
      throw new NullPointerException("Argument 'stream' cannot be null");
    }
    this.stream = stream;
    this.ensureWrite = ensureWrite;
  }

  /**
   * Closes the stream. This just calls the close method of the attached stream,
   * thus closing it. Usually, this method may never be called. Remember that closing
   * a stream twice might throw an IOException, so if you call this close method, don't
   * call the base Stream's.
   *
   * @throws totalcross.io.IOException
   * @throws IOException
   */
  @Override
  public void close() throws totalcross.io.IOException {
    if (stream != null) {
      stream.close();
    }
  }

  /** Pads the stream writing 0 the given number of times.
   * @param n The number of zeros to be written
   * @throws totalcross.io.IOException */
  final public int pad(int n) throws totalcross.io.IOException {
    if (zeros.length < n) {
      zeros = new byte[n + 16]; // guich@200 grows the buffer if necessary - corrected by mike roberts
    }
    return writeBytesInternal(zeros, 0, n);
  }

  /**
   * Reads a byte from the stream as a boolean. True is returned if the byte is
   * not zero, false if it is.
   *
   * @return the boolean value read.
   * @throws EOFException
   * @throws totalcross.io.IOException
   */
  final public boolean readBoolean() throws EOFException, totalcross.io.IOException {
    readBytesInternal(buffer, 0, 1, true);
    return buffer[0] != 0;
  }

  /**
   * Reads a single byte from the stream. The returned value will range from
   * -128 to 127.
   *
   * @return the read byte
   * @throws EOFException
   * @throws totalcross.io.IOException
   */
  final public byte readByte() throws EOFException, totalcross.io.IOException {
    readBytesInternal(buffer, 0, 1, true);
    return buffer[0];
  }

  @Override
  final public int readBytes(byte buf[], int start, int count) throws totalcross.io.IOException {
    return readBytesInternal(buf, start, count, false);
  }

  /**
   * Reads bytes from the stream. Returns the number of bytes actually read.
   *
   * @param buf the byte array to read data into
   * @throws totalcross.io.IOException  If an error prevented the read operation from occurring.
   */
  final public int readBytes(byte buf[]) throws totalcross.io.IOException {
    readBytesInternal(buf, 0, buf.length, false);
    return buf.length;
  }

  /**
   * Reads a float value from the stream as four bytes in IEEE 754 format.
   *
   * @return the float value as a double.
   * @throws EOFException
   * @throws totalcross.io.IOException
   */
  public double readFloat() throws EOFException, totalcross.io.IOException {
    return Convert.intBitsToDouble(readInt());
  }

  /**
   * Reads an integer from the stream as four bytes. The returned value will
   * range from -2147483648 to 2147483647.
   *
   * @return the integer value
   * @throws EOFException
   * @throws totalcross.io.IOException
   */
  public int readInt() throws EOFException, totalcross.io.IOException {
    byte[] b = buffer;
    readBytesInternal(b, 0, 4, true);
    return (((b[0] & 0xFF) << 24) | ((b[1] & 0xFF) << 16) | ((b[2] & 0xFF) << 8) | (b[3] & 0xFF));
  }

  /**
   * Reads a short from the stream as two bytes. The returned value will range
   * from -32768 to 32767.
   *
   * @return the short value
   * @throws EOFException
   * @throws totalcross.io.IOException
   */
  public short readShort() throws EOFException, totalcross.io.IOException {
    byte[] b = buffer;
    readBytesInternal(b, 0, 2, true);
    return (short) (((b[0] & 0xFF) << 8) | (b[1] & 0xFF));
  }

  /**
   * Reads a double.
   *
   * @return the double value
   * @throws EOFException
   * @throws totalcross.io.IOException
   * @since SuperWaba 2.0
   */
  public double readDouble() throws EOFException, totalcross.io.IOException {
    return Convert.longBitsToDouble(readLong());
  }

  /**
   * Reads a long.
   *
   * @return the long value
   * @throws EOFException
   * @throws totalcross.io.IOException
   * @since SuperWaba 2.0
   */
  public long readLong() throws EOFException, totalcross.io.IOException {
    long l1 = (long) readInt() & 0xFFFFFFFFL;
    long l2 = (long) readInt() & 0xFFFFFFFFL;
    return (l1 << 32) | l2;
  }

  /**
   * Reads a single unsigned byte from the stream. The returned value will
   * range from 0 to 255. Use writeByte to write the unsigned byte.
   *
   * @return the read byte.
   * @throws EOFException
   * @throws totalcross.io.IOException
   * @see #writeByte(int)
   */
  final public int readUnsignedByte() throws EOFException, totalcross.io.IOException {
    readBytesInternal(buffer, 0, 1, true);
    return buffer[0] & 0xFF;
  }

  /**
   * Reads an unsigned short from the stream as two bytes. The returned value
   * will range from 0 to 65535. Use writeShort to write the unsigned short.
   *
   * @return the short
   * @throws EOFException
   * @throws totalcross.io.IOException
   * @see #writeShort(int)
   */
  public int readUnsignedShort() throws EOFException, totalcross.io.IOException {
    byte[] b = buffer;
    readBytesInternal(b, 0, 2, true);
    return (((b[0] & 0xFF) << 8) | (b[1] & 0xFF));
  }

  /**
   * Reads an unsigned integer from the stream as four bytes.<br>
   * The returned value will range from 0 to 4294967295.
   * 
   * @return the integer value stored in a long
   * @throws EOFException
   * @throws totalcross.io.IOException
   * @since TotalCross 1.27
   */
  public long readUnsignedInt() throws EOFException, totalcross.io.IOException {
    byte[] b = buffer;
    readBytesInternal(b, 0, 4, true);
    return ((((long) (b[0] & 0xFF)) << 24) | (((long) (b[1] & 0xFF)) << 16) | ((b[2] & 0xFF) << 8) | (b[3] & 0xFF));
  }

  /**
   * Writes a boolean to the stream as a byte. True values are written as 1 and
   * false values as 0.
   *
   * @param bool the boolean to write
   * @return the number of bytes written: 1
   * @throws totalcross.io.IOException
   */
  final public int writeBoolean(boolean bool) throws totalcross.io.IOException {
    buffer[0] = (bool ? (byte) 1 : (byte) 0);
    return writeBytesInternal(buffer, 0, 1);
  }

  /**
   * Writes a single byte to the stream.
   *
   * @param by
   *           the byte to write
   * @return the number of bytes written: 1
   * @throws totalcross.io.IOException
   */
  final public int writeByte(byte by) throws totalcross.io.IOException {
    buffer[0] = by;
    return writeBytesInternal(buffer, 0, 1);
  }

  /**
   * Writes a single byte to the stream.
   *
   * @param by
   *           the byte to write (only least significant byte is written)
   * @return the number of bytes written: 1
   * @throws totalcross.io.IOException
   */
  final public int writeByte(int by) throws totalcross.io.IOException {
    buffer[0] = (byte) by;
    return writeBytesInternal(buffer, 0, 1);
  }

  @Override
  final public int writeBytes(byte buf[], int start, int count) throws totalcross.io.IOException {
    return writeBytesInternal(buf, start, count);
  }

  /**
   * Writes bytes to the stream.
   *
   * @param buf the byte array to write data from
   * @return the number of bytes written: buf.length
   * @throws totalcross.io.IOException
   */
  @Override
  final public int writeBytes(byte buf[]) throws totalcross.io.IOException {
    return writeBytesInternal(buf, 0, buf.length);
  }

  /**
   * Writes a float value to the stream as four bytes in IEEE 754 format
   *
   * @param f the float to write
   * @return the number of bytes written: 4
   * @throws totalcross.io.IOException
   */
  public int writeFloat(double f) throws totalcross.io.IOException {
    return writeInt(Convert.doubleToIntBits(f));
  }

  /**
   * Writes an integer to the stream as four bytes.
   *
   * @param i
   *           the integer to write
   * @return the number of bytes written: 4
   * @throws totalcross.io.IOException
   */
  public int writeInt(int i) throws totalcross.io.IOException {
    byte[] b = buffer;
    b[3] = (byte) i;
    i >>= 8; // guich@300_40
    b[2] = (byte) i;
    i >>= 8;
    b[1] = (byte) i;
    i >>= 8;
    b[0] = (byte) i;
    return writeBytesInternal(b, 0, 4);
  }

  /**
   * Writes a short to the stream as two bytes.
   *
   * @param i
   *           the short to write
   * @return the number of bytes written: 2
   * @throws totalcross.io.IOException
   */
  public int writeShort(int i) throws totalcross.io.IOException {
    byte[] b = buffer;
    b[1] = (byte) i;
    i >>= 8; // guich@300_40
    b[0] = (byte) i;
    return writeBytesInternal(b, 0, 2);
  }

  /**
   * Reads a string, converting from Pascal (the length is placed before the
   * string) to Java format.
   * <p>The String size is limited to 65535 characters.
   *
   * @return a zero or more length string. null is never returned.
   * @throws EOFException
   * @throws totalcross.io.IOException
   */
  public String readString() throws EOFException, totalcross.io.IOException {
    int len = readUnsignedShort(); // guich@341_2
    if (len == 0) {
      return "";
    }

    if (buffer.length < len) {
      buffer = new byte[len + 16]; // guich@200 grows the buffer if necessary - corrected by mike mcroberts
    }

    readBytesInternal(buffer, 0, len, true);
    return new String(totalcross.sys.Convert.charConverter.bytes2chars(buffer, 0, len)); // eisvogel@450_17: make sure that desktop and device use the same convertion algorithm
  }

  /**
   * Reads a big string, converting from Pascal (the length is placed before the
   * string) to Java format.
   * <p>The String size is limited to 2,147,483,647 characters.
   *
   * @return a zero or more length string. null is never returned.
   * @throws EOFException
   * @throws totalcross.io.IOException
   * @since TotalCross 1.0
   */
  public String readBigString() throws EOFException, totalcross.io.IOException {
    int len = readInt(); // guich@341_2
    if (len == 0) {
      return "";
    }

    if (buffer.length < len) {
      buffer = new byte[len + 16]; // guich@200 grows the buffer if necessary - corrected by mike mcroberts
    }

    readBytesInternal(buffer, 0, len, true);
    return new String(totalcross.sys.Convert.charConverter.bytes2chars(buffer, 0, len)); // eisvogel@450_17: make sure that desktop and device use the same convertion algorithm
  }

  /**
   * Reads an array of Strings.
   * <p>The array length is limited to 65535 elements.
   *
   * @return a zero or more length array. null is never returned.
   * @throws EOFException
   * @throws totalcross.io.IOException
   */
  public String[] readStringArray() throws EOFException, totalcross.io.IOException {
    int size = (int) readUnsignedShort(), i = 0;
    String[] a = new String[size];
    while (--size >= 0) {
      a[i++] = readString();
    }
    return a;
  }

  /**
   * Reads an array of Strings.
   *
   * @param size The of the string array.
   * @return a zero or more length array. null is never returned.
   * @throws EOFException
   * @throws totalcross.io.IOException
   */
  public String[] readStringArray(int size) throws EOFException, totalcross.io.IOException {
    int i = 0;
    String[] a = new String[size];
    while (--size >= 0) {
      a[i++] = readString();
    }
    return a;
  }

  /**
   * Writes the string into the stream, converting it from Java format
   * to Pascal format (the length is placed before the string).
   * <p>The String size is limited to 65535 characters.
   *
   * @return the number of bytes written.
   * @throws totalcross.io.IOException
   */
  public int writeString(String s) throws totalcross.io.IOException {
    int n = 0, l;
    if (s != null && (l = s.length()) > 0) // eisvogel@421_70: don't let write more than the pdb can handle
    {
      char[] ac = s.toCharArray();
      if (l > 65535) {
        throw new IOException("String size " + l + " is too big to use with writeString!");
      }
      byte[] c = totalcross.sys.Convert.charConverter.chars2bytes(ac, 0, ac.length); // eisvogel@450_17
      n += writeShort(c.length);
      n += writeBytesInternal(c, 0, c.length);
    } else {
      n += writeShort(0);
    }
    return n;
  }

  /**
   * Writes the string into the stream, converting it from Java format
   * to Pascal format (the length is placed before the string).
   * <p>The String size is limited to 2,147,483,647 characters.
   *
   * @return the number of bytes written.
   * @throws totalcross.io.IOException
   * @since TotalCross 1.0
   */
  public int writeBigString(String s) throws totalcross.io.IOException {
    int n = 0;
    if (s != null && s.length() > 0) // eisvogel@421_70: don't let write more than the pdb can handle
    {
      char[] ac = s.toCharArray();
      byte[] c = totalcross.sys.Convert.charConverter.chars2bytes(ac, 0, ac.length); // eisvogel@450_17
      n += writeInt(c.length);
      n += writeBytesInternal(c, 0, c.length);
    } else {
      n += writeInt(0);
    }
    return n;
  }

  /**
   * writes the string array into the stream
   *
   * @return the number of bytes written.
   * @throws totalcross.io.IOException
   */
  public int writeStringArray(String[] v) throws totalcross.io.IOException {
    int n = 0;
    if (v == null || v.length == 0) {
      n += writeShort(0);
    } else {
      n += writeShort(v.length);
      for (int i = 0; i < v.length; i++) {
        n += writeString(v[i]);
      }
    }
    return n;
  }

  /**
   * Writes a double.
   *
   * @return the number of bytes written: 8
   * @throws totalcross.io.IOException
   * @since SuperWaba 2.0
   */
  public int writeDouble(double d) throws totalcross.io.IOException {
    return writeLong(Convert.doubleToLongBits(d));
  }

  /**
   * Writes a long.
   * @throws totalcross.io.IOException
   *
   * @since SuperWaba 2.0
   */
  public int writeLong(long l) throws totalcross.io.IOException {
    return writeInt((int) (l >> 32)) + writeInt((int) l);
  }

  /**
   * Reads a C-style string from the stream. This is a NUL (0) terminated
   * series of characters. This format is commonly used by other 
   * applications. Note that if you're creating your own stream, choose
   * readString instead of readCString, because readCString is *much* slower.
   * Also, this method does not handle correctly unicode characters.
   *
   * @return the loaded String
   * @throws EOFException
   * @throws totalcross.io.IOException
   */
  public String readCString() throws EOFException, totalcross.io.IOException {
    // we have to read one character at a time.
    byte b[] = buffer, c;
    byte[] buf = new byte[1];
    int size = 0;
    while (true) {
      readBytesInternal(buf, 0, 1, true);
      c = buf[0];
      if (c == 0) {
        break;
      }
      if (size == b.length) // grow the array if necessary
      {
        byte[] temp = new byte[b.length * 3 / 2];
        Vm.arrayCopy(b, 0, temp, 0, size);
        buffer = b = temp;
      }
      b[size++] = c;
    }
    return new String(totalcross.sys.Convert.charConverter.bytes2chars(b, 0, size)); // eisvogel@450_17
  }

  /**
   * Writes a C-style string to the stream. This means that all the characters
   * of the string are written out, followed by a NUL (0) character. This
   * format is commonly used by other applications. 
   *
   * @param s the string to write
   * @return the number of bytes written.
   * @throws totalcross.io.IOException
   */
  final public int writeCString(String s) throws totalcross.io.IOException {
    if (s == null) {
      return writeByte(0);
    }

    int n = 0;
    char[] ac = s.toCharArray();
    byte[] ab = totalcross.sys.Convert.charConverter.chars2bytes(ac, 0, ac.length); // eisvogel@450_17
    n += writeBytesInternal(ab, 0, ab.length);
    n += writeByte(0);
    return n;
  }

  /**
   * Reads a two-byte character.
   *
   * @throws EOFException
   * @throws totalcross.io.IOException
   * @since SuperWaba 4.21
   */
  public char readChar() throws EOFException, totalcross.io.IOException // guich@421_31
  {
    byte[] b = buffer;
    readBytesInternal(b, 0, 2, true);
    return (char) (((b[0] & 0xFF) << 8) | (b[1] & 0xFF));
  }

  /**
   * Writes a two-byte character.
   *
   * @param c the character to be written.
   * @return the number of bytes written: 2
   * @throws totalcross.io.IOException
   * @since SuperWaba 4.21
   */
  public int writeChar(char c) throws totalcross.io.IOException // guich@421_31
  {
    byte[] b = buffer;
    b[1] = (byte) c;
    c >>= 8;
    b[0] = (byte) c;
    return writeBytesInternal(b, 0, 2);
  }

  /**
   * Reads an array of chars. The length is stored in the first two bytes
   * as an unsigned short.
   * <p>The char array size is limited to 65535 characters.
   * 
   * @throws EOFException
   * @throws totalcross.io.IOException
   * @since SuperWaba 3.5
   */
  public char[] readChars() throws EOFException, totalcross.io.IOException {
    return readChars(readUnsignedShort());
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
  public void readChars(char[] chars, int len) throws EOFException, totalcross.io.IOException {
    byte[] bytes = buffer;
    int buflen = bytes.length / 2, start = 0;

    while (len > 0) {
      int avail = (len > buflen ? buflen : len) * 2;
      readBytesInternal(bytes, 0, avail, true);
      for (int i = 0; i < avail; i += 2) {
        chars[start++] = (char) (((bytes[i] & 0xFF) << 8) | (bytes[i + 1] & 0xFF));
      }
      len -= avail / 2;
    }
  }

  /**
   * Reads 'count' chars from the stream to the given char array, starting from index 'start'.
   * 
   * @param chars
   *           a char array
   * @param start
   *           starting position on the char array
   * @param count
   *           number of chars to be read from the stream
   * 
   * @throws EOFException
   * @throws totalcross.io.IOException
   * @since TotalCross 1.15
   */
  public int readChars(char[] chars, int start, int count) throws EOFException, totalcross.io.IOException {
    byte[] bytes = buffer;
    int buflen = bytes.length / 2;
    int current = start;

    while (count > 0) {
      int avail = (count > buflen ? buflen : count) * 2;
      readBytesInternal(bytes, 0, avail, true);
      for (int i = 0; i < avail; i += 2) {
        chars[current++] = (char) (((bytes[i] & 0xFF) << 8) | (bytes[i + 1] & 0xFF));
      }
      count -= avail / 2;
    }
    return current - start;
  }

  /**
   * Reads an array of chars, where its length is stored in the first four bytes
   * as an int.
   * <p>The char array size is limited to 2,147,483,647 characters.
   * 
   * @throws EOFException
   * @throws totalcross.io.IOException
   * @since TotalCross 1.0 beta3
   */
  public char[] readBigChars() throws EOFException, totalcross.io.IOException {
    return readChars(readInt());
  }

  /**
   * Reads a char array with the given length.
   * 
   * @param len
   * @return
   * @throws EOFException
   * @throws totalcross.io.IOException
   * @since TotalCross 1.0 beta3
   */
  protected char[] readChars(int len) throws EOFException, totalcross.io.IOException {
    char[] chars = new char[len];
    byte[] bytes = buffer;
    int buflen = bytes.length / 2, start = 0;
    while (len > 0) {
      int avail = (len > buflen ? buflen : len) * 2;
      readBytesInternal(bytes, 0, avail, true);
      for (int i = 0; i < avail; i += 2) {
        chars[start++] = (char) (((bytes[i] & 0xFF) << 8) | (bytes[i + 1] & 0xFF));
      }
      len -= avail / 2;
    }
    return chars;
  }

  /**
   * Writes an array of chars, placing its length in the first two bytes, as an
   * unsigned short.
   * <p>The char array size is limited to 65535 characters.
   *
   * @param len   the length to be written or -1 if it is to write the whole char array
   * @param chars the char array to be written.
   * @param start the starting index (in most cases: 0).
   * @return the number of bytes written.
   * @throws totalcross.io.IOException
   * @since SuperWaba 3.5
   */
  public int writeChars(char[] chars, int start, int len) throws totalcross.io.IOException {
    return writeChars(chars, start, len, 2);
  }

  /**
   * Writes an array of chars, placing its length in the first four bytes, as an
   * int.
   * <p>The char array size is limited to 2,147,483,647 characters.
   *
   * @param len   the length to be written or -1 if it is to write the whole char array
   * @param chars the char array to be written.
   * @param start the starting index (in most cases: 0).
   * @return the number of bytes written.
   * @throws totalcross.io.IOException
   * @since TotalCross 1.0 beta3
   */
  public int writeBigChars(char[] chars, int start, int len) throws totalcross.io.IOException {
    return writeChars(chars, start, len, 4);
  }

  /** Writes the given char array, writting the length as an int or as a short or as a byte or don't
   * writting the length, depending on the number of bytes given (4,2,1,0). */
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
        bytes[i + 1] = (byte) c;
        bytes[i] = (byte) (c >> 8);
      }
      n += writeBytesInternal(bytes, 0, avail);
      len -= avail / 2;
    }
    return n;
  }

  /** Writes the String as a char array. The chars are read using the charAt method from the String class.
   * This method is faster than the other writeChars method on blackberry, but slower on other devices. 
   * <p>The char array size is limited to 65535 characters.
   * @param s The String to be written. Must not be null!
   * @param len The maximum number of chars to be written. Must be less than the String's length.
   * @since TotalCross 1.0 beta 4
   */
  public int writeChars(String s, int len) throws totalcross.io.IOException {
    if (len > 65535) {
      throw new IOException("String size " + len + " is too big to use with writeChars!");
    }
    int c;
    int start = 0;
    int n = writeShort(len);
    byte[] bytes = buffer;
    int buflen = bytes.length / 2;
    while (len > 0) {
      int avail = (len > buflen ? buflen : len) * 2;
      for (int i = 0; i < avail; i += 2) {
        c = s.charAt(start++);
        bytes[i + 1] = (byte) c;
        bytes[i] = (byte) (c >> 8);
      }
      n += writeBytesInternal(bytes, 0, avail);
      len -= avail / 2;
    }
    return n;
  }

  /** Returns the base stream attached to this stream. */
  public Stream getStream() {
    return stream;
  }

  /**
   * Reads a fixed length string from the stream. The given number of
   * characters are read and converted to a String.
   *
   * @param length the number of characters to read
   * @return the loaded string
   * @throws EOFException
   * @throws totalcross.io.IOException
   */
  public String readFixedString(int length) throws EOFException, totalcross.io.IOException {
    if (buffer.length < length) {
      buffer = new byte[length];
    }

    readBytesInternal(buffer, 0, length, true);
    return new String(totalcross.sys.Convert.charConverter.bytes2chars(buffer, 0, length)); // guich@tc100b5_25: use the byte array instead
  }

  /**
   * Writes a fixed length string to the stream. If the given string is longer
   * than the given length, it will be truncated and if it is shorter, it will
   * be padded with spaces.
   *
   * @param s      the string to write
   * @param length the length of the fixed string
   * @throws totalcross.io.IOException
   */
  public void writeFixedString(String s, int length) throws totalcross.io.IOException {
    writeFixedString(s, length, ' ');
  }

  /**
   * Writes a fixed length string to the stream. If the given string is longer
   * than the given length, it will be truncated and if it is shorter, it will
   * be padded the given pad character.
   *
   * @param s      the string to write
   * @param length the length of the fixed string
   * @param pad    the character to pad if the string is shorter than the length
   * @throws totalcross.io.IOException
   */
  public void writeFixedString(String s, int length, char pad) throws totalcross.io.IOException {
    if (length == 0) {
      return;
    }
    if (buffer.length < length) {
      buffer = new byte[length];
    }
    byte[] b = buffer;
    int slen = 0;
    if (s != null) {
      char[] c = s.toCharArray();
      slen = c.length;
      for (int i = 0; i < slen && i < length; i++) {
        b[i] = (byte) c[i];
      }
    }
    Convert.fill(b, slen, length, pad); // pad the rest
    writeBytesInternal(b, 0, length);
  }

  /** Write the given Object using the Storable class.
   * @see Storable 
   */
  public void writeObject(Storable s) throws totalcross.io.IOException {
    writeString(s.getClass().getName());
    s.saveState(this);
  }

  /**
   * Read a Storable object.
   * 
   * @throws ClassNotFoundException
   * @throws InstantiationException
   * @throws IllegalAccessException
   * @throws EOFException
   * @throws totalcross.io.IOException
   * @see Storable
   */
  public Object readObject() throws ClassNotFoundException, InstantiationException, IllegalAccessException,
      EOFException, totalcross.io.IOException {
    String className = readString();
    Class<?> c = Class.forName(className);
    Object o = c.newInstance();
    ((Storable) o).loadState(this);
    return o;
  }

  /**
   * Read a small String. If the length is 0, an
   * empty String is returned.
   * <p>
   * The String size is limited to 255 characters.
   * 
   * @throws EOFException
   * @throws totalcross.io.IOException
   * @since TotalCross 1.0
   */
  public String readSmallString() throws EOFException, totalcross.io.IOException {
    int len = readUnsignedByte();
    if (len == 0) {
      return "";
    }
    readBytesInternal(buffer, 0, len, true);
    return new String(totalcross.sys.Convert.charConverter.bytes2chars(buffer, 0, len));
  }

  /** Write a small String comprised of only ASCII chars (each char is casted to byte).
   * The length is written as a single byte before the byte array.
   * <p>The String size is limited to 255 characters.
   * @since TotalCross 1.0
   */
  public int writeSmallString(String s) throws totalcross.io.IOException {
    int len = s == null ? 0 : s.length();
    if (len > 255) {
      throw new IOException("String size " + s.length() + " is too big to use with writeSmallString!");
    }
    writeByte(len);
    int ret = len + 1;
    if (len > 0) {
      for (int i = 0; len-- > 0; i++) {
        writeByte(s.charAt(i));
      }
    }
    return ret;
  }

  /** Write a small String taking each char as a byte. To read it, use readString.
   * The maximum allowed length is 65536.
   * @since TotalCross 1.52
   */
  public int writeSmallString8(String s) throws IOException {
    int len = s == null ? 0 : s.length();
    if (len > 65536) {
      throw new IOException("String size " + s.length() + " is too big to use with writeSmallString8!");
    }
    writeShort(len);
    int ret = len + 2;
    if (len > 0) {
      for (int i = 0; len-- > 0; i++) {
        writeByte(s.charAt(i));
      }
    }
    return ret;
  }

  protected int writeBytesInternal(byte[] buf, int start, int count) throws totalcross.io.IOException {
    int written = stream.writeBytes(buf, start, count);
    if (ensureWrite && written < count) {
      do {
        written += stream.writeBytes(buf, start + written, count - written);
      } while (written < count);
    }
    return written;
  }

  /**
   * Blocks until the requested number of bytes is read from the underlying stream, only returning less than count if
   * the end of the stream is reached.
   * 
   * @param buf
   * @param start
   * @param count
   * @return The number of bytes read, which will usually be equal to count. It may return a positive value smaller
   *         than count if the end of the stream is reached during the read operation, or -1 if the end of stream and
   *         nothing was read.
   * @throws EOFException
   * @throws totalcross.io.IOException
   */
  protected int readBytesInternal(byte[] buf, int start, int count, boolean throwEOF)
      throws EOFException, totalcross.io.IOException {
    if (count == 0) {
      return 0;
    }
    int r = stream.readBytes(buf, start, count);
    if (r == count) {
      return r;
    }

    int bytesRead = r;
    if (r != -1) {
      while (bytesRead < count) {
        r = stream.readBytes(buf, start + bytesRead, count - bytesRead); // stop after we finish reading or at eos
        if (r == -1) {
          break;
        }
        bytesRead += r;
      }
    }
    if (r == -1 && throwEOF) {
      throw new EOFException(EOSMessage);
    }
    return bytesRead;
  }

  /** @deprecated Use skipBytes instead. */
  @Deprecated
  public int skip(int n) throws IOException {
    return skipBytes(n);
  }
}
