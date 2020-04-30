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

package totalcross.net.ssl;

import totalcross.io.IOException;
import totalcross.net.Socket;
import totalcross.net.SocketFactory;
import totalcross.net.UnknownHostException;

/**
 * SSLSocketFactory creates SSLSockets.
 */
public class SSLSocketFactory extends SocketFactory {
  private static SSLSocketFactory instance;

  public static SocketFactory getDefault() {
    if (instance == null) {
      instance = new SSLSocketFactory();
    }
    return instance;
  }

  @Override
  public Socket createSocket(String host, int port, int timeout) throws UnknownHostException, IOException {
    return new SSLSocket(host, port, timeout);
  }
}
