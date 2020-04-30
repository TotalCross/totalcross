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

/**
 * Constants use in the LiteSSL package.
 */
public interface Constants {
  /** Library version number */
  static final String versionStr = "1.0.0";
  static final int version = 100;

  /** The optional parameters that can be given to the client/server SSL engine */

  /** Enable the client authentication support in the context. */
  public final static int SSL_CLIENT_AUTHENTICATION = 0x00010000;
  /** Differ the server authentication in the context. */
  public final static int SSL_SERVER_VERIFY_LATER = 0x00020000;
  /** Differ the server authentication in the context. */

  //   public final static int SSL_NO_DEFAULT_KEY        = 0x00040000;
  //   public final static int SSL_DISPLAY_STATES        = 0x00080000;
  //   public final static int SSL_DISPLAY_BYTES         = 0x00100000;
  //   public final static int SSL_DISPLAY_CERTS         = 0x00200000;
  //   public final static int SSL_DISPLAY_RSA           = 0x00400000;

  /** errors that can be generated */

  /** SSL handshake is in progress. */
  public final static int SSL_HANDSHAKE_IN_PROGRESS = 1;
  /** The operation succeeded. */
  public final static int SSL_OK = 0;
  /** The operation failed. */
  public final static int SSL_NOT_OK = -1;
  /** A fatal error occurred. */
  public final static int SSL_ERROR_DEAD = -2;
  /** The SSL connection have been lost. */
  public final static int SSL_ERROR_CONN_LOST = -256;
  /** The underlaying socket couldn't be configured. */
  public final static int SSL_ERROR_SOCK_SETUP_FAILURE = -258;
  /** The SSL handshake failed. */
  public final static int SSL_ERROR_INVALID_HANDSHAKE = -260;
  /** An SSL protocol failure occurred. */
  public final static int SSL_ERROR_INVALID_PROT_MSG = -261;
  /** Bad message authentication code. */
  public final static int SSL_ERROR_INVALID_HMAC = -262;
  /** Invalid version. */
  public final static int SSL_ERROR_INVALID_VERSION = -263;
  /** Invalid session. */
  public final static int SSL_ERROR_INVALID_SESSION = -265;
  /** No cipher method available. */
  public final static int SSL_ERROR_NO_CIPHER = -266;
  /** The certificate is not valid. */
  public final static int SSL_ERROR_BAD_CERTIFICATE = -268;
  public final static int SSL_ERROR_INVALID_KEY = -269;
  public final static int SSL_ERROR_FINISHED_INVALID = -271;
  public final static int SSL_ERROR_NO_CERT_DEFINED = -272;
  public final static int SSL_ERROR_TOO_MANY_CERTS = -273;
  public final static int SSL_ERROR_NOT_SUPPORTED = -274;

  /** X509 verifying errors */

  public final static int X509_NOT_OK = -513;
  public final static int X509_VFY_ERROR_NO_TRUSTED_CERT = -514;
  public final static int X509_VFY_ERROR_BAD_SIGNATURE = -515;
  public final static int X509_VFY_ERROR_NOT_YET_VALID = -516;
  public final static int X509_VFY_ERROR_EXPIRED = -517;
  public final static int X509_VFY_ERROR_SELF_SIGNED = -518;
  public final static int X509_VFY_ERROR_INVALID_CHAIN = -519;
  public final static int X509_VFY_ERROR_UNSUPPORTED_DIGEST = -520;
  public final static int X509_INVALID_PRIV_KEY = -521;

  /** these are all the alerts that are recognized */

  /** The SSL connection has been closed. */
  public final static int SSL_ALERT_CLOSE_NOTIFY = 0;
  /** The SSL encountered an unexpected message. */
  public final static int SSL_ALERT_UNEXPECTED_MESSAGE = 10;
  /** Bad message MAC checksum. */
  public final static int SSL_ALERT_BAD_RECORD_MAC = 20;
  /** An SSL handshake failure occurred. */
  public final static int SSL_ALERT_HANDSHAKE_FAILURE = 40;
  /** SSL received a bad certificate. */
  public final static int SSL_ALERT_BAD_CERTIFICATE = 42;
  public final static int SSL_ALERT_ILLEGAL_PARAMETER = 47;
  public final static int SSL_ALERT_DECODE_ERROR = 50;
  public final static int SSL_ALERT_DECRYPT_ERROR = 51;
  public final static int SSL_ALERT_INVALID_VERSION = 70;

  /** The ciphers that are supported.
   * See http://www.rfc-archive.org/getrfc.php?rfc=3268 and
   * section A.5 in http://www.rfc-archive.org/getrfc.php?rfc=2246
   */
  public final static int TLS_RSA_WITH_AES_128_CBC_SHA = 0x2f;
  public final static int TLS_RSA_WITH_AES_256_CBC_SHA = 0x35;
  public final static int TLS_RSA_WITH_RC4_128_SHA = 0x05;
  public final static int TLS_RSA_WITH_RC4_128_MD5 = 0x04;

  /** build mode ids' */

  public final static int SSL_BUILD_SKELETON_MODE = 0x01;
  public final static int SSL_BUILD_SERVER_ONLY = 0x02;
  public final static int SSL_BUILD_ENABLE_VERIFICATION = 0x03;
  public final static int SSL_BUILD_ENABLE_CLIENT = 0x04;
  public final static int SSL_BUILD_FULL_MODE = 0x05;

  /** offsets to retrieve configuration information */

  public final static int SSL_BUILD_MODE = 0;
  public final static int SSL_MAX_CERT_CFG_OFFSET = 1;
  public final static int SSL_MAX_CA_CERT_CFG_OFFSET = 2;
  public final static int SSL_HAS_PEM = 3;

  /** default session sizes */

  public final static int SSL_DEFAULT_SVR_SESS = 5;
  public final static int SSL_DEFAULT_CLNT_SESS = 1;

  /** X.509/X.520 distinguished name types */

  public final static int SSL_X509_CERT_COMMON_NAME = 0;
  public final static int SSL_X509_CERT_ORGANIZATION = 1;
  public final static int SSL_X509_CERT_ORGANIZATIONAL_NAME = 2;
  public final static int SSL_X509_CA_CERT_COMMON_NAME = 3;
  public final static int SSL_X509_CA_CERT_ORGANIZATION = 4;
  public final static int SSL_X509_CA_CERT_ORGANIZATIONAL_NAME = 5;

  /** SSL object loader types */

  /** X.509 client/server certificate. */
  public final static int SSL_OBJ_X509_CERT = 1;
  /** X.509 CA certificate. */
  public final static int SSL_OBJ_X509_CACERT = 2;
  /** RSA private key. */
  public final static int SSL_OBJ_RSA_KEY = 3;
  /** PKCS8 encrypted private key. */
  public final static int SSL_OBJ_PKCS8 = 4;
  /** PKCS12 certificate or private key. */
  public final static int SSL_OBJ_PKCS12 = 5;

  /** need to predefine before ssl_lib.h gets to it */
  public final static int SSL_SESSION_ID_SIZE = 32;
}
