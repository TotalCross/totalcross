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

/**
 * ServerSocket is a TCP/IP network server socket.
 * <p>
 * Under Java and Windows CE, if no network is present, the socket constructor
 * may hang for an extended period of time due to the implementation of sockets
 * in the underlying OS. This is a known problem.
 * <p>
 * Here is an example showing the server accepting a client connection and
 * writing/reading some data to/from it:
 *
 * <pre>
 * ServerSocket server = new ServerSocket(1024);
 * Socket client = server.accept();
 * byte[] bytes = &quot;HELLO WORLD&quot;.getBytes();
 * client.writeBytes(bytes, 0, bytes.length);
 * byte[] buf = new byte[10];
 * int count = client.readBytes(buf, 0, buf.length);
 * if (count == buf.length)
 *    ...
 * client.close();
 * server.close();
 * </pre>
 *
 * Important: you cannot open a server socket before the main event loop. In other
 * words, you cannot open a socket in the app's constructor, but CAN in the
 * initUI method.
 * <p>
 */
public class ServerSocket {
  Object serverRef;
  String addr;
  int port;

  /**
   * Stores the timeout value for accept operations. The value specifies the
   * number of milliseconds to wait before timing out an accept operation.
   * @see #WAIT_FOREVER
   */
  int timeout = DEFAULT_SOTIMEOUT;

  /** The default value for the accept operation timeout. */
  public static final int DEFAULT_SOTIMEOUT = 10000;
  /** The default backlog value */
  public static final int DEFAULT_BACKLOG = 100;

  /** Passing a value of 0 to the constructor causes any accept operation to wait indefinitely. */
  public static final int WAIT_FOREVER = 0;

  /** For internal use only */
  protected ServerSocket() {
  }

  /**
   * Opens a server socket. This method establishes a server socket connection
   * at the specified port, with the default timeout for accept operations.
   *
   * @param port the local TCP port to listen for incoming connections
   * @throws totalcross.io.IOException
   */
  public ServerSocket(int port) throws totalcross.io.IOException {
    this(port, DEFAULT_SOTIMEOUT, DEFAULT_BACKLOG, null);
  }

  /**
   * Opens a server socket. This method establishes a server socket connection
   * at the specified port, with the specified timeout for accept operations.
   *
   * @param port the local TCP port to listen for incoming connections
   * @param timeout the accept operation timeout
   * @throws totalcross.io.IOException
   */
  public ServerSocket(int port, int timeout) throws totalcross.io.IOException {
    this(port, timeout, DEFAULT_BACKLOG, null);
  }

  /**
   * Opens a server socket. This method establishes a server socket connection
   * at the specified port, with the specified timeout for accept operations.
   * The addr argument can be used on a multi-homed host for a ServerSocket that
   * will only accept connect requests to one of its addresses.
   *
   * @param port the local TCP port to listen for incoming connections
   * @param timeout the accept operation timeout
   * @param addr the local address this server will bind to
   * @throws totalcross.io.IOException
   */
  public ServerSocket(int port, int timeout, String addr) throws totalcross.io.IOException {
    this(port, timeout, DEFAULT_BACKLOG, addr);
  }

  /**
   * Opens a server socket. This method establishes a server socket connection
   * at the specified port, with the specified timeout for accept operations.
   * The addr argument can be used on a multi-homed host for a ServerSocket that
   * will only accept connect requests to one of its addresses.
   *
   * @param port the local TCP port to listen for incoming connections
   * @param timeout the accept operation timeout
   * @param backlog the limit of concurrent connections that are accepted.
   * @param addr the local address this server will bind to
   * @throws totalcross.io.IOException
   */
  public ServerSocket(int port, int timeout, int backlog, String addr) throws totalcross.io.IOException {
    if (port < 0 || port > 65535) {
      throw new java.lang.IllegalArgumentException("Invalid value for argument 'port': " + port);
    }
    if (timeout < 0) {
      throw new java.lang.IllegalArgumentException("Invalid value for argument 'timeout': " + timeout);
    }
    if (backlog <= 0) {
      throw new java.lang.IllegalArgumentException("Invalid value for argument 'backlog': " + backlog);
    }

    this.port = port;
    this.timeout = timeout;
    this.addr = addr;

    try {
      serverRef = new java.net.ServerSocket(port, backlog);
      ((java.net.ServerSocket) serverRef).setSoTimeout(timeout);
    } catch (java.net.SocketException e) {
      throw new totalcross.io.IOException(e.getMessage());
    } catch (java.io.IOException e) {
      throw new totalcross.io.IOException(e.getMessage());
    }
  }

  /**
   * Returns the local address of this server socket.
   * @return the address to which this socket is bound, or null if the socket
   * is unbound.
   */
  public String getHost() {
    return addr;
  }

  /**
   * Returns the local TCP port on which this socket is listening, passed in the constructor.
   * @return the port number to which this socket is listening.
   */
  public int getLocalPort() {
    return port;
  }

  /**
   * Listens for a connection to be made to this socket and accepts it, returning
   * a Socket representing the connection established with the client. This
   * method blocks until a connection is made or the operation times out.
   *
   * @return the client Socket
   * @throws totalcross.io.IOException
   */
  public Socket accept() throws totalcross.io.IOException {
    if (serverRef == null) {
      throw new totalcross.io.IOException("The server socket is closed");
    }

    totalcross.net.Socket clientSocket = nativeAccept();
    return clientSocket;
  }

  final private totalcross.net.Socket nativeAccept() throws totalcross.io.IOException {
    totalcross.net.Socket clientSocket = null;
    java.net.Socket socketRef;

    try {
      java.net.ServerSocket ss = (java.net.ServerSocket) serverRef;
      ss.setSoTimeout(timeout);
      if ((socketRef = ss.accept()) != null) {
        clientSocket = new totalcross.net.Socket();
        clientSocket.socketRef = socketRef;
      }
      return clientSocket;
    } catch (java.net.SocketTimeoutException e) {
      return null;
    } catch (java.io.IOException e) {
      throw new totalcross.io.IOException(e.getMessage());
    }
  }

  /**
   * Closes the server socket.
   * @throws totalcross.io.IOException
   */
  public void close() throws totalcross.io.IOException {
    if (serverRef == null) {
      throw new totalcross.io.IOException("The server socket is closed");
    }

    try {
      nativeClose();
    } finally {
      serverRef = null;
    }
  }

  private void nativeClose() throws totalcross.io.IOException {
    try {
      ((java.net.ServerSocket) serverRef).close();
    } catch (java.io.IOException e) {
      throw new totalcross.io.IOException(e.getMessage());
    }
  }

  @Override
  protected void finalize() {
    try {
      close();
    } catch (totalcross.io.IOException e) {
    }
  }
}
