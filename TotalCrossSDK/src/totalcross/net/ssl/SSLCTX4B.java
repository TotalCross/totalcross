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

import java.io.*;
import net.rim.device.api.applicationcontrol.*;
import net.rim.device.api.crypto.*;
import net.rim.device.api.crypto.certificate.*;
import net.rim.device.api.crypto.certificate.x509.*;
import net.rim.device.api.crypto.encoder.*;
import net.rim.device.api.crypto.keystore.*;
import net.rim.device.api.io.*;
import totalcross.*;
import totalcross.crypto.CryptoException;
import totalcross.crypto.NoSuchAlgorithmException;
import totalcross.io.*;
import totalcross.io.IOException;
import totalcross.net.*;
import totalcross.util.*;

public class SSLCTX4B
{
   int options;
   int num_sessions;

   private Vector caCerts = new Vector();
   private Vector certs = new Vector();
   private Vector keys = new Vector();
   private Vector data = new Vector();
   boolean dontFinalize;

   private static KeyStore ks;
   private static KeyStoreTicket ticket;
   private static Logger logger = Logger.getLogger("totalcross.net");
   public static final String toSign = "TotalCross";
   
   static
   {
      Launcher4B.requestAppPermission(ApplicationPermissions.PERMISSION_HANDHELD_KEYSTORE);
   }

   protected SSLCTX4B(int options, int num_sessions) throws NoSuchAlgorithmException
   {
      this.options = options;
      this.num_sessions = num_sessions;
   }

   public final SSL4B newClient(Socket socket, byte[] sessionId) throws IOException, NoSuchAlgorithmException, CryptoException
   {
      prepareSecurity();
      return new SSL4B(null, socket);
   }

   public final SSL4B newServer(Socket socket) throws IOException, NoSuchAlgorithmException, CryptoException
   {
      prepareSecurity();
      return new SSL4B(null, socket);
   }

   public final void dispose()
   {
      for (int i = data.size() - 1; i >= 0; i--)
      {
         try
         {
            ks.removeKey((KeyStoreData)data.items[i], ticket);
         }
         catch (Exception ex)
         {
            logger.throwing("SSLCTX", "dispose", ex);
         }
      }
      dontFinalize = true;
   }
   
   protected final void finalize()
   {
      dispose();
   }

   public final SSL4B find(Socket socket)
   {
      return SSL4B.cacheGetSSL(socket);
   }

   public final int objLoad(int objType, Stream material, String password) throws IOException, NoSuchAlgorithmException, CryptoException
   {
      byte[] data = Launcher4B.readStream(material);
      return objLoad(objType, data, data.length, password);
   }

   public final int objLoad(int objType, byte[] data, int len, String password) throws NoSuchAlgorithmException, CryptoException
   {
      switch (objType)
      {
         case Constants.SSL_OBJ_X509_CERT:
         case Constants.SSL_OBJ_X509_CACERT:
            return loadX509Cert(objType, data, len);
         case Constants.SSL_OBJ_PKCS8:
         case Constants.SSL_OBJ_PKCS12:
         case Constants.SSL_OBJ_RSA_KEY:
            return loadPrivateKey(objType, data, len);
         default:
            return Constants.SSL_NOT_OK;
      }
   }

   private int loadX509Cert(int objType, byte[] data, int len) throws NoSuchAlgorithmException, CryptoException
   {
      try
      {
         X509Certificate cert = (X509Certificate)CertificateFactory.getInstance("X509", getInputStream(objType, data, len));

         if (objType == Constants.SSL_OBJ_X509_CACERT)
            caCerts.addElement(cert);
         else
            certs.addElement(cert);

         return Constants.SSL_OK;
      }
      catch (net.rim.device.api.crypto.NoSuchAlgorithmException ex)
      {
         throw new NoSuchAlgorithmException(ex.getMessage());
      }
      catch (net.rim.device.api.crypto.certificate.CertificateParsingException ex)
      {
         throw new CryptoException(ex.getMessage());
      }
   }

