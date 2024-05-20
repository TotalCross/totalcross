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

public class SSLContextMbedtls extends SSLContextSpi {

    @SuppressWarnings("unused")
    private byte[] context;

    @SuppressWarnings("unused")
    private byte[] mbedtls_net_context;

    @SuppressWarnings("unused")
    private byte[] mbedtls_entropy_context;

    @SuppressWarnings("unused")
    private byte[] mbedtls_ctr_drbg_context;

    @SuppressWarnings("unused")
    private byte[] mbedtls_ssl_context;

    @SuppressWarnings("unused")
    private byte[] mbedtls_ssl_config;

    protected SSLContextMbedtls() {
        super("mbedtls");
    }

    @Override
    native void init(Socket socket);

    @Override
    native int readWriteBytes(Socket socket, byte[] buf, int start, int count, boolean isRead) throws IOException;

    @Override
    void startHandshake(Socket socket) throws IOException {
        // TODO Auto-generated method stub
        // throw new UnsupportedOperationException("Unimplemented method
        // 'startHandshake'");
    }

    @Override
    native void close() throws IOException;
}
