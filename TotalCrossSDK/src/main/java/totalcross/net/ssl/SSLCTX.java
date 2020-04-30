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

import java.io.ByteArrayInputStream;
import java.net.InetSocketAddress;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import totalcross.crypto.CryptoException;
import totalcross.crypto.NoSuchAlgorithmException;
import totalcross.io.IOException;
import totalcross.net.Socket;
import totalcross.sys.Vm;
import totalcross.util.Vector;

/**
 * A base object for SSLServer/SSLClient.
 */
public class SSLCTX {
  int options;
  int num_sessions;

  boolean dontFinalize; //flsobral@tc114_36: finalize support.

  private SSLContext ctx_ssl;
  private TrustManager[] trustMgrs;
  private Vector CACerts = new Vector();
  private Vector Certs = new Vector();
  private Vector Keys = new Vector();

  public static final String toSign = "TotalCross";

  /**
   * Establish a new client/server context.
   *
   * This function is called before any client/server SSL connections are
   * made.  If multiple threads are used, then each thread will have its
   * own SSLCTX context. Any number of connections may be made with a single
   * context.
   *
   * Each new connection will use the this context's private key and
   * certificate chain. If a different certificate chain is required, then a
   * different context needs to be be used.
   *
   * @param options [in]  Any particular options. At present the options
   * supported are:
   * - SSL_SERVER_VERIFY_LATER (client only): Don't stop a handshake if the
   * server authentication fails. The certificate can be authenticated
   * later with a call to verifyCert().
   * - SSL_CLIENT_AUTHENTICATION (server only): Enforce client authentication
   * i.e. each handshake will include a "certificate request" message
   * from the server.
   * - SSL_NO_DEFAULT_KEY: Don't use the default key/certificate. The user
   * will load the key/certificate explicitly.
   * - SSL_DISPLAY_BYTES (full mode build only): Display the byte sequences
   * during the handshake.
   * - SSL_DISPLAY_STATES (full mode build only): Display the state changes
   * during the handshake.
   * - SSL_DISPLAY_CERTS (full mode build only): Display the certificates that
   * are passed during a handshake.
   * - SSL_DISPLAY_RSA (full mode build only): Display the RSA key details
   * that are passed during a handshake.
   *
   * @param num_sessions [in] The number of sessions to be used for session
   * caching. If this value is 0, then there is no session caching.
   *
   * If this option is null, then the default internal private key/
   * certificate pair is used (if CONFIG_SSL_USE_DEFAULT_KEY is set).
   *
   * The resources used by this object are automatically freed.
   * @throws NoSuchAlgorithmException 
   */
  protected SSLCTX(int options, int num_sessions) throws NoSuchAlgorithmException {
    this.options = options;
    this.num_sessions = num_sessions;

    try {
      Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
      ctx_ssl = SSLContext.getInstance("TLS");
    } catch (java.security.NoSuchAlgorithmException e) {
      throw new NoSuchAlgorithmException(e.getMessage());
    }
  }

  /**
   * Remove a client/server context.
   * Frees any used resources used by this context. Each connection will be
   * sent a "Close Notify" alert (if possible).
   */
  final public void dispose() {
  }

  /**
   * Find an ssl object based on a Socket reference.
   *
   * Goes through the list of SSL objects maintained in a client/server
   * context to look for a socket match.
   * @param s [in] A reference to a totalcross.net.Socket object.
   * @return A reference to the SSL object. Returns null if the object
   * could not be found.
   */
  final public SSL find(Socket s) {
    return SSL.cacheGetSSL(s);
  }

  /**
   * Load security material (CA, Cert or private key) in binary DER or ASCII PEM format.
   * These information are used during the SSL protocol to authentication of SSL parts
   * and cyphering/uncyphering of the data exchanged between the two parts.
   * @param obj_type [in] The format of the file. Can be one of:
   * - SSL_OBJ_X509_CERT (no password required).
   * - SSL_OBJ_X509_CACERT (no password required).
   * - SSL_OBJ_RSA_KEY (AES128/AES256 PEM encryption supported). (not supported on Desktop)
   * - SSL_OBJ_P8 (RC4-128 encrypted data supported). (password protection not supported on Desktop)
   * - SSL_OBJ_P12 (RC4-128 encrypted data supported).
   * PEM encoded files are automatically detected and may contain several material,
   * whereas DER encoding only support one single material.
   * @param material [in] security material input stream.
   * @param password [in] The password used. Can be null if not required.
   * @return SSL_OK if the call succeeded
   * @throws CryptoException 
   * @throws NoSuchAlgorithmException 
   */
  final public int objLoad(int obj_type, totalcross.io.Stream material, String password)
      throws IOException, NoSuchAlgorithmException, CryptoException {
    byte buffer[] = new byte[1024];
    byte bytes[] = new byte[0];

    int count;
    while ((count = material.readBytes(buffer, 0, buffer.length)) > 0) {
      byte[] temp = new byte[bytes.length + count];
      Vm.arrayCopy(bytes, 0, temp, 0, bytes.length);
      Vm.arrayCopy(buffer, 0, temp, bytes.length, count);
      bytes = temp;
    }

    if (bytes != null) {
      return objLoad(obj_type, bytes, bytes.length, password);
    }

    return Constants.SSL_NOT_OK;
  }

