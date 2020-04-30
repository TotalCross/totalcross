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

import totalcross.sys.CharacterConverter;
import totalcross.sys.Convert;
import totalcross.util.Vector;
import totalcross.util.zip.ZLib;

/**
 * Creates a compressed byte array stream, saving memory when reading and writting
 * huge amount of data. Arrays of
 * 16000 bytes will be created and each byte array will be compressed once
 * filled and will be automatically decompressed on read. This saves space but
 * adds a slowdown to the process. It is useful when transferring FTP files
 * to/from the server.
 * <p>
 * This class cannot be used for output AND input, but only for output OR input,
 * in an absolutely sequential mode (the skipBytes method is NOT implemented):
 * you must write everything, then read everything. To change the mode, use the
 * setMode(READ_MODE or WRITE_MODE) method. No check is made to see if you're in
 * the right mode, but your program will probably crash if you do it in the
 * wrong one.
 * <p>
 * Sample that transfers bytes to the server:
 *
 * <pre>
 * CompressedByteArrayStream cbas = new CompressedByteArrayStream(9); // default mode is WRITE_MODE
 * for (int i = 0; i &lt; 50000; i++)
 *    cbas.writeLine(&quot;1234567890&quot;); // already appends \r\n
 * cbas.flush();
 * cbas.setMode(CompressedByteArrayStream.READ_MODE); // prepare for read
 * ftp.sendFile(cbas, &quot;bigfile.txt&quot;, true);
 * // if you want to send another one, just call
 * <code>
 * cbas.setMode(CompressedByteArrayStream.WRITE_MODE);
 * </code>
 * </pre>
 *
 * Sample that transfers bytes from the server:
 *
 * <pre>
 * CompressedByteArrayStream cbas = new CompressedByteArrayStream(9);
 * ftp.receiveFile(&quot;bigfile.txt&quot;, cbas);
 * cbas.flush();
 * String line;
 * while ((line = cbas.readLine()) != null)
 *    // do something with the line!
 * </pre>
 *
 * Here is another fully functional sample:
 *
 * <pre>
 * int i;
 * String g = &quot;1234567890&quot;;
 * CompressedByteArrayStream cbas = new CompressedByteArrayStream(9); // default mode is WRITE_MODE
 * for (i = 0; i &lt; 50000; i++)
 *    cbas.writeLine(g); // already appends \r\n
 * cbas.flush();
 * Vm.debug(&quot;size: &quot; + cbas.getCompressedSize() + &quot; -&gt; &quot; + cbas.getSize());
 * String s;
 * for (i = 0; (s = cbas.readLine()) != null; i++)
 *    if (!g.equals(s))
 *       Vm.debug(&quot;error in &quot; + i);
 * if (i != 50000)
 *    Vm.debug(&quot;i differs!&quot;);
 * cbas.close();
 * </pre>
 *
 * Note that, although the samples above use writeLine and readLine, you can store any
 * kind of data. By attaching a DataStream it's possible to read any data type from the stream.
 *
 * <pre>
 * CompressedByteArrayStream cbas = new CompressedByteArrayStream(5);
 * DataStream ds = new DataStream(cbas);
 * byte[] big = new byte[200000];
 * // fill big with something
 * ds.writeBytes(big);
 * for (int i = 0; i &lt; 100000; i++)
 * {
 *    ds.writeInt(0x123456);
 *    ds.writeString(&quot;Natasha&quot;);
 *    ds.writeDouble(123.456d);
 * }
 * // well, now we do something with these!
 * int realSize = cbas.getSize(); // just for fun
 * int compressed = cbas.getCompressedSize(); // just for fun
 * ds.readBytes(big);
 * for (int i = 0; i &lt; 100000; i++)
 * {
 *    int i = ds.readInt();
 *    String love = ds.readString(); // Natasha
 *    double d = ds.readDouble();
 * }
 * </pre>
 *
 * Call the close method only when you're completely done in using it: all the
 * internal buffers will be released, and reading from it will crash your
 * program.
 * <p>
 * Note that the readLine method will not work if there are any character with
 * accentuation.
 */

