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

import totalcross.util.Vector;

/**
 * Used to read an array of tokens in a line ending with \r\n (enter/linefeed) from a stream. Consecutive newlines are skipped. 
 * This class does not work well with multi-byte characters when the second byte contains the delimiter or enter/linefeed.
 * <br><br>The usual way to parse a CSV file is:
 * <pre>
 * do
 *   line = readline
 *   Convert.tokenizeString(line)
 * </pre>
 * Using this class takes less memory, because the line is read in tokens. For example, suppose a line contains 200 chars, 
 * and splitting them contains 10 tokens of 20 chars each. Using the first approach (readline/tokenizeString), the readline will
 * create a string with 200 chars, then that String will be tokenized into 10 smaller strings of 20 chars each.<br><br>
 * Using this class, it will read the 10 tokens of 20 chars each directly, no longer having to create the temporary string of 200 chars.
 * <br><br>
 * The delimiter can be any character except for \r and \n. Note that two consecutive delimiters are considered a single token. So
 * <code>;a;;</code> is returned as <code>{"","a","",""}</code>.
 * <br><br>
 * Here's a sample that parses the input from a file:
 *
 * <pre>
 * TokenReader reader = new TokenReader(new File(&quot;text.csv&quot;,File.READ_WRITE), ',');
 * String[] tokens;
 * while ((tokens = reader.readTokens()) != null)
 * {
 *    ... do whatever you want with the tokens.
 * }
 * </pre>
 * And here's another sample that parses from a string:
 * <pre>
 * String lines = "a;;;a;\na;;;a; \nb;;b;b;b \nb;;b;b;b;\nb\nb;\n;b\n;\n b ;\n b \n  b \n b  \nb  \n  b";
 * String ll[] = Convert.tokenizeString(lines,'\n');
 * TokenReader tk = new TokenReader(new ByteArrayStream(lines.getBytes()),';');
 * tk.doTrim = true;
 * String []line;
 * for (int j =0; ((line = tk.readTokens()) != null); j++)
 * {
 *    Vm.debug('"'+ll[j]+'"');
 *    for (int i =0; i < line.length; i++)
 *       Vm.debug(i+": '"+line[i]+"'");
 *    Vm.debug("");
 * }
 * </pre>
 * The output is:
 * <pre>
 * "a;;;a;"
 * 0: 'a'
 * 1: ''
 * 2: ''
 * 3: 'a'
 * 4: ''
 * 
 * "a;;;a; "
 * 0: 'a'
 * 1: ''
 * 2: ''
 * 3: 'a'
 * 4: ''
 * 
 * "b;;b;b;b "
 * 0: 'b'
 * 1: ''
 * 2: 'b'
 * 3: 'b'
 * 4: 'b'
 * 
 * "b;;b;b;b;"
 * 0: 'b'
 * 1: ''
 * 2: 'b'
 * 3: 'b'
 * 4: 'b'
 * 5: ''
 * 
 * "b"
 * 0: 'b'
 * 
 * "b;"
 * 0: 'b'
 * 1: ''
 * 
 * ";b"
 * 0: ''
 * 1: 'b'
 * 
 * ";"
 * 0: ''
 * 1: ''
 * 
 * " b ;"
 * 0: 'b'
 * 1: ''
 * 
 * " b "
 * 0: 'b'
 * 
 * "  b "
 * 0: 'b'
 * 
 * " b  "
 * 0: 'b'
 * 
 * "b  "
 * 0: 'b'
 * 
 * "  b"
 * 0: 'b'
 * </pre>
 * Note that this class already uses a buffer for faster detection of the newline and delimiters.
 * Don't use TokenReader with a BufferedStream, it's nonsense and it will throw a warning on the desktop.
 *
 * @author Guilherme Campos Hazan (guich)
 * @since TotalCross 1.23
 */
public class TokenReader extends LineReader {
  protected char delimiter;
  private Vector lines = new Vector(10);

  /**
   * Constructs a new TokenReader and sets maxTries accordingly to the type of
   * class: 10 if its a Socket or a PortConnector; 0, otherwise.
   *
   * @throws totalcross.io.IOException
   */
  public TokenReader(Stream f, char delimiter) throws totalcross.io.IOException {
    super(f);
    this.delimiter = delimiter;
  }

  /**
   * Constructs a new TokenReader and sets maxTries accordingly to the type of
   * class: 10 if its a Socket or a PortConnector, 0 otherwise.
   * The given buffer contents are added to the internal buffer to start reading from them.
   *
   * @throws totalcross.io.IOException
   * @since TotalCross 1.25
   */
  public TokenReader(Stream f, char delimiter, byte[] buffer, int start, int len) throws totalcross.io.IOException // guich@tc125_16
  {
    super(f, buffer, start, len);
    this.delimiter = delimiter;
  }

  /** Cannot be used; throws a RuntimeException.
   * @see #readTokens
   */
  @Override
  public String readLine() {
    throw new RuntimeException("Use readTokens instead of readLine!");
  }

  /**
   * Returns the next tokens available in this stream or null if none. Empty
   * lines are skipped.
   *
   * @throws totalcross.io.IOException
   */
  public String[] readTokens() throws totalcross.io.IOException {
    byte[] buf = readBuf.getBuffer();
    int size = readBuf.getPos();
    byte delimiter = (byte) this.delimiter;

    // skip starting control chars
    while (ofs < size && (buf[ofs] == '\n' || buf[ofs] == '\r')) {
      ofs++;
    }

    lines.removeAllElements();

    while (true) {
      int i;
      for (i = ofs; i < size; i++) {
        if (buf[i] == delimiter || buf[i] == '\n') // found a token or a linefeed?
        {
          int len = i - ofs;
          if (i > 0 && buf[i - 1] == '\r') {
            len--;
          }
          int ii = ofs + len;
          if (doTrim && len > 0 && (buf[ofs] <= ' ' || buf[ii - 1] <= ' ')) // guich@tc123_37
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
          String s = new String(buf, ofs, len);
          ofs = i;
          lines.addElement(s);
          if (buf[i] == '\n') {
            return (String[]) lines.toObjectArray();
          }
          ofs++; // strip the cr/lf from the string
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
        if (len == 0 && lines.size() == 0) {
          return null;
        }
        ofs = len;
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
        String s = new String(buf, lastOfs, len - lastOfs);
        lines.addElement(s);
        return (String[]) lines.toObjectArray();
      }
    }
  }
}
