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

package totalcross.net;

import java.io.IOException;
import totalcross.io.Stream;
import totalcross.sys.Convert;
import totalcross.sys.Vm;

/**
 * Socket is a TCP/IP network socket.
 * <p>
 * Under Java and Windows CE, if no network is present, the socket constructor
 * may hang for an extended period of time due to the implementation of sockets
 * in the underlying OS. This is a known problem.
 * <p>
 * Here is an example showing data being written and read from a socket:
 *
 * <pre>
 * Socket socket = new Socket(&quot;www.totalcross.com&quot;, 80);
 * DataStream ds = new DataStream(socket);
 * ds.writeBytes(&quot;GET / HTTP/1.0\n\n&quot;);
 * String ack = socket.readLine();
 * socket.close();
 * </pre>
 *
 * Important: you cannot open a socket before the main event loop. In other
 * words, you cannot open a socket in the app's constructor, but CAN in the
 * initUI method.
 * <p>
 * When using GPRS connections, it is very important that you set a big timeout
 * (20 seconds at least), otherwise the connection will be closed before the
 * data is fully flushed (which only occurs after Socket.close).
 */

public class Socket extends Stream {
  /**
   * Stores the timeout value for read operations. The value specifies the
   * number of milliseconds to wait from the time of last activity before
   * timing out a read operation. Passing a value of 0 sets no timeout causing
   * any read operation to return immediately with or without data. The default
   * read timeout is 5000 milliseconds.
   */
  public int readTimeout = DEFAULT_READ_TIMEOUT;
  /**
   * Stores the timeout value for write operations. The value specifies the
   * number of milliseconds to wait from the time of last activity before
   * timing out a write operation. Passing a value of 0 sets no timeout causing
   * any write operation to return immediately, successfully or not. The
   * default write timeout is 2000 milliseconds.
   */
  public int writeTimeout = DEFAULT_WRITE_TIMEOUT;

  /** Used just in the desktop side */
  Object socketRef;
  /** Used just in the desktop side */
  private Object myInputStream;
  /** Used just in the desktop side */
  private Object myOutputStream;
  /** The remote host which this socket is connected to */
  private String host;
  /** The remote port which this socket is connected to */
  private int port;

  /** Default timeout value for socket creation */
  public static final int DEFAULT_OPEN_TIMEOUT = 2000;
  /** Default timeout value for read operations */
  public static final int DEFAULT_READ_TIMEOUT = 5000;
  /** Default timeout value for write operations */
  public static final int DEFAULT_WRITE_TIMEOUT = 2000;

  /** For internal use only */
  protected Socket() {
  }

  /**
   * Opens a socket with the given host, port and open timeout of 1500ms.
   *
   * @param host the host name or IP address to connect to
   * @param port the port number to connect to
   * @see #Socket(String, int, int, String)
   */
  public Socket(String host, int port) throws totalcross.net.UnknownHostException, totalcross.io.IOException {
    this(host, port, DEFAULT_OPEN_TIMEOUT, null);
  }

  /**
   * Opens a socket with the given host, port, open timeout.
   *
   * @param host the host name or IP address to connect to
   * @param port the port number to connect to
   * @param timeout the specified timeout, in milliseconds.
   * @see #Socket(String, int, int, String)
   */
  public Socket(String host, int port, int timeout)
      throws totalcross.net.UnknownHostException, totalcross.io.IOException {
    this(host, port, timeout, null);
  }

  /**
   * Opens a socket with the given parameters. This method establishes a socket connection by looking up
   * the given host and performing the 3 way TCP/IP handshake.
   *
   * @param host the host name or IP address to connect to
   * @param port the port number to connect to
   * @param timeout the specified timeout, in milliseconds.
   * @param params 
   *           the string specifying additional parameters to this socket.
   *           Each parameter is specified in a 'key=value' form and separated
   *           by a ';' from the next parameter. For example: 'p1=v1;p2=v2'.
   *           On BlackBerry, the following parameters are valid: 'apn=[value]',
   *           'apnuser=[value]', 'apnpass=[value]', 'directtcp=[true|false]' and
   *           'nolinger=[true|false]'.           
   *           On Palm OS devices, the only valid parameter is 'nolinger=[true|
   *           false]'. If true, the socket is closed immediately, and no ack
   *           is waited from the server. Note that this must be done for the
   *           very first socket creation per application.
   * @throws totalcross.net.UnknownHostException
   * @throws totalcross.io.IOException
   */
  public Socket(String host, int port, int timeout, String params)
      throws totalcross.net.UnknownHostException, totalcross.io.IOException {
    if (port < 0 || port > 65535) {
      throw new java.lang.IllegalArgumentException("Invalid value for argument 'port': " + port);
    }
    if (timeout < 0) {
      throw new java.lang.IllegalArgumentException("Invalid value for argument 'timeout': " + timeout);
    }

    this.host = host;
    this.port = port;
    socketCreate(host, port, timeout);
  }

