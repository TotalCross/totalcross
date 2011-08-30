/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.SecurityInfo;
import javax.microedition.io.StreamConnection;
import javax.microedition.pki.Certificate;

import net.rim.device.api.crypto.asn1.ASN1EncodingException;
import net.rim.device.api.crypto.certificate.x509.X509DistinguishedName;
import net.rim.device.api.crypto.tls.AlertProtocol;
import net.rim.device.api.crypto.tls.TLSAlertException;
import net.rim.device.api.crypto.tls.ssl30.SSL30Connection;
import net.rim.device.cldc.io.ssl.TLSException;
import net.rim.device.cldc.io.ssl.TLSIOException;
import totalcross.Launcher4B;
import totalcross.crypto.*;
import totalcross.io.*;
import totalcross.net.Socket;
import totalcross.net.Socket4B;
import totalcross.net.SocketTimeoutException;
import totalcross.util.Hashtable;
import totalcross.util.Logger;

public class SSL4B
{
   Object ssl;
   Socket socket;
   int status = Constants.SSL_NOT_OK;
   Exception lastException;

   private Thread opener;
   private SSL30Connection conn;
   private InputStream is;
   private OutputStream os;
   private SecurityInfo info;
   private Certificate cert;
   
   static private Hashtable cache;
   private static Logger logger = Logger.getLogger("totalcross.net");
   
   static void cachePutSSL(Socket s, SSL4B ssl)
   {
      if (cache == null)
         cache = new Hashtable(17);
      
      if (ssl == null)
         cache.remove(s);
      else
         cache.put(s, ssl);
   }
   static SSL4B cacheGetSSL(Socket s)
   {
      return (cache == null) ? null : (SSL4B)cache.get(s);
   }

   protected SSL4B(Object ssl, Socket socket)
   {
      this.socket = socket;
      cachePutSSL(socket, this);

      open();
   }

   public final int renegotiate() throws IOException
   {
      if (ssl == null)
         return Constants.SSL_NOT_OK;

      close(true);
      open();

      return Constants.SSL_OK;
   }

   public final void dispose() throws IOException
   {
      if (ssl != null)
      {
         close(true);
         cachePutSSL(socket, null);
      }
   }

   public final int handshakeStatus()
   {
      if (ssl == null && status != Constants.SSL_HANDSHAKE_IN_PROGRESS)
         return Constants.SSL_NOT_OK;

      return status;
   }

   public final byte getCipherId()
   {
      if (ssl != null)
      {
         try
         {
            String cs = ((SSL30Connection)ssl).getSecurityInfo().getCipherSuite();
            if (cs.equals("TLS_RSA_WITH_AES_128_CBC_SHA"))
               return Constants.TLS_RSA_WITH_AES_128_CBC_SHA;
            else if (cs.equals("TLS_RSA_WITH_AES_256_CBC_SHA"))
               return Constants.TLS_RSA_WITH_AES_256_CBC_SHA;
            else if (cs.equals("TLS_RSA_WITH_RC4_128_SHA"))
               return Constants.TLS_RSA_WITH_RC4_128_SHA;
            else if (cs.equals("TLS_RSA_WITH_RC4_128_MD5"))
               return Constants.TLS_RSA_WITH_RC4_128_MD5;
         }
         catch (java.io.IOException ex)
         {
            lastException = ex;
            logger.throwing("SSL", "getCipherId", ex);
         }
      }

      return -1;
   }

   public final byte[] getSessionId()
   {
      return null;
   }

   public final String getCertificateDN(int component) throws CryptoException
   {
      if (ssl != null)
      {
         try
         {
            switch (component)
            {
               case Constants.SSL_X509_CA_CERT_COMMON_NAME:
                  return cert.getIssuer();
               case Constants.SSL_X509_CA_CERT_ORGANIZATION:
                  return null;
               case Constants.SSL_X509_CA_CERT_ORGANIZATIONAL_NAME:
                  return null;
               case Constants.SSL_X509_CERT_COMMON_NAME:
                  return new X509DistinguishedName(cert.getSubject(), ';').getCommonName();
               case Constants.SSL_X509_CERT_ORGANIZATION:
                  return new X509DistinguishedName(cert.getSubject(), ';').getOrganization();
               case Constants.SSL_X509_CERT_ORGANIZATIONAL_NAME:
                  return new X509DistinguishedName(cert.getSubject(), ';').getOrganizationalUnit();
            }
         }
         catch (ASN1EncodingException ex)
         {
            throw new CryptoException(ex.getMessage());
         }
      }

      return null;
   }