public class CompressedByteArrayStream extends Stream {
  /** Implements a CharacterConverter that converts from char[] to byte[] which just
   * casts the char to byte; thus, ignoring any non-ASCII character. */
  public static class DirectCharConverter extends CharacterConverter {
    /** Just casts the char to byte; thus, ignoring any non-ASCII character. */
    @Override
    public byte[] chars2bytes(char[] chars, int offset, int length) {
      offset += length;
      byte[] b = new byte[length];
      while (--length >= 0) {
        b[length] = (byte) chars[--offset];
      }
      return b;
    }
  }

  private static final int SIZE = 16000;

  /** Used in the setMode method. Turns the mode into READ. */
  public static final int READ_MODE = 1;
  /** Used in the setMode method. Turns the mode into WRITE. */
  public static final int WRITE_MODE = 0;
  /**
   * Used in the setMode method. Turns the mode into READ, and after reading
   * each buffer, discards it, releasing memory. CompressedByteArrayStream will not be able to
   * read the buffer again. This is useful when you download data and then want to read from it,
   * releasing memory on-demand.
   */
  public static final int DESTRUCTIVE_READ_MODE = 2; // guich@570_28

  /**
   * Defines the line terminator, which is by default \r\n. To change it to a single \n
   * use <code>CompressedByteArrayStream.crlf = new byte[]{'\n'};</code>
   */
  public static byte[] crlf = { (byte) '\r', (byte) '\n' };

  private int mode; // READ or WRITE
  private int compressionLevel;
  private int rSize, cSize; // real and compressed sizes
  private int readIdx; // current buffer under use when reading
  private Vector zbufs = new Vector(); // stores the compressed data
  private ByteArrayStream buf = new ByteArrayStream(SIZE); // current read/write buffer
  private byte[] bufbytes = buf.getBuffer(); // since buf size won't change, this is safe
  private static ByteArrayStream temp = new ByteArrayStream(SIZE); // used to compress/uncompress
  private StringBuffer sbuf; // used in readLine
  private byte[] writeBuf; // used in readFully

  /**
   * Creates a new CompressedByteArrayStream, using the given compression level (0 =
   * no compression, 9 = max compression).
   */
  public CompressedByteArrayStream(int compressionLevel) throws IllegalArgumentException {
    if (compressionLevel < 0 || compressionLevel > 9) {
      throw new IllegalArgumentException("Argument 'compressionLevel' must be >= 0 and <= 9");
    }
    this.compressionLevel = compressionLevel;
  }

  /**
   * Creates a new CompressedByteArrayStream using the maximum compression level (9)
   */
  public CompressedByteArrayStream() {
    this(9);
  }

  /**
   * After everything was written, call this method to flush the internal buffers 
   * and prepare the CompressedByteArrayStream for read. It is already called by setMode
   * when it changes the modes.
   * @throws IOException 
   * @see #setMode(int)
   */
  public void flush() throws IOException {
    if (buf.getPos() > 0) {
      saveCurrentBuffer();
    }
    if (mode == WRITE_MODE) // guich@566_37
    {
      mode = -1; // don't let setMode call us again.
      setMode(READ_MODE);
    }
  }

  /** Changes the mode to the given one, calling <code>flush</code> if in write mode. 
   * @param newMode the new mode
   * @throws IOException 
   * @see #WRITE_MODE
   * @see #READ_MODE
   * @see #DESTRUCTIVE_READ_MODE
   */
  public void setMode(int newMode) throws IOException {
    // flsobral@tc100b5_45: Stream was not being reseted when the new mode was the same as the current one.
    if (mode == WRITE_MODE && mode != newMode) {
      flush();
    }
    if (newMode == READ_MODE || newMode == DESTRUCTIVE_READ_MODE) {
      readIdx = -1;
    }
    mode = newMode;
    loadNextBuffer();
  }

