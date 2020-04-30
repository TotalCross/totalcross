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

public class ServerSocket4D {
  private Object serverRef;
  private String addr;
  private int port;
  int timeout = DEFAULT_SOTIMEOUT;
  boolean dontFinalize;

  public static final int DEFAULT_SOTIMEOUT = 10000;
  public static final int DEFAULT_BACKLOG = 100;
  public static final int WAIT_FOREVER = 0;

  public ServerSocket4D(int port) throws totalcross.io.IOException {
    this(port, DEFAULT_SOTIMEOUT, DEFAULT_BACKLOG, null);
  }

  public ServerSocket4D(int port, int timeout) throws totalcross.io.IOException {
    this(port, timeout, DEFAULT_BACKLOG, null);
  }

  public ServerSocket4D(int port, int timeout, String addr) throws totalcross.io.IOException {
    this(port, timeout, DEFAULT_BACKLOG, addr);
  }

  public ServerSocket4D(int port, int timeout, int backlog, String addr) throws totalcross.io.IOException {
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
    serversocketCreate(port, backlog, timeout, addr);
  }

  native void serversocketCreate(int port, int backlog, int timeout, String addr) throws totalcross.io.IOException;

  public String getHost() {
    return addr;
  }

  public int getLocalPort() {
    return port;
  }

  public Socket accept() throws totalcross.io.IOException {
    if (serverRef == null) {
      throw new totalcross.io.IOException("The server socket is closed");
    }

    totalcross.net.Socket clientSocket = nativeAccept();
    return clientSocket;
  }

  native totalcross.net.Socket nativeAccept() throws totalcross.io.IOException;

  public void close() throws totalcross.io.IOException {
    if (serverRef == null) {
      throw new totalcross.io.IOException("The server socket is closed");
    }
    nativeClose();
  }

  native private void nativeClose() throws totalcross.io.IOException;

  @Override
  protected void finalize() {
    try {
      close();
    } catch (totalcross.io.IOException e) {
    }
  }
}
