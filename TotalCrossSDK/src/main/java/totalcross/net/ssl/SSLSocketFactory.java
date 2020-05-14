// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
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
