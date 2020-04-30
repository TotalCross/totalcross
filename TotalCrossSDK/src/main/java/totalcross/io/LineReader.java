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

import totalcross.io.device.PortConnector;
import totalcross.net.Socket;
import totalcross.sys.Convert;
import totalcross.sys.Settings;
import totalcross.sys.Vm;

/**
 * Used to read lines ending with \r\n (enter/linefeed) or \n (linefeed) from a stream. Consecutive newlines are skipped. 
 * This class does not work well with multi-byte characters when the second byte contains the delimiter or enter/linefeed.
 * <p>
 * Here's a sample:
 *
 * <pre>
 * LineReader reader = new LineReader(new File(&quot;text.txt&quot;,File.READ_WRITE));
 * String line;
 * while ((line = reader.readLine()) != null)
 * {
 *    ... do whatever you want with the line.
 * }
 * </pre>
 * Note that this class already uses a buffer for faster detection of the newline.
 * Don't use LineReader with a BufferedStream, it's nonsense and it will throw a warning on the desktop.
 *
 * @author Guilherme Campos Hazan (guich)
 * @since SuperWaba 5.12
 */
public class LineReader {
  protected Stream f;
  protected ByteArrayStream readBuf = new ByteArrayStream(!Settings.onJavaSE ? 4096 : 32768); // guich@554_10: increase buffer size on applet
  protected int ofs;
  /**
   * The number of times it tries to read more data if none is available.
   * Defaults to 10 if the Stream is a Socket or a PortConnector; 0, otherwise.
   */
  public int maxTries;

  /** Set to true to apply a trim in the string that is returned.
   * @since TotalCross 1.23
   */
  public boolean doTrim; // guich@tc123_37

  /** Set to true to receive empty lines (\r\n\r\n returns "","").
   * @since TotalCross 1.3
   */
  public boolean returnEmptyLines;

  /**
   * Constructs a new LineReader and sets maxTries accordingly to the type of
   * class: 10 if its a Socket or a PortConnector, 0 otherwise.
   *
   * @throws totalcross.io.IOException
   */
  public LineReader(Stream f) throws totalcross.io.IOException {
    this(f, null, 0, 0);
  }

  /**
   * Constructs a new LineReader and sets maxTries accordingly to the type of
   * class: 10 if its a Socket or a PortConnector; 0, otherwise.
   * The given buffer contents are added to the internal buffer to start reading from them.
   *
   * @throws totalcross.io.IOException
   * @since TotalCross 1.25
   */
  public LineReader(Stream f, byte[] buffer, int start, int len) throws totalcross.io.IOException // guich@tc125_16
  {
    this.f = f;
    if ((f instanceof Socket) || (f instanceof PortConnector)) {
      maxTries = 10;
    } else if (f instanceof BufferedStream && Settings.onJavaSE) {
      Vm.warning("Don't use " + getClass().getName()
          + " with a BufferedStream, because the LineReader class already uses a buffer for faster operation. Pass to LineReader's constructor the Stream you're using with the BufferedStream and discard the BufferedStream");
    }
    if (buffer != null && len > 0) {
      readBuf.writeBytes(buffer, start, len);
    }
  }

  /** Change the initial Stream to the attached one, and fetches some data.
   * Reusing a LineReader throught this method can preserve memory.
   * @since TotalCross 1.23
   */
  public void setStream(Stream f) throws IOException // guich@tc123_34
  {
    this.f = f;
    readBuf.reset();
    ofs = 0;
  }

  /** Returns the Stream attached to this LineReader.
   * @since TotalCross 1.23
   */
  public Stream getStream() {
    return f;
  }

  /**
   * Move the buffer to the beginning, in order to preserve the bytes that were
   * not read yet.
   */
  protected void reuse() {
    int pos = readBuf.getPos();
    int stillUnread = pos - ofs;
    readBuf.skipBytes(-pos); // reset the pos to 0
    readBuf.skipBytes(ofs); // move up to where we already read
    readBuf.reuse();
    readBuf.skipBytes(stillUnread);
    // reset the offset
    ofs = 0;
  }

