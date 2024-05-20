/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  This file is covered by the GNU LESSER GENERAL PUBLIC LICENSE VERSION 3.0    *
 *  A copy of this license is located in file license.txt at the root of this    *
 *  SDK or can be downloaded here:                                               *
 *  http://www.gnu.org/licenses/lgpl-3.0.txt                                     *
 *                                                                               *
 *********************************************************************************/

package totalcross.net.ssl;

import totalcross.io.IOException;
import totalcross.net.Socket;
import totalcross.net.UnknownHostException;

public abstract class SSLContextSpi {
    protected final String name;

    protected SSLContextSpi(String name) {
        this.name = name;
    }

    private final SSLSocketFactory factory = new SSLSocketFactory() {
        public Socket createSocket(String host, int port, int timeout) throws UnknownHostException, IOException {
            return new SSLSocket(SSLContextSpi.this, host, port, timeout);
        }
    };

    public SSLSocketFactory getSocketFactory() {
        return factory;
    }

    abstract void init(Socket socket) throws IOException;

    abstract void startHandshake(Socket socket) throws IOException;

    abstract int readWriteBytes(Socket socket, byte[] buf, int start, int count, boolean isRead) throws IOException;

    abstract void close() throws IOException;
}