  /** Deletes all internal buffers. Do not try to use the object afterwards. */
  @Override
  public void close() {
    buf = null;
    zbufs.removeAllElements();
  }

  /** Returns the real (uncompressed) size of data written. */
  public int getSize() {
    return rSize; // luciana@572_20 - fixed, it was returning cSize
  }

  /** Returns the compressed size of the data written. */
  public int getCompressedSize() {
    return cSize; // luciana@572_20 - fixed, it was returning rSize
  }

  /** Compresses the current buffer and add it to the buffer arrays 
   * @throws IOException */
  private void saveCurrentBuffer() throws IOException {
    buf.mark(); // note: unless for the last buffer, all the others will have SIZE bytes
    temp.reset();
    cSize += ZLib.deflate(buf, temp, compressionLevel);
    zbufs.addElement(temp.toByteArray()); // save the compressed buffer
    //Vm.debug("saved "+buf.count()+" -> "+temp.count());
    buf.reset();
  }

  /** Uncompresses the next buffer and load it to memory 
   * @throws IOException */
  private boolean loadNextBuffer() throws IOException {
    if (++readIdx >= zbufs.size()) {
      return false;
    }
    buf.reset();
    if (readIdx > 0 && mode == DESTRUCTIVE_READ_MODE) {
      zbufs.items[readIdx - 1] = null;
    }
    byte[] b = (byte[]) zbufs.items[readIdx];
    ByteArrayStream bas = new ByteArrayStream(b);
    /* int s = */ZLib.inflate(bas, buf);
    buf.mark();
    //Vm.debug("loaded "+b.length+" -> "+s);
    return true;
  }

  /**
   * Transfers count bytes from the internal buffer to the given one.
   *
   * @param buffer the byte array to read data into
   * @param start  the start position in the array
   * @param count  the number of bytes to read
   * @return the number of bytes read. If an error occurred, -1 is returned and 
   * @throws IOException 
   */
  @Override
  public int readBytes(byte buffer[], int start, int count) throws IOException {
    if (start < 0) {
      throw new IllegalArgumentException("Argument 'start' cannot be less than 0");
    }
    if (count < 0) {
      throw new IllegalArgumentException("Argument 'count' cannot be less than 0");
    }

    int orig = count;
    while (true) {
      if (buf.available() > 0) {
        int n = buf.readBytes(buffer, start, count);
        count -= n;
        start += n;
      }
      if (count == 0) {
        break;
      }
      if (!loadNextBuffer()) {
        break;
      }
    }
    return orig - count;
  }

  /**
   * This method writes to the byte array, expanding it if necessary. 
   *
   * @param buffer the byte array to write data from
   * @param start  the start position in the byte array
   * @param count  the number of bytes to write
   * @return the number of bytes written. If an error occurred, -1 is returned and 
   * @throws IOException, IllegalArgumentException 
   * @since SuperWaba 2.0 beta 2
   */
  @Override
  public int writeBytes(byte buffer[], int start, int count) throws IOException, IllegalArgumentException {
    if (start < 0) {
      throw new IllegalArgumentException("Argument 'start' cannot be less than 0");
    }
    if (count < 0) {
      throw new IllegalArgumentException("Argument 'count' cannot be less than 0");
    }

    int orig = count, a;
    while (true) {
      if ((a = buf.available()) > 0) {
        int w = count > a ? a : count;
        int n = buf.writeBytes(buffer, start, w);
        count -= n;
        start += n;
      }
      if (count == 0) {
        break;
      }
      saveCurrentBuffer(); // if failed, this will just abort the program with a OutOfMemoryError
    }
    orig -= count;
    rSize += orig;
    return orig;
  }