  /**
   * Read more bytes from the stream. If there's no data immediately to be
   * read, it causes the current thread to yield the current use of CPU before 
   * attempting a new read, up to <code>maxTries</code> times.
   *
   * @throws totalcross.io.IOException
   */
  protected boolean readMore() throws totalcross.io.IOException {
    // read more bytes
    if (readBuf.available() == 0) {
      readBuf.setSize(readBuf.getPos() + 1024, true); // grow the buffer if needed
    }
    int r = f.readBytes(readBuf.getBuffer(), readBuf.getPos(), readBuf.available());
    if (r < 0) {
      for (int i = maxTries - 1; i >= 0; i--) {
        Thread.yield();
        r = f.readBytes(readBuf.getBuffer(), readBuf.getPos(), readBuf.available());
        if (r > 0) {
          break;
        }
      }
    }
    if (r > 0) {
      readBuf.skipBytes(r); // mark the size, moving pos to the end.
    }
    return r > 0;
  }

  /**
   * Returns the next line available on this stream or null if none. Empty
   * lines are skipped by default.
   *
   * @throws totalcross.io.IOException
   */
  public String readLine() throws totalcross.io.IOException {
    byte[] buf = readBuf.getBuffer();
    int size = readBuf.getPos();
    boolean foundEnter = false;

    // skip starting control chars
    if (!returnEmptyLines) {
      while (ofs < size && (buf[ofs] == '\n' || buf[ofs] == '\r')) {
        ofs++;
      }
    } else {
      if (ofs < size && buf[ofs] == '\r') {
        ofs++;
      }
      if (ofs < size && buf[ofs] == '\n') {
        ofs++;
      }
    }

    while (true) {
      int i;
      for (i = ofs; i < size; i++) {
        if (buf[i] == '\n') // found an enter? - guich@tc123_31
        {
          foundEnter = true;
          int len = i - ofs; // guich@552_28: verify if the length is not 0
          if (i > 0 && buf[i - 1] == '\r') {
            len--;
          }
          if (len > 0 || returnEmptyLines) {
            int ii = ofs + len;
            if (doTrim && (buf[ofs] <= ' ' || buf[ii - 1] <= ' ')) // guich@tc123_37
            {
              while (ofs < ii && buf[ofs] <= ' ') {
                ofs++;
              }
              while (ii > ofs && buf[ii - 1] <= ' ') {
                ii--;
              }
              len = ii - ofs;
            }
            // allocate the new String and return
            String s = new String(Convert.charConverter.bytes2chars(buf, ofs, len));
            ofs = i;
            return s;
          }
          ofs++; // guich@552_28: strip the cr/lf from the string
        }
      }
      // no enter found; fetch more data
      int lastOfs = ofs;
      reuse();
      boolean foundMore = readMore();
      size = readBuf.getPos(); // size had changed
      buf = readBuf.getBuffer(); // buffer may have changed
      if (!foundMore) {
        int len = i - lastOfs;
        if (len > 0 || (foundMore && returnEmptyLines)) // any remaining string on the buffer?
        {
          if (foundEnter) {
            ofs = len;
          }
          int len0 = len;
          lastOfs = 0;
          if (doTrim && len > 0 && (buf[0] <= ' ' || buf[len - 1] <= ' ')) // guich@tc123_37
          {
            while (lastOfs < len && buf[lastOfs] <= ' ') {
              lastOfs++;
            }
            while (len > lastOfs && buf[len - 1] <= ' ') {
              len--;
            }
          }
          String s = new String(Convert.charConverter.bytes2chars(buf, ofs, len));
          ofs = len0;
          return s;
        }
        return null;
      }
    }
  }
}