   public final int read(SSLReadHolder rh) throws SocketTimeoutException, IOException
   {
      int res = -1;
      
      if (ssl != null)
      {
         try
         {
            rh.m_buf = Launcher4B.readNonBlocking(is, socket.readTimeout);
            if (rh.m_buf != null)
               res = rh.m_buf.length;
         }
         catch (TLSIOException ex) // TLS failure
         {
            lastException = ex;
            logger.throwing("SSL", "read", ex);
            if (ex.getException() instanceof TLSAlertException)
            {
               TLSAlertException alert = (TLSAlertException)ex.getException();
               logger.warning("TLSAlertException (" + getTLSAlertLevel(alert) + "): " + getTLSAlertDescription(alert));
               
               byte level = alert.getAlertLevel();
               if (level == AlertProtocol.CRITICAL || level == AlertProtocol.FATAL)
                  res = Constants.SSL_ERROR_DEAD;
            }
            else
               res = Constants.SSL_ERROR_DEAD;
         }
         catch (java.io.IOException ex) // other IO failure
         {
            throw new IOException(ex.getMessage());
         }
      }

      logger.finest("SSL.read returning " + res + " bytes read");
      return res;
   }

   public final int write(byte[] out_data) throws IOException
   {
      return write(out_data, out_data.length);
   }

   public final int write(byte[] out_data, int out_len) throws IOException
   {
      int res = -1;
      
      if (ssl != null)
      {
         try
         {
            os.write(out_data, 0, out_len);
            os.flush();
            
            res = out_len;
         }
         catch (TLSIOException ex) // TLS failure
         {
            lastException = ex;
            logger.throwing("SSL", "write", ex);
            if (ex.getException() instanceof TLSAlertException)
            {
               TLSAlertException alert = (TLSAlertException)ex.getException();
               logger.warning("TLSAlertException (" + getTLSAlertLevel(alert) + "): " + getTLSAlertDescription(alert));
               
               byte level = alert.getAlertLevel();
               if (level == AlertProtocol.CRITICAL || level == AlertProtocol.FATAL)
                  res = Constants.SSL_ERROR_DEAD;
            }
         }
         catch (java.io.IOException ex)
         {
            throw new IOException(ex.getMessage());
         }
      }

      logger.finest("SSL.write returning " + res + " bytes written (requested " + out_len + " bytes)");
      return res;
   }

   public final int verifyCertificate() throws CryptoException
   {
      return Constants.SSL_ERROR_NOT_SUPPORTED;
   }
   
   public final Exception getLastException()
   {
      return lastException;
   }

