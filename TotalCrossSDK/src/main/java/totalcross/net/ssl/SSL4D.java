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

/*
 * A wrapper around the unmanaged interface to give a semi-decent Java API
 */

package totalcross.net.ssl;

/**
 * @defgroup java_api Java API.
 *
 * Ensure that the appropriate dispose() methods are called when finished with
 * various objects - otherwise memory leaks will result.
 */

/**
 * A representation of an SSL connection.
 */
public class SSL4D {
  /** A pointer to the real SSL type. For internal use only. */
  long ssl;
  boolean dontFinalize;

  protected Object nativeHeap;

  /**
   * Store the reference to an SSL context.
   * @param ssl [in] A reference to an SSL object.
   */
  protected SSL4D() {
  }

  /**
   * Free any used resources on this connection.
   *
   * A "Close Notify" message is sent on this connection (if possible). It
   * is up to the application to close the socket.
   */
  native public void dispose4D();

  /**
   * Return the result of a handshake.
   * @return SSL_OK if the handshake is complete and ok.
   */
  native public int handshakeStatus4D();

  /**
   * Return the SSL cipher id.
   * @return The cipher id which is one of:
   * - TLS_RSA_WITH_AES_128_CBC_SHA  (0x2f)
   * - TLS_RSA_WITH_AES_256_CBC_SHA  (0x35)
   * - TLS_RSA_WITH_RC4_128_SHA      (0x05)
   * - TLS_RSA_WITH_RC4_128_MD5      (0x04)
   */
  native public byte getCipherId4D();

  /**
   * Get the session id for a handshake.
   *
   * This will be a 32 byte sequence and is available after the first
   * handshaking messages are sent.
   * @return The session id as a 32 byte sequence.
   * @note A SSLv23 handshake may have only 16 valid bytes.
   */
  native public byte[] getSessionId4D();

  /**
   * Retrieve an X.509 distinguished name component.
   *
   * When a handshake is complete and a certificate has been exchanged, then
   * the details of the remote certificate can be retrieved.
   *
   * This will usually be used by a client to check that the server's common
   * name matches the URL.
   *
   * A full handshake needs to occur for this call to work.
   *
   * @param component [in] one of:
   * - SSL_X509_CERT_COMMON_NAME
   * - SSL_X509_CERT_ORGANIZATION
   * - SSL_X509_CERT_ORGANIZATIONAL_NAME
   * - SSL_X509_CA_CERT_COMMON_NAME
   * - SSL_X509_CA_CERT_ORGANIZATION
   * - SSL_X509_CA_CERT_ORGANIZATIONAL_NAME
   * @return The appropriate string (or null if not defined)
   */
  native public String getCertificateDN4D(int component);

  /**
   * Read the SSL data stream.
   * @param rh [out] After a successful read, the decrypted data can be
   * retrieved with rh.getData(). It will be null otherwise.
   * @return The number of decrypted bytes:
   * - if > 0, then the handshaking is complete and we are returning the
   * number of decrypted bytes.
   * - SSL_OK if the handshaking stage is successful (but not yet complete).
   * - < 0 if an error.
   * @note Use rh before doing any successive ssl calls.
   */
  native public int read4D(SSLReadHolder rh);

  /**
   * Write to the SSL data stream.
   * @param out_data [in] The data to be written
   * @return The number of bytes sent, or if < 0 if an error.
   */
  final public int write(byte[] out_data) {
    return write4D(out_data, out_data.length);
  }

  /**
   * Write to the SSL data stream.
   * @param out_data [in] The data to be written
   * @param out_len [in] The number of bytes to be written
   * @return The number of bytes sent, or if < 0 if an error.
   */
  native public int write4D(byte[] out_data, int out_len);

  /**
   * Authenticate a received certificate.
   * This call is usually made by a client after a handshake is complete
   * and the context is in SSL_SERVER_VERIFY_LATER mode.
   * @return SSL_OK if the certificate is verified.
   */
  native public int verifyCertificate4D();

  /**
   * Force the client to perform its handshake again.
   * For a client this involves sending another "client hello" message.
   * For the server is means sending a "hello request" message.
   * This is a blocking call on the client (until the handshake completes).
   * @return SSL_OK if renegotiation instantiation was ok
   */
  native public int renegotiate4D();

  public final Exception getLastException() {
    return null;
  }

  @Override
  protected void finalize() {
    dispose4D();
  }
}