  /** Reads a String until the next control character (newline, enter, tab, etc) is read.
   * @return A line of text read from internal buffer or null if no more lines are available.
   * @throws IOException 
   */
  public String readLine() throws IOException {
    if (sbuf == null) {
      sbuf = new StringBuffer(1024);
    }
    StringBuffer sb = sbuf;
    sb.setLength(0);
    boolean stop = false;
    while (!stop) {
      int a = buf.available();
      if (a == 0) {
        if (!loadNextBuffer()) {
          break;
        }
        a = buf.available();
      }
      int p0 = buf.getPos();
      int p = p0;
      byte[] b = bufbytes;
      while (a > 0 && (b[p] == '\r' || b[p] == '\n')) // skip starting enters - guich@565_10: discard negative values - guich@tc123_31: use only \r and \n as delimiters
      {
        if (sb.length() > 0) {
          return sb.toString();
        }
        p++;
        a--;
      }
      int i = p;
      // search for the \r\n
      for (; a > 0; i++, a--) {
        if (b[i] == '\r' || b[i] == '\n') // guich@565_10: discard negative values - guich@tc123_31
        {
          stop = true;
          break;
        }
      }
      int len = i - p;
      if (len > 0) {
        sb.append(Convert.charConverter.bytes2chars(b, p, len));
      }
      buf.skipBytes(i - p0);
    }
    return sb.length() > 0 ? sb.toString() : null;
  }

  /**
   * Reads all data from the input stream into our buffer. Note that
   * setMode(WRITE) is called prior to writting. When returned, data is ready
   * to be read.
   *
   * @param inputStream The input stream from where data will be read
   * @param retryCount  The number of times to retry if no data is read. In remote connections, 
   * use at least 5; for files, it can be 0.
   * @param bufSize The size of the buffer used to read data.
   * @throws IOException
   * @since SuperWaba 5.7
   */
  public void readFully(Stream inputStream, int retryCount, int bufSize) throws IOException // guich@570_31
  {
    byte[] buf = (writeBuf != null && writeBuf.length >= bufSize) ? writeBuf : (writeBuf = new byte[bufSize]);
    setMode(WRITE_MODE);
    while (true) {
      int n = inputStream.readBytes(buf, 0, buf.length);
      if (n <= 0 && --retryCount <= 0) {
        break;
      }
      if (n > 0) {
        writeBytes(buf, 0, n);
      }
    }
    flush();
  }

  /**
   * Writes a line of text. The \r\n line terminator is appended to the line.
   * You can avoid this by setting
   * <code>CompressedByteArrayStream.crlf = new byte[0];</code>
   * @param s the String to be written; cannot be null!
   * @throws IOException 
   */
  public void writeLine(String s) throws IOException {
    byte[] b = s.getBytes();
    writeBytes(b, 0, b.length);
    writeBytes(crlf, 0, crlf.length);
  }

  /** Reads the buffer until the given character is found.  
   * @return A line of text read from internal buffer or null if no more lines are available.
   * @throws IOException 
   */
  public String readUntilNextChar(char c) throws IOException {
    if (sbuf == null) {
      sbuf = new StringBuffer(1024);
    }
    StringBuffer sb = sbuf;
    sb.setLength(0);

    boolean stop = false;
    while (!stop) {
      int a = buf.available();
      if (a == 0) {
        if (!loadNextBuffer()) {
          break;
        }
        a = buf.available();
      }
      int p0 = buf.getPos();
      int p = p0;
      byte[] b = bufbytes;
      while (a > 0 && (b[p] & 0xFF) == c) // skip starting enters - guich@565_10: discard negative values
      {
        if (sb.length() > 0) {
          return sb.toString();
        }
        p++;
        a--;
      }
      int i = p;
      // search for the \r\n
      for (; a > 0; i++, a--) {
        if ((b[i] & 0xFF) == c) // guich@565_10: discard negative values
        {
          stop = true;
          break;
        }
      }
      int len = i - p;
      if (len > 0) {
        sb.append(Convert.charConverter.bytes2chars(b, p, len));
      }
      buf.skipBytes(i - p0);
    }
    return sb.length() > 0 ? sb.toString() : null;
  }
}