   private void open()
   {
      status = Constants.SSL_HANDSHAKE_IN_PROGRESS;
      opener = new Thread()
      {
         public void run()
         {
            Socket4B nativeSocket = (Socket4B)(Object)socket;
            int rt = nativeSocket.readTimeout;
            int wt = nativeSocket.writeTimeout;
            nativeSocket.readTimeout = nativeSocket.writeTimeout = nativeSocket.openTimeout;
            
            try
            {
               ssl = conn = new SSL30Connection(new SSLSocket(nativeSocket), nativeSocket.url, true);
               is = conn.openInputStream();
               os = conn.openOutputStream();
               
               info = conn.getSecurityInfo();
               cert = info.getServerCertificate();
               
               status = Constants.SSL_OK;
            }
            catch (TLSException ex)
            {
               lastException = ex;
               logger.throwing("SSL", "open", ex);
               
               if (ex.getException() instanceof TLSAlertException)
               {
                  TLSAlertException alert = (TLSAlertException)ex.getException();
                  logger.warning("TLSAlertException (" + getTLSAlertLevel(alert) + "): " + getTLSAlertDescription(alert));
                  
                  switch (alert.getAlertDescription())
                  {
                     case AlertProtocol.BAD_CERTIFICATE:
                        status = Constants.SSL_ALERT_BAD_CERTIFICATE;
                        break;
                     case AlertProtocol.BAD_RECORD_MAC:
                        status = Constants.SSL_ALERT_BAD_RECORD_MAC;
                        break;
                     case AlertProtocol.CLOSE_NOTIFY:
                        status = Constants.SSL_ALERT_CLOSE_NOTIFY;
                        break;
                     case AlertProtocol.DECODE_ERROR:
                        status = Constants.SSL_ALERT_DECODE_ERROR;
                        break;
                     case AlertProtocol.DECRYPT_ERROR:
                        status = Constants.SSL_ALERT_DECRYPT_ERROR;
                        break;
                     case AlertProtocol.HANDSHAKE_FAILURE:
                        status = Constants.SSL_ALERT_HANDSHAKE_FAILURE;
                        break;
                     case AlertProtocol.ILLEGAL_PARAMETER:
                        status = Constants.SSL_ALERT_ILLEGAL_PARAMETER;
                        break;
                     case AlertProtocol.PROTOCOL_VERSION:
                        status = Constants.SSL_ALERT_INVALID_VERSION;
                        break;
                     case AlertProtocol.UNEXPECTED_MESSAGE:
                        status = Constants.SSL_ALERT_UNEXPECTED_MESSAGE;
                        break;
                     default:
                        status = Constants.SSL_NOT_OK;
                        break;
                  }
               }
               else // not a TLSException
                  status = Constants.SSL_NOT_OK;
            }
            catch (java.io.IOException ex)
            {
               lastException = ex;
               logger.throwing("SSL", "open", ex);
               status = Constants.SSL_NOT_OK;
            }
            finally
            {
               nativeSocket.readTimeout = rt;
               nativeSocket.writeTimeout = wt;
            }

            if (status != Constants.SSL_OK)
            {
               try
               {
                  close(false);
               }
               catch (IOException ex)
               {
                  lastException = ex;
                  logger.throwing("SSL", "open", ex);
                  status = Constants.SSL_NOT_OK;
               }
            }
         }
      };
      opener.start();
   }

   private void close(boolean logExceptions) throws IOException
   {
      if (opener.isAlive() && opener != Thread.currentThread()) // Interrupt opener thread, if still running
      {
         logger.info("Close called during handshake; interrupting opener thread...");
         opener.interrupt();
         try
         {
            opener.join();
         }
         catch (InterruptedException ex) {}
      }
      
      if (ssl != null)
      {
         ssl = null;
         
         try
         {
            is.close();
            os.close();
            conn.close();
         }
         catch (java.io.IOException ex)
         {
            throw new IOException(ex.getMessage());
         }
      }
   }
   
   private String getTLSAlertLevel(TLSAlertException ex)
   {
      switch (ex.getAlertLevel())
      {
         case AlertProtocol.WARNING:
            return "WARNING";
         case AlertProtocol.FATAL:
            return "FATAL";
         case AlertProtocol.CRITICAL:
            return "CRITICAL";
         default:
            return "UNKNOWN_LEVEL";
      }
   }
   