  /**
   * Opens a socket with the given host, port, timeout and linger option.
   *
   * @param host the host name or IP address to connect to
   * @param port the port number to connect to
   * @param timeout the specified timeout, in milliseconds.
   * @param noLinger if true, the socket is closed immediately, and no ack is waited
   *           from the server. Note that this must be done for the very first
   *           socket creation per application.
   * @see #Socket(String, int, int, String)
   */
  public Socket(String host, int port, int timeout, boolean noLinger)
      throws totalcross.net.UnknownHostException, totalcross.io.IOException {
    this(host, port, timeout, noLinger ? ("nolinger=" + noLinger) : null);
  }

  /**
   * Native implementation for socket creation. <br>
   * NOTE: The socket constructor already took care of all validation.
   *
   * @param host
   * @param port
   * @param timeout
   * @param noLingerOrDirectTCP
   * @throws totalcross.net.UnknownHostException
   * @throws totalcross.io.IOException
   */
  final private void socketCreate(final String host, final int port, final int timeout)
      throws totalcross.net.UnknownHostException, totalcross.io.IOException {
    try {
      java.net.Socket socket = new java.net.Socket();
      socketRef = socket;
      socket.connect(new java.net.InetSocketAddress(host, port), timeout);
    } catch (java.lang.SecurityException e) {
      throw new totalcross.io.IOException(e.getMessage());
    } catch (java.net.UnknownHostException e) {
      throw new totalcross.net.UnknownHostException(e.getMessage());
    } catch (java.io.IOException e) {
      throw new totalcross.io.IOException(e.getMessage());
    }

    try {
      ((java.net.Socket) socketRef).setSoTimeout(readTimeout);
      myInputStream = ((java.net.Socket) socketRef).getInputStream();
      myOutputStream = ((java.net.Socket) socketRef).getOutputStream();
    } catch (java.io.IOException e1) {
      try {
        ((java.net.Socket) socketRef).close();
        throw new totalcross.io.IOException(e1.getMessage());
      } catch (java.io.IOException e2) {
        throw new totalcross.io.IOException(e1.getMessage());
      }
    }
  }

  /**
   * Closes the socket.
   *
   * @throws totalcross.io.IOException If the socket is closed more than once
   */
  @Override
  public void close() throws totalcross.io.IOException {
    if (socketRef == null) {
      throw new totalcross.io.IOException("The socket is already closed.");
    }

    try {
      nativeClose();
    } finally {
      socketRef = null;
    }
  }

  /**
   * Native implementation for socket close. <br>
   * NOTE: All validation was done by the public close method.
   *
   * @throws totalcross.io.IOException
   */
  final private void nativeClose() throws totalcross.io.IOException {
    try {
      ((java.net.Socket) this.socketRef).close();
    } catch (java.io.IOException e) {
      throw new totalcross.io.IOException(e.getMessage());
    }
  }

  /**
   * Reads bytes from the socket into a byte array. Note that in the device it
   * may return with less bytes than requested. Note also that, if -1 is
   * returned and the lastError is 4626, please insist 3 or 4 more times,
   * because it may work.
   *
   * @param buf the byte array to read data into
   * @param start the start position in the byte array
   * @param count the number of bytes to read
   * @return The number of bytes actually read; or &lt;= 0 if no data is
   *         available; or -1 if the server closed the connection or an error
   *         prevented the read operation from occurring.
   * @throws totalcross.io.IOException
   */
  @Override
  public int readBytes(byte buf[], int start, int count) throws totalcross.io.IOException {
    if (socketRef == null) {
      throw new totalcross.io.IOException("The socket is closed.");
    }
    if (buf == null) {
      throw new NullPointerException();
    }
    if (start < 0 || count < 0 || start + count > buf.length) {
      throw new IndexOutOfBoundsException();
    }
    if (count == 0) {
      return 0;
    }

    return readWriteBytes(buf, start, count, true);
  }

