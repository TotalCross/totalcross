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
public abstract class SSLSocketFactory extends SocketFactory {

	@Override
	public Socket createSocket(String host, int port) throws UnknownHostException, IOException {
		return createSocket(host, port, Socket.DEFAULT_OPEN_TIMEOUT);
	}

	@Override
	public abstract Socket createSocket(String host, int port, int timeout) throws UnknownHostException, IOException;
}