   private String getTLSAlertDescription(TLSAlertException ex)
   {
      switch (ex.getAlertDescription())
      {
         case AlertProtocol.ACCESS_DENIED:
            return "ACCESS_DENIED";
         case AlertProtocol.BAD_CERTIFICATE:
            return "BAD_CERTIFICATE";
         case AlertProtocol.BAD_RECORD_MAC:
            return "BAD_RECORD_MAC";
         case AlertProtocol.CERTIFICATE_EXPIRED:
            return "CERTIFICATE_EXPIRED";
         case AlertProtocol.CERTIFICATE_REVOKED:
            return "CERTIFICATE_REVOKED";
         case AlertProtocol.CERTIFICATE_UNKNOWN:
            return "CERTIFICATE_UNKNOWN";
         case AlertProtocol.CLOSE_NOTIFY:
            return "CLOSE_NOTIFY";
         case AlertProtocol.DECODE_ERROR:
            return "DECODE_ERROR";
         case AlertProtocol.DECOMPRESSION_FAILURE:
            return "DECOMPRESSION_FAILURE";
         case AlertProtocol.DECRYPT_ERROR:
            return "DECRYPT_ERROR";
         case AlertProtocol.DECRYPTION_FAILED:
            return "DECRYPTION_FAILED";
         case AlertProtocol.DISABLED_KEY_ID:
            return "DISABLED_KEY_ID";
         case AlertProtocol.DUPLICATE_FINISHED_RECEIVED:
            return "DUPLICATE_FINISHED_RECEIVED";
         case AlertProtocol.EXPORT_RESTRICTION:
            return "EXPORT_RESTRICTION";
         case AlertProtocol.HANDSHAKE_FAILURE:
            return "HANDSHAKE_FAILURE";
         case AlertProtocol.ILLEGAL_PARAMETER:
            return "ILLEGAL_PARAMETER";
         case AlertProtocol.INSUFFICIENT_SECURITY:
            return "INSUFFICIENT_SECURITY";
         case AlertProtocol.INTERNAL_ERROR:
            return "INTERNAL_ERROR";
         case AlertProtocol.KEY_EXCHANGE_DISABLED:
            return "KEY_EXCHANGE_DISABLED";
         case AlertProtocol.NO_CERTIFICATE:
            return "NO_CERTIFICATE";
         case AlertProtocol.NO_CONNECTION:
            return "NO_CONNECTION";
         case AlertProtocol.NO_RENEGOTIATION:
            return "NO_CONNECTION";
         case AlertProtocol.PROTOCOL_VERSION:
            return "PROTOCOL_VERSION";
         case AlertProtocol.RECORD_OVERFLOW:
            return "RECORD_OVERFLOW";
         case AlertProtocol.SESSION_CLOSE_NOTIFY:
            return "SESSION_CLOSE_NOTIFY";
         case AlertProtocol.SESSION_NOT_READY:
            return "SESSION_NOT_READY";
         case AlertProtocol.TIME_REQUIRED:
            return "TIME_REQUIRED";
         case AlertProtocol.UNEXPECTED_MESSAGE:
            return "UNEXPECTED_MESSAGE";
         case AlertProtocol.UNKNOWN_CA:
            return "UNKNOWN_CA";
         case AlertProtocol.UNKNOWN_KEY_ID:
            return "UNKNOWN_KEY_ID";
         case AlertProtocol.UNKNOWN_PARAMETER_INDEX:
            return "UNKNOWN_PARAMETER_INDEX";
         case AlertProtocol.UNSUPPORTED_CERTIFICATE:
            return "UNSUPPORTED_CERTIFICATE";
         case AlertProtocol.USER_CANCELLED:
            return "USER_CANCELLED";
         default:
            return "UNKNOWN_DESCRIPTION";
      }
   }
   
   // This is an adapter class used to read and write data from the socket
   // in the SSL connection.
   private static class SSLSocket implements StreamConnection
   {
      private Socket4B socket;
      
      public SSLSocket(Socket4B socket)
      {
         this.socket = socket;
      }
      
      public DataInputStream openDataInputStream() throws java.io.IOException
      {
         return new DataInputStream(openInputStream());
      }

      public InputStream openInputStream() throws java.io.IOException
      {
         return new Launcher4B.S2IS(socket, -1, false);
      }

      public void close() throws java.io.IOException
      {
      }

      public DataOutputStream openDataOutputStream() throws java.io.IOException
      {
         return new DataOutputStream(openOutputStream());
      }

      public OutputStream openOutputStream() throws java.io.IOException
      {
         return new Launcher4B.S2OS(socket, false);
      }
   }
}