  /**
   * Native implementation for reading and writing bytes from a socket.
   * NOTE: All validation was done by the read/write methods.
   */
  final private int readWriteBytes(byte buf[], int start, int count, boolean isRead) throws totalcross.io.IOException {
    if (isRead) {
      if (myInputStream == null) {
        try {
          myInputStream = ((java.net.Socket) socketRef).getInputStream();
        } catch (java.io.IOException e) {
          throw new totalcross.io.IOException(e.getMessage());
        }
      }
      try {
        ((java.net.Socket) socketRef).setSoTimeout(readTimeout < 0 ? 0 : readTimeout);
      } catch (java.net.SocketException e) {
        throw new totalcross.io.IOException(e.getMessage());
      }

      try {
        return ((java.io.InputStream) myInputStream).read(buf, start, count);
      } catch (java.net.SocketTimeoutException e) {
        throw new totalcross.net.SocketTimeoutException(e.getMessage());
      } catch (java.io.IOException e) {
        throw new totalcross.io.IOException(e.getMessage());
      }
    } else {
      if (myOutputStream == null) {
        try {
          myOutputStream = ((java.net.Socket) socketRef).getOutputStream();
        } catch (java.io.IOException e) {
          throw new totalcross.io.IOException(e.getMessage());
        }
      }

      try {
        ((java.io.OutputStream) this.myOutputStream).write(buf, start, count);
        return count;
      } catch (IOException e) {
        throw new totalcross.io.IOException(e.getMessage());
      }
    }
  }

  /**
   * Reads bytes from the socket into a byte array, from offset 0 to
   * buf.length. Note that in the device it may return with less bytes than
   * requested. Note also that, if -1 is returned and the lastError is 4626,
   * please insist 3 or 4 more times, because it may work.
   *
   * @param buf
   *           the byte array to read data into
   * @return The number of bytes actually read; or &lt;= 0 if no data is
   *         available; or -1 if the server closed the connection or an error
   *         prevented the read operation from occurring.
   * @throws totalcross.io.IOException
   * @since SuperWaba 5.6
   * @see #readBytes(byte[], int, int)
   */
  public int readBytes(byte buf[]) throws totalcross.io.IOException {
    if (socketRef == null) {
      throw new totalcross.io.IOException("The socket is closed.");
    }
    if (buf == null) {
      throw new java.lang.NullPointerException();
    }
    if (buf.length == 0) {
      return 0;
    }

    return readWriteBytes(buf, 0, buf.length, true);
  }

  /**
   * Writes to the socket. Returns the number of bytes written or -1 if an
   * error prevented the write operation from occurring. If data can't be
   * written to the socket for approximately 2 seconds, the write operation
   * will time out.
   *
   * @param buf   the byte array to write data from
   * @param start the start position in the byte array
   * @param count the number of bytes to write
   * @throws totalcross.io.IOException
   */
  @Override
  public int writeBytes(byte buf[], int start, int count) throws totalcross.io.IOException {
    if (socketRef == null) {
      throw new totalcross.io.IOException("The socket is closed.");
    }
    if (buf == null) {
      throw new NullPointerException();
    }
    if (start < 0 || count < 0 || start + count > buf.length) {
      throw new IndexOutOfBoundsException();
    }
    if (count == 0) {
      return 0;
    }

    return readWriteBytes(buf, start, count, false);
  }

  private byte[] rlbuf; // guich@tc123_48: use a byte[] buffer instead of a StringBuffer

  /**
   * Reads a line of text from this socket. Any char lower than space
   * is considered a new line separator. This method correctly handles newlines
   * with \\n or \\r\\n.
   * <br><br>Note: this method is VERY slow since it reads a single character per time. Consider using 
   * new LineReader(socket) or new BufferedStream(socket) instead.
   *
   * @return the read line or <code>null</code> if nothing was read.
   * @throws totalcross.io.IOException
   * @since SuperWaba 5.61
   * @see totalcross.io.LineReader
   * @see totalcross.io.BufferedStream
   */
  public String readLine() throws totalcross.io.IOException {
    if (socketRef == null) {
      throw new totalcross.io.IOException("The socket is closed.");
    }

    if (rlbuf == null) {
      rlbuf = new byte[256];
    }

    byte[] buf = rlbuf;
    int pos = 0;
    int r;
    while ((r = readWriteBytes(buf, pos, 1, true)) == 1) {
      if (buf[pos] == '\n') // guich@tc123_47
      {
        if (pos > 0 && buf[pos - 1] == '\r') {
          pos--;
        }
        // note that pos must be same of length, otherwise the String will be constructed with one less character
        break;
      }
      if (++pos == buf.length) // reached buffer size?
      {
        byte[] temp = new byte[buf.length + 256];
        Vm.arrayCopy(buf, 0, temp, 0, pos);
        rlbuf = buf = temp;
      }
    }
    return (pos > 0 || r == 1) ? new String(Convert.charConverter.bytes2chars(buf, 0, pos)) : null; // brunosoares@582_11
  }

  @Override
  protected void finalize() {
    try {
      close();
    } catch (totalcross.io.IOException e) {
    }
  }

  /** Used internally by the SSL classes. Not available at the device. */
  public Object getNativeSocket() {
    return socketRef;
  }

  /**
   * Returns the remote host which this socket is connected to, passed in the constructor.
   */
  public String getHost() {
    return host;
  }

  /**
   * Returns the remote port which this socket is connected to, passed in the constructor.
   */
  public int getPort() {
    return port;
  }
}
