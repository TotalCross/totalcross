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

import totalcross.crypto.CryptoException;
import totalcross.io.ByteArrayStream;
import totalcross.io.IOException;
import totalcross.net.Socket;
import totalcross.net.UnknownHostException;
import totalcross.sys.Vm;

/**
 * This class extends Sockets and provides secure socket using protocols such as the "Secure Sockets Layer" (SSL) or
 * IETF "Transport Layer Security" (TLS) protocols.<br>
 * 
 * The initial handshake on this connection is initiated by calling startHandshake which explicitly begins handshakes.
 * If handshaking fails for any reason, the SSLSocket is closed, and no further communications can be done.
 */
public class SSLSocket extends Socket {
  private SSLClient sslClient;
  private SSL sslConnection;
  private SSLReadHolder sslReader;
  private ByteArrayStream buffer = null;

  /**
   * Constructs an SSL connection to a named host at a specified port, with the specified connection timeout, binding
   * the client side of the connection a given address and port. This acts as the SSL client.
   * 
   * @param host
   *           the server's host
   * @param port
   *           its port
   * @param timeout
   *           the timeout for this operation
   * @throws UnknownHostException
   *            if the host is not known
   * @throws IOException
   *            if an I/O error occurs when creating the socket
   */
  public SSLSocket(String host, int port, int timeout) throws UnknownHostException, IOException {
    super(host, port, timeout);
  }

  /**
   * Creates a new SSLClient to be used by this instance of SSLSocket during the handshake. The default implementation
   * does not perform any kind of validation. Subclasses may override this method to use their own implementation of
   * SSLClient.
   * 
   * @return a SSLClient initialized with the objects required to perform the validation for this socket.
   * @throws CryptoException
   */
  protected SSLClient prepareContext() throws CryptoException {
    return new SSLClient(Constants.SSL_SERVER_VERIFY_LATER, 0);
  }

  /**
   * Starts an SSL handshake on this connection.
   * 
   * @throws IOException
   *            on a network level error
   */
  public void startHandshake() throws IOException {
    try {
      sslClient = prepareContext();
      sslConnection = sslClient.connect(this, null);
      Exception e = sslConnection.getLastException();
      if (e != null) {
        throw new IOException(e.getMessage());
      }
      int status;
      for (int elapsedTime = 0; (status = sslConnection.handshakeStatus()) == Constants.SSL_HANDSHAKE_IN_PROGRESS
          && elapsedTime < super.readTimeout; elapsedTime += 25) {
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
        this.close();
      } catch (IOException e2) {
      }
      if (e instanceof IOException) {
        throw (IOException) e;
      }
      throw new IOException(e.getMessage());
    }
  }

  @Override
  public int readBytes(byte[] buf, int start, int count) throws IOException {
    if (buffer == null) {
      return super.readBytes(buf, start, count);
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
  }

  @Override
  public int readBytes(byte[] buf) throws IOException {
    return this.readBytes(buf, 0, buf.length);
  }

  @Override
  public int writeBytes(byte[] buf, int start, int count) throws IOException {
    if (buffer == null) {
      return super.writeBytes(buf, start, count);
    }
    if (start > 0) {
      byte[] buf2 = new byte[count];
      Vm.arrayCopy(buf, start, buf2, 0, count);
      buf = buf2;
    }
    return sslConnection.write(buf, count);
  }

  @Override
  public void close() throws IOException {
    if (buffer != null) {
      buffer = null;
    }
    if (sslConnection != null) {
      sslConnection.dispose();
    }
    if (sslClient != null) {
      sslClient.dispose();
    }
    super.close();
  }
}
