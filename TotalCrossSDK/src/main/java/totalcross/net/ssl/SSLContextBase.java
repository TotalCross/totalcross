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

import totalcross.crypto.CryptoException;
import totalcross.io.ByteArrayStream;
import totalcross.io.IOException;
import totalcross.net.Socket;
import totalcross.sys.Vm;

public class SSLContextBase extends SSLContextSpi {

    private SSLClient sslClient;
    private SSL sslConnection;
    private SSLReadHolder sslReader;
    private ByteArrayStream buffer = null;

    protected SSLContextBase() {
        super("base");
    }

    @Override
    void init(Socket socket) throws IOException {
        // startHandshake(socket);
    }

    protected SSLClient prepareContext() throws CryptoException {
        return new SSLClient(Constants.SSL_SERVER_VERIFY_LATER, 0);
    }

    @Override
    void startHandshake(Socket socket) throws IOException {
        try {
            sslClient = prepareContext();
            sslConnection = sslClient.connect(socket, null);
            Exception e = sslConnection.getLastException();
            if (e != null) {
                throw new IOException(e.getMessage());
            }
            int status;
            for (int elapsedTime = 0; (status = sslConnection.handshakeStatus()) == Constants.SSL_HANDSHAKE_IN_PROGRESS
                    && elapsedTime < socket.readTimeout; elapsedTime += 25) {
                Vm.sleep(25);
            }
            if (status != Constants.SSL_OK) {
                throw new IOException("SSL handshake failed: " + status);
            }
            sslReader = new SSLReadHolder();
            buffer = new ByteArrayStream(256);
            buffer.mark();
        } catch (Exception e) {
            try {
                socket.close();
            } catch (IOException e2) {
            }
            if (e instanceof IOException) {
                throw (IOException) e;
            }
            throw new IOException(e.getMessage());
        }
    }

    @Override
    int readWriteBytes(Socket socket, byte[] buf, int start, int count, boolean isRead) throws IOException {
        if (isRead) {
            if (buffer == null) {
                return socket.readBytes(buf, start, count);
            }
            if (buffer.available() == 0) {
                int sslReadBytes = sslConnection.read(sslReader);
                buffer.reuse();
                if (sslReadBytes > 0) {
                    buffer.writeBytes(sslReader.getData(), 0, sslReadBytes);
                }
                buffer.mark();
            }
            int readBytes = buffer.readBytes(buf, start, count);

            return readBytes;
        } else {
            if (buffer == null) {
                return socket.writeBytes(buf, start, count);
            }
            if (start > 0) {
                byte[] buf2 = new byte[count];
                Vm.arrayCopy(buf, start, buf2, 0, count);
                buf = buf2;
            }
            return sslConnection.write(buf, count);
        }
    }

    @Override
    void close() throws IOException {
        if (buffer != null) {
            buffer = null;
        }
        if (sslConnection != null) {
            sslConnection.dispose();
            sslConnection = null;
        }
        if (sslClient != null) {
            sslClient.dispose();
            sslClient = null;
        }
    }
}