   private int loadPrivateKey(int objType, byte[] data, int len)
   {
      try
      {
         PrivateKey key = null;

         if (objType == Constants.SSL_OBJ_PKCS8)
            key = (RSAPrivateKey)PrivateKeyDecoder.decode(new ByteArrayInputStream(data, 0, len), "PKCS8");

         if (key != null)
         {
            keys.addElement(key);
            return Constants.SSL_OK;
         }
         else
            return Constants.SSL_NOT_OK;
      }
      catch (net.rim.device.api.crypto.NoSuchAlgorithmException ex)
      {
         logger.throwing("SSLCTX", "loadRSAKey", ex);
         return Constants.SSL_ERROR_NO_CIPHER;
      }
      catch (InvalidKeyEncodingException ex)
      {
         logger.throwing("SSLCTX", "loadRSAKey", ex);
         return Constants.SSL_ERROR_INVALID_KEY;
      }
      catch (InvalidKeyException ex)
      {
         logger.throwing("SSLCTX", "loadRSAKey", ex);
         return Constants.SSL_ERROR_INVALID_KEY;
      }
      catch (Exception ex)
      {
         logger.throwing("SSLCTX", "loadRSAKey", ex);
         return Constants.SSL_NOT_OK;
      }
   }

   private InputStream getInputStream(int objType, byte[] data, int len)
   {
      String type = null;
      switch (objType)
      {
         case Constants.SSL_OBJ_X509_CACERT:
         case Constants.SSL_OBJ_X509_CERT:
            type = "CERTIFICATE";
            break;
      }

      if (type != null)
      {
         byte[] header = ("-----BEGIN " + type + "-----").getBytes();
         byte[] footer = ("-----END " + type + "-----").getBytes();

         int hOff = indexOf(data, 0, len, header);
         if (hOff >= 0)
         {
            hOff += header.length;
            int fOff = indexOf(data, hOff, len, footer);
            if (fOff >= 0)
               return new Base64InputStream(new ByteArrayInputStream(data, hOff, fOff - hOff));
         }
      }

      return new ByteArrayInputStream(data, 0, len);
   }

   private int indexOf(byte[] source, int off, int len, byte[] data)
   {
      int total = data.length;
      int j = 0;

      for (int i = off; (i + (total - j)) <= len; i++)
      {
         if (source[i] == data[j])
         {
            j++;
            if (j == total)
               return i - total + 1;
         }
         else if (j > 0)
            j = 0;
      }

      return -1;
   }

   private void prepareSecurity() throws IOException, NoSuchAlgorithmException, CryptoException
   {
      if (caCerts.size() > 0 || certs.size() > 0 || keys.size() > 0)
      {
         if (ks == null)
            ks = DeviceKeyStore.getInstance();
   
         try
         {
            if (ticket == null)
               ticket = ks.getTicket();
  
            if (ticket != null)
            {
               // CA Certificates
               for (int i = caCerts.size() - 1; i >= 0; i--)
               {
                  X509Certificate cert = (X509Certificate) caCerts.items[i];
                  data.addElement(ks.set(null, cert.getSubjectFriendlyName(), cert, cert.getStatus(), ticket));
               }
  
               // Certificates
               for (int i = certs.size() - 1; i >= 0; i--)
               {
                  X509Certificate cert = (X509Certificate) certs.items[i];
                  data.addElement(ks.set(null, cert.getSubjectFriendlyName(), cert, cert.getStatus(), ticket));
               }
  
               // Private Keys
               for (int i = keys.size() - 1; i >= 0; i--)
               {
                  PrivateKey key = (PrivateKey) keys.items[i];
                  data.addElement(ks.set(null, null, key, "PKCS8", KeyStore.SECURITY_LEVEL_LOW, ticket));
               }
            }
         }
         catch (net.rim.device.api.crypto.NoSuchAlgorithmException e)
         {
            throw new NoSuchAlgorithmException(e.getMessage());
         }
         catch (net.rim.device.api.crypto.CryptoException e)
         {
            throw new CryptoException(e.getMessage());
         }
      }
   }
}