  private final int objLoadOne(int obj_type, byte[] data, int len, String password)
      throws NoSuchAlgorithmException, CryptoException {
    try {
      if (obj_type == Constants.SSL_OBJ_PKCS8) {
        byte[] pkcs8_bytes = new byte[len];
        Vm.arrayCopy(data, 0, pkcs8_bytes, 0, len);
        PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(pkcs8_bytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        RSAPrivateKey privKey = (RSAPrivateKey) keyFactory.generatePrivate(privSpec);
        Keys.push(privKey);
        return Constants.SSL_OK;
      } else if (obj_type == Constants.SSL_OBJ_PKCS12) {
        KeyStore ks12 = KeyStore.getInstance("pkcs12");
        ks12.load(new ByteArrayInputStream(data, 0, len), password.toCharArray());

        java.util.Enumeration<?> aliases = ks12.aliases();
        while (aliases.hasMoreElements()) {
          String alias = (String) aliases.nextElement();
          java.security.cert.Certificate[] certchain = null;
          certchain = ks12.getCertificateChain(alias);
          if (certchain != null) {
            for (int k = 0; k < certchain.length; k++) {
              Certs.push((X509Certificate) certchain[k]);
            }
          }
          PrivateKey pke = null;
          pke = (PrivateKey) ks12.getKey(alias, password.toCharArray());
          if (pke != null) {
            Keys.push(pke);
          }
        }

        return Constants.SSL_OK;
      } else if (obj_type == Constants.SSL_OBJ_RSA_KEY) {
        throw new UnsupportedOperationException("Only pkcs8/pkcs12 encoding are supported for private keys");
      } else if (obj_type == Constants.SSL_OBJ_X509_CACERT || obj_type == Constants.SSL_OBJ_X509_CERT) {
        java.security.cert.CertificateFactory cf = java.security.cert.CertificateFactory.getInstance("X509");
        java.security.cert.X509Certificate x509certificate = (java.security.cert.X509Certificate) cf
            .generateCertificate(new ByteArrayInputStream(data, 0, len));
        if (x509certificate != null) {
          if (obj_type == Constants.SSL_OBJ_X509_CACERT) {
            if (CACerts.size() < SSLUtil.CONFIG_X509_MAX_CA_CERTS) {
              CACerts.push(x509certificate);
            }
          } else {
            if (Certs.size() < SSLUtil.CONFIG_SSL_MAX_CERTS) {
              Certs.push(x509certificate);
            }
          }
        }
        return Constants.SSL_OK;
      }
    } catch (java.security.NoSuchAlgorithmException e) {
      throw new NoSuchAlgorithmException(e.getMessage());
    } catch (java.security.GeneralSecurityException e) {
      throw new CryptoException(e.getMessage());
    } catch (java.io.IOException e) {
      throw new CryptoException(e.getMessage());
    }
    return Constants.SSL_NOT_OK;
  }

  /**
   * Load security material (CA, Cert or private key) in binary DER or ASCII PEM format.
   * These information are used during the SSL protocol to authenticate an SSL part
   * and cyphering/uncyphering of the data exchanged between the two parts.
   * @param obj_type [in] The format of the memory data.
   * - SSL_OBJ_X509_CERT (no password required).
   * - SSL_OBJ_X509_CACERT (no password required).
   * - SSL_OBJ_RSA_KEY (AES128/AES256 PEM encryption supported). (not supported on Desktop)
   * - SSL_OBJ_P8 (RC4-128 encrypted data supported). (password protection not supported on Desktop)
   * - SSL_OBJ_P12 (RC4-128 encrypted data supported).
   * PEM encoded data is automatically detected and may contain several material,
   * whereas DER encoding only support one single material.
   * @param data [in] The binary data to be loaded.
   * @param len [in] The amount of data to be loaded.
   * @param password [in] The password used. Can be null if not required.
   * @return SSL_OK if the call succeeded
   * @throws CryptoException 
   * @throws NoSuchAlgorithmException 
   */
  final public int objLoad(int obj_type, byte[] data, int len, String password)
      throws NoSuchAlgorithmException, CryptoException {
    if (ctx_ssl == null) {
      return -1;
    }

    int ret = Constants.SSL_OK;

    /* check if the data is a PEM content */
    boolean pem = false;
    String str_content = new String(data, 0, len);

    int start, end = -1;
    do {
      start = str_content.indexOf(SECTION_BEGIN);
      if (start > 0) {
        end = str_content.indexOf(SECTION_END);
        if (end > 0) {
          pem = true;
          int nl = str_content.indexOf('\n', end);
          String pem_data;
          if (nl > 0) {
            pem_data = str_content.substring(start, nl);
            str_content = str_content.substring(nl + 1);
          } else {
            pem_data = str_content.substring(start);
            str_content = "";
          }
          data = pem_data.getBytes();
          ret = objLoadOne(obj_type, data, data.length, password);
          if (ret != Constants.SSL_OK) {
            Vm.debug("Failed to load certificate:\r\n" + pem_data);
          }
        }
      }
    } while (start > 0 && end > 0);

    if (!pem) {
      ret = objLoadOne(obj_type, data, len, password);
    }
    return ret;
  }

  private final static String SECTION_BEGIN = "-----BEGIN ";
  private final static String SECTION_END = "-----END ";
  private final static char[] ks_pass = "123456".toCharArray();

  final public SSL newClient(Socket socket, byte[] session_id)
      throws IOException, NoSuchAlgorithmException, CryptoException {
    SSLSocketFactory sf = prepareSecurity();
    java.net.Socket ns = (java.net.Socket) socket.getNativeSocket();

    SSLSocket ssl_socket = null;
    try {
      InetSocketAddress inet = (InetSocketAddress) ns.getRemoteSocketAddress();
      ssl_socket = (SSLSocket) sf.createSocket(ns, inet.getHostName(), inet.getPort(), false);
      if (ssl_socket != null) {
        ssl_socket.setSoTimeout(socket.readTimeout < 0 ? 0 : socket.readTimeout);
        SSL ssl = new SSL(ssl_socket, socket);
        ssl._trustMgrs = trustMgrs;
        ssl_socket.setUseClientMode(true);
        ssl.renegotiate();
        return ssl;
      }
    } catch (java.io.IOException e) {
      throw new IOException(e.getMessage());
    }
    return null;
  }

  final public SSL newServer(Socket socket) throws IOException, NoSuchAlgorithmException, CryptoException {
    SSLSocketFactory sf = prepareSecurity();
    java.net.Socket ns = (java.net.Socket) socket.getNativeSocket();

    SSLSocket ssl_socket = null;
    try {
      InetSocketAddress inet = (InetSocketAddress) ns.getRemoteSocketAddress();
      ssl_socket = (SSLSocket) sf.createSocket(ns, inet.getHostName(), inet.getPort(), false);
      if (ssl_socket != null) {
        ssl_socket.setSoTimeout(socket.readTimeout < 0 ? 0 : socket.readTimeout);
        SSL ssl = new SSL(ssl_socket, socket);
        ssl._trustMgrs = trustMgrs;
        ssl_socket.setUseClientMode(false);
        ssl_socket.setNeedClientAuth((options & Constants.SSL_CLIENT_AUTHENTICATION) != 0);
        ssl.renegotiate();
        return ssl;
      }
    } catch (java.io.IOException e) {
      throw new IOException(e.getMessage());
    }
    return null;
  }

  private SSLSocketFactory prepareSecurity() throws IOException, NoSuchAlgorithmException, CryptoException {
    try {
      if (ctx_ssl == null) {
        return null;
      }

      KeyStore ts = null;
      KeyStore ks = null;

      ts = KeyStore.getInstance("JKS");
      //ts.load(new FileInputStream("truststore.ks"), password);
      ts.load(null, null);
      ks = KeyStore.getInstance("JKS");
      //ks.load(new FileInputStream("keystore.ks"), password);
      ks.load(null, null);

      //KeyStore inputKeyStore = KeyStore.getInstance("PKCS12");
      for (int i = CACerts.size() - 1; i >= 0; i--) {
        java.security.cert.X509Certificate caCert = (java.security.cert.X509Certificate) (CACerts.items[i]);
        ts.setCertificateEntry("ca" + i, caCert);
        ks.setCertificateEntry("ca" + i, caCert);
      }

      for (int i = Certs.size() - 1; i >= 0; i--) {
        java.security.cert.X509Certificate cert = (java.security.cert.X509Certificate) (Certs.items[i]);
        ks.setCertificateEntry("cert", cert);
      }

      for (int i = Keys.size() - 1; i >= 0; i--) {
        PrivateKey pkey = (PrivateKey) Keys.items[i];
        java.security.cert.Certificate[] certChain = buildChain(pkey, CACerts, Certs);
        if (certChain != null) {
          ks.setKeyEntry("cert", pkey, ks_pass, certChain);
        }
      }

      /* store both stores for checking...
         try
         {
            ts.store(new FileOutputStream("truststore.ks"), ks_pass);
            ks.store(new FileOutputStream("keystore.ks"), ks_pass);
         }
         catch (Exception e) { e.printStackTrace(); }
       */

      TrustManager[] tm = null;
      TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
      if (tmf != null) {
        tmf.init(ts);
        trustMgrs = tmf.getTrustManagers();
      }

      if ((options & Constants.SSL_SERVER_VERIFY_LATER) != 0) {
        tm = new TrustManager[] {
            // no check! trusts everyone
            new X509TrustManager() {
              /**
               * Doesn't throw an exception, so this is how it approves a certificate.
               * @see javax.net.ssl.X509TrustManager#checkClientTrusted(java.security.cert.X509Certificate[], String)
               **/
              @Override
              public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
              }

              /**
               * Doesn't throw an exception, so this is how it approves a certificate.
               * @see javax.net.ssl.X509TrustManager#checkServerTrusted(java.security.cert.X509Certificate[], String)
               **/
              @Override
              public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
              }

              /**
               * @see javax.net.ssl.X509TrustManager#getAcceptedIssuers()
               **/
              @Override
              public X509Certificate[] getAcceptedIssuers() {
                return null;
              }
            } };
      } else {
        tm = trustMgrs;
      }

      KeyManager[] km = null;
      KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
      if (kmf != null) {
        kmf.init(ks, ks_pass);
        km = kmf.getKeyManagers();
      }

      ctx_ssl.init(km, tm, new java.security.SecureRandom());
      return (SSLSocketFactory) ctx_ssl.getSocketFactory();
    } catch (java.security.NoSuchAlgorithmException e) {
      throw new NoSuchAlgorithmException(e.getMessage());
    } catch (java.security.GeneralSecurityException e) {
      throw new CryptoException(e.getMessage());
    } catch (java.io.IOException e) {
      throw new IOException(e.getMessage());
    }
  }

  private static X509Certificate findIssuer(X509Certificate cert, Vector all, Vector chain) {
    for (int i = all.size() - 1; i >= 0; i--) {
      java.security.cert.X509Certificate issuerCert = (java.security.cert.X509Certificate) all.items[i];
      java.security.PublicKey publickey = issuerCert.getPublicKey();
      try {
        cert.verify(publickey);
        //            System.out.println("Subject: " + subject);
        //            System.out.println("Issuer: " + issuer);
        chain.addElement(cert);
        return issuerCert;
      } catch (java.security.GeneralSecurityException e) {
        // keep searching
      }
    }
    return null;
  }

  private static Certificate[] buildChain(PrivateKey pkey, Vector cacerts, Vector certs)
      throws NoSuchAlgorithmException, CryptoException {
    Vector chain = new Vector();

    X509Certificate cert = null;

    try {
      Signature sigInst = Signature.getInstance("MD5withRSA");
      for (int i = certs.size() - 1; i >= 0; i--) {
        sigInst.initSign(pkey);
        sigInst.update(toSign.getBytes());
        byte sign[] = sigInst.sign();
        sigInst.initVerify((Certificate) certs.items[i]);
        sigInst.update(toSign.getBytes());
        if (sigInst.verify(sign)) {
          cert = (X509Certificate) certs.items[i];
          break;
        }
      }
    } catch (java.security.NoSuchAlgorithmException e) {
      throw new NoSuchAlgorithmException(e.getMessage());
    } catch (java.security.GeneralSecurityException e) {
      throw new CryptoException(e.getMessage());
    }

    X509Certificate issuerCert;
    do {
      issuerCert = null;

      java.security.Principal subject = cert.getSubjectDN();
      java.security.Principal issuer = cert.getIssuerDN();
      //         System.out.println("Subject: " + subject);
      //         System.out.println("Issuer: " + issuer);

      if (subject.equals(issuer)) {
        chain.addElement(cert);
        Certificate[] ch = new Certificate[chain.size()];
        chain.copyInto(ch);
        return ch;
      }
      issuerCert = findIssuer(cert, certs, chain);
      if (issuerCert == null) {
        issuerCert = findIssuer(cert, cacerts, chain);
      }
      cert = issuerCert;
    } while (issuerCert != null);
    return null;
  }

  @Override
  protected final void finalize() //flsobral@tc114_36: finalize support.
  {
    try {
      if (dontFinalize != true) {
        dispose();
      }
    } catch (Throwable t) {
    }
  }
}
