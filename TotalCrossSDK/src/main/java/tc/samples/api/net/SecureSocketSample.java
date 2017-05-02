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



package tc.samples.api.net;

import tc.samples.api.*;

import totalcross.crypto.*;
import totalcross.io.*;
import totalcross.net.*;
import totalcross.net.ssl.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;

/** ATTENTION: THE SSL IMPLEMENTATION IS BUGGY; USE IT AT YOUR OWN RISK! */

public class SecureSocketSample extends BaseContainer
{
   Button btnOpen, btnAction, btnClear;
   ComboBox cb, auth_cb;
   Edit edA;
   Socket socket;
   int auth_mode = NO_AUTH;

   byte[] session_id;
   SSLClient ssl_ctx;
   SSL ssl;

   final static int SERVER_AUTH = 0;
   final static int CLIENT_AUTH = 1;
   final static int NO_AUTH     = 2;

   public void initUI()
   {
      super.initUI();
      new MessageBox("Attention","The SSL cerfiticate used by this sample is no longer valid, but you can see the sources and use with a valid certificate you may have.").popup();
      add(new Label("Address: "),LEFT+2,TOP+1);
      add(edA = new Edit(""),AFTER+3,SAME);
      edA.setText("https://webmail.grad.inf.puc-rio.br");
      add(cb = new ComboBox(
            new String[] {
                  "View CA X509 certificate",
                  "View my X509 certificate",
                  "View my private key",
            }),LEFT+2,AFTER+3);
      add(btnAction = new Button("Show"),AFTER+3,SAME);
      add(btnClear = new Button("Clear"),LEFT+2,AFTER+5);
      add(btnOpen = new Button("Http/get"),AFTER+5,SAME);
      add(auth_cb = new ComboBox(
            new String[] {
                  "Svr auth",
                  "Svr+Clt auth",
                  "No auth",
            }),RIGHT-2,SAME);
      addLog(LEFT,AFTER+3,FILL,FILL,null);
      cb.setSelectedIndex(0);
      auth_cb.setSelectedIndex(auth_mode);

      log("scrolling log display area");
      log(" -port 7000: server auth only");
      log(" -port 7001: server + client auth");
      log("");

      displaySettings();
   }

   private void dispose_ssl() throws IOException
   {
      if (ssl != null)
      {
         ssl.dispose();
         ssl = null;
      }
      if (ssl_ctx != null)
      {
         ssl_ctx.dispose();
         ssl = null;
      }
   }

   public void onRemove()
   {
      try
      {
         dispose_ssl();
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }

   private void displaySettings()
   {
      log("LteSSL version : " + SSLUtil.version());
      log("max CA authorities : " + SSLUtil.maxCACerts());
      log("max CERTIFICATES : " + SSLUtil.maxCerts());
      log("");
   }

   private void display(String title, String s)
   {
      log("**** " + title + "****");
      int startline = 0;
      do
      {
         int endline = s.indexOf('\n', startline);
         String str = (endline > 0) ? s.substring(startline, endline) : s.substring(startline);
         log(str);
         startline += str.length() + 1;
      }
      while(startline < s.length());
   }

   private String toHex(byte[] bytes)
   {
      String hex = "";
      for (int i = 0; i < bytes.length; i++)
      {
         hex = hex.concat("0x" + Convert.unsigned2hex(bytes[i], 2) + ",");
         if ((i+1) % 8 == 0) hex = hex.concat("\n");
      }
      return hex;
   }

   public void onEvent(Event e)
   {
      if (e.type == ControlEvent.PRESSED)
      {
         if (e.target == btnOpen)
         {
            try
            {
               httpsGetLine();
            }
            catch (NoSuchAlgorithmException e1)
            {
               totalcross.ui.dialog.MessageBox.showException(e1, true);
            }
            catch (CryptoException e1)
            {
               totalcross.ui.dialog.MessageBox.showException(e1, true);
            }
            catch (IOException e1)
            {
               totalcross.ui.dialog.MessageBox.showException(e1, true);
            }
         }
         if (e.target == btnClear)
         {
            lblog.removeAll();
            lblog.repaintNow();
         }
         else if (e.target == btnAction)
         {
            switch (cb.getSelectedIndex())
            {
            case 0:
               display(cb.getSelectedItem().toString(), X509CACert);
               break;
            case 1:
               display(cb.getSelectedItem().toString(), X509Cert);
               break;
            case 2:
               display(cb.getSelectedItem().toString(), toHex(PKCS8PrivateKey));
               break;
            }
         }
      }
   }

   private void httpsGetLine() throws NoSuchAlgorithmException, CryptoException, IOException
   {
      repaintNow(); // release the button

      String scheme = null;
      String host = null;
      int port = 0;
      String path = null;
      int ret;

      String url = edA.getText();

      int idx = url.indexOf("://");
      if (idx > 0)
      {
         scheme = url.substring(0, idx);
         if (scheme.equalsIgnoreCase("http")) port = 80;
         else if (scheme.equalsIgnoreCase("https")) port = 443;
         else
         {
            log("scheme '"+scheme+"' is not supported,");
            log("use 'http' or 'https'.");
            return;
         }
         url = url.substring(idx+3); // remove scheme +'://'
      }
      idx = url.indexOf(":"); // any port ? (user info are not supported!)
      if (idx > 0)
      {
         host = url.substring(0, idx);
         int slash = url.indexOf('/', idx+1);
         try
         {
            if (slash > 0)
            {
               port = Convert.toInt(url.substring(idx+1, slash));
               path = url.substring(slash);
            }
            else port = Convert.toInt(url.substring(idx+1));
         } catch (InvalidNumberException ine) {log("Invalid port number"); return;}
      }
      else
      {
         int slash = url.indexOf('/', idx+1);
         if (slash > 0)
         {
            host = url.substring(0, slash);
            path = url.substring(slash);
         }
         else
         {
            host = url;
         }
      }

      log("scheme : " + scheme);
      log("host   : " + host);
      log("port   : " + port);
      log("path   : " + path);

      if (host == null || host.length() == 0 || port == 0)
      {
         log("This URL is invalid!");
         return;
      }

      if (ssl_ctx == null || auth_mode != auth_cb.getSelectedIndex())
      {
         auth_mode = auth_cb.getSelectedIndex();

         if (ssl_ctx != null)
            dispose_ssl();

         log("version: " + SSLUtil.version());

         int mode = (auth_mode == NO_AUTH) ? Constants.SSL_SERVER_VERIFY_LATER:0;
         ssl_ctx = new SSLClient(mode, Constants.SSL_DEFAULT_CLNT_SESS);

         if (ssl_ctx == null)
         {
            log("Failed to create SSL client context");
            return;
         }

         if (auth_mode != NO_AUTH)
         {
            /* load the CA authorithy */
            ret = ssl_ctx.objLoad(Constants.SSL_OBJ_X509_CACERT, X509CACert.getBytes(), X509CACert.getBytes().length, null);
            if (ret != Constants.SSL_OK)
            {
               log("Err=" + ret + ":failed to load trusted CA");
               return;
            }
            else log("X509 trusted CA loaded");
   
            /* If the server requests client authentication, we have to send our client certificate or the SSL handshake will fail. */
            if (auth_mode == CLIENT_AUTH)
            {
               ret = ssl_ctx.objLoad(Constants.SSL_OBJ_PKCS8, PKCS8PrivateKey, PKCS8PrivateKey.length, null);
               if (ret != Constants.SSL_OK)
               {
                  log("Err=" + ret + ":failed to load private key");
                  return;
               }
               else log("Client private key loaded");
   
               ret = ssl_ctx.objLoad(Constants.SSL_OBJ_X509_CERT, X509Cert.getBytes(), X509Cert.getBytes().length, null);
               if (ret != Constants.SSL_OK)
               {
                  log("Err=" + ret + ":failed to load certificate");
                  return;
               }
               else log("X509 client certificate loaded");
            }
         }
      }

      try
      {
         socket = new Socket(host, port, 25000);
         socket.readTimeout = 2500;

         log("Opening connection...");
         ssl = ssl_ctx.connect(socket, session_id);
         if (ssl == null)
         {
            log("Failed to connect to secured socket");
            return;
         }

         log("Socket opened");
         int start = Vm.getTimeStamp();

         int hs;
         while ((hs = ssl.handshakeStatus()) == Constants.SSL_HANDSHAKE_IN_PROGRESS)
         {
            Vm.sleep(25);
            if (Vm.getTimeStamp() - start > 5000) break;
         }
         if (hs != Constants.SSL_OK)
         {
            log("Err=" + hs + ":SSL handshake failed");
            ssl.dispose();
            ssl = null;
            return;
         }
         else log("SSL handshake succeeded");

         display("SSL Peer information", "Host:" + host);
         log("Cert DN: " + ssl.getCertificateDN(Constants.SSL_X509_CERT_COMMON_NAME));
         log("Cert O: " + ssl.getCertificateDN(Constants.SSL_X509_CERT_ORGANIZATION));
         log("Cert OU: " + ssl.getCertificateDN(Constants.SSL_X509_CERT_ORGANIZATIONAL_NAME));
         log("CA DN: " + ssl.getCertificateDN(Constants.SSL_X509_CA_CERT_COMMON_NAME));
         log("CA O: " + ssl.getCertificateDN(Constants.SSL_X509_CA_CERT_ORGANIZATION));
         log("CA OU: " + ssl.getCertificateDN(Constants.SSL_X509_CA_CERT_ORGANIZATIONAL_NAME));
         log("session ID: " + hexString(ssl.getSessionId()));
         log("Cypher ID: " + (int)ssl.getCipherId());

         if (auth_mode == NO_AUTH)
         {
            ret = ssl.verifyCertificate();
	         switch (ret)
	         {
            case Constants.SSL_OK:
               log("Verify: trusted");
               break;
            case Constants.X509_VFY_ERROR_NO_TRUSTED_CERT:
               log("Verify: not trusted");
               break;
            default:
               log("Verify result " + ret);
               break;
	         }
         }
         
         String request_string =
            "GET " + ((path == null || path.length() == 0) ? "/" : path) + " HTTP/1.0" + Convert.CRLF +
            "User-Agent: LiteSSL/1.0" + Convert.CRLF +
            "Host: " + host + Convert.CRLF +
            "Accept: */*" + Convert.CRLF +
            "Connection: keep-alive" + Convert.CRLF +
            Convert.CRLF;
         Vm.debug("HTTP request: " + request_string);

         ret = ssl.write(request_string.getBytes());
         if (ret < Constants.SSL_OK)
         {
            log("Err= " + ret + ":SSL write error");
         }
         else
         {
            log("HTTP request sent");

            /* now read (and display) whatever the client sends us */
            SSLReadHolder rh = new SSLReadHolder();

            boolean finished = false;
            do
            {
               /* keep reading until we get something interesting */
               while ((ret = ssl.read(rh)) == Constants.SSL_OK)
               {
                  Vm.debug("wait read completion...");
                  Vm.sleep(200);
               }
               if (ret > 0)
               {
                  String content = new String(rh.getData(), 0, rh.getData().length);
                  Vm.debug("HTTP content: " + content);
                  if (content.indexOf('\n') >= 0)
                  {
                     // stop at first newline
                     content = content.substring(0, content.indexOf('\n'));
                     finished = true;
                  }
                  display("HTTP result", content);
               }
               else if (ret < Constants.SSL_OK)
                  log("Err=" + ret + ":SSL read error");
            }
            while (!finished && ret >= Constants.SSL_OK);
         }

         ssl.dispose();
         ssl = null;
      }
      catch (totalcross.net.UnknownHostException e)
      {
         log("UnknownHostException on Socket creation: " + e.getMessage());
      }
      catch (IOException e)
      {
         log("IOException on Socket creation: " + e.getMessage());
      }

      if (socket != null)
      {
         try
         {
            socket.close();
            log("Socket closed");
         }
         catch (IOException e)
         {
            log("IOException on Socket close: " + e.getMessage());
         }
      }
   }

   private static String hexString(byte[] buffer)
   {
      if (buffer == null)
         return null;

      String s = "";
      for (int i = 0; i < buffer.length; i++)
         s = s.concat(Convert.unsigned2hex(buffer[i], 2));
      return s;
   }

   // note: this certificate was issued by SuperWaba to work with a https server that
   // is NO LONGER AVAILABLE. It is still here just for an example.
   private final static String X509CACert =
      "Certificate:"+Convert.CRLF+
      "    Data:"+Convert.CRLF+
      "        Version: 3 (0x2)"+Convert.CRLF+
      "        Serial Number:"+Convert.CRLF+
      "            c6:ef:2f:bb:65:9f:8b:46"+Convert.CRLF+
      "        Signature Algorithm: sha1WithRSAEncryption"+Convert.CRLF+
      "        Issuer: C=BR, ST=Rio de Janeiro state, L=Rio de Janeiro, O=SuperWaba Ltda, OU=SuperWaba dev. department, CN=SuperWaba Sample CA/emailAddress=guich@superwaba.com.br"+Convert.CRLF+
      "        Validity"+Convert.CRLF+
      "            Not Before: Jul 17 18:00:27 2007 GMT"+Convert.CRLF+
      "            Not After : Jul 14 18:00:27 2017 GMT"+Convert.CRLF+
      "        Subject: C=BR, ST=Rio de Janeiro state, L=Rio de Janeiro, O=SuperWaba Ltda, OU=SuperWaba dev. department, CN=SuperWaba Sample CA/emailAddress=guich@superwaba.com.br"+Convert.CRLF+
      "        Subject Public Key Info:"+Convert.CRLF+
      "            Public Key Algorithm: rsaEncryption"+Convert.CRLF+
      "            RSA Public Key: (1024 bit)"+Convert.CRLF+
      "                Modulus (1024 bit):"+Convert.CRLF+
      "                    00:e0:2d:bb:f1:0d:8f:7f:bd:f1:8f:bd:ad:5a:21:"+Convert.CRLF+
      "                    33:3d:a4:dd:6e:f3:04:e2:a6:d9:4d:f6:41:e2:63:"+Convert.CRLF+
      "                    b0:42:7a:2f:3a:de:47:0a:95:de:6d:bb:d8:76:54:"+Convert.CRLF+
      "                    70:73:e2:49:e8:be:7f:68:4f:b2:8e:69:3a:d8:d9:"+Convert.CRLF+
      "                    d2:d9:7c:15:22:19:68:14:4e:90:f9:43:e5:f1:96:"+Convert.CRLF+
      "                    6e:a5:2f:e8:6b:81:9f:a0:1e:4a:08:73:87:c0:f2:"+Convert.CRLF+
      "                    fb:23:b8:c6:fb:f1:92:94:f8:79:c5:3d:08:cc:ac:"+Convert.CRLF+
      "                    5b:d6:0f:77:cd:f9:79:5f:6b:c1:1b:6e:85:5b:0c:"+Convert.CRLF+
      "                    7d:38:d1:c0:e3:94:c5:b8:5f"+Convert.CRLF+
      "                Exponent: 65537 (0x10001)"+Convert.CRLF+
      "        X509v3 extensions:"+Convert.CRLF+
      "            X509v3 Subject Key Identifier: "+Convert.CRLF+
      "                08:71:24:91:C1:7A:6D:07:A4:89:41:9B:EE:A3:BE:54:85:B2:86:82"+Convert.CRLF+
      "            X509v3 Authority Key Identifier: "+Convert.CRLF+
      "                keyid:08:71:24:91:C1:7A:6D:07:A4:89:41:9B:EE:A3:BE:54:85:B2:86:82"+Convert.CRLF+
      "                DirName:/C=BR/ST=Rio de Janeiro state/L=Rio de Janeiro/O=SuperWaba Ltda/OU=SuperWaba dev. department/CN=SuperWaba Sample CA/emailAddress=guich@superwaba.com.br"+Convert.CRLF+
      "                serial:C6:EF:2F:BB:65:9F:8B:46"+Convert.CRLF+
      "            X509v3 Basic Constraints: "+Convert.CRLF+
      "                CA:TRUE"+Convert.CRLF+
      "    Signature Algorithm: sha1WithRSAEncryption"+Convert.CRLF+
      "        16:45:62:6e:13:ed:2d:4d:42:ad:46:52:82:54:10:5d:2f:30:"+Convert.CRLF+
      "        bd:f1:43:3f:76:da:80:fb:24:5c:5b:ec:96:3d:00:68:98:15:"+Convert.CRLF+
      "        71:e0:a3:f0:92:f3:1d:49:a2:a2:3f:9c:22:68:e0:22:0d:21:"+Convert.CRLF+
      "        c7:b2:12:c2:44:6d:66:60:78:25:98:61:83:6b:1b:57:7b:c5:"+Convert.CRLF+
      "        59:5a:4f:42:b0:74:75:62:00:c1:2b:62:0c:00:5f:04:a4:5a:"+Convert.CRLF+
      "        a3:df:c0:8d:07:b5:5e:83:9b:36:3d:83:ab:b2:8a:9b:64:7b:"+Convert.CRLF+
      "        de:b9:3a:89:66:18:e7:9e:8a:34:23:cb:be:f7:b6:7c:9f:d4:"+Convert.CRLF+
      "        30:fe"+Convert.CRLF+
      "-----BEGIN CERTIFICATE-----"+Convert.CRLF+
      "MIIEQDCCA6mgAwIBAgIJAMbvL7tln4tGMA0GCSqGSIb3DQEBBQUAMIHHMQswCQYD"+Convert.CRLF+
      "VQQGEwJCUjEdMBsGA1UECBMUUmlvIGRlIEphbmVpcm8gc3RhdGUxFzAVBgNVBAcT"+Convert.CRLF+
      "DlJpbyBkZSBKYW5laXJvMRcwFQYDVQQKEw5TdXBlcldhYmEgTHRkYTEiMCAGA1UE"+Convert.CRLF+
      "CxMZU3VwZXJXYWJhIGRldi4gZGVwYXJ0bWVudDEcMBoGA1UEAxMTU3VwZXJXYWJh"+Convert.CRLF+
      "IFNhbXBsZSBDQTElMCMGCSqGSIb3DQEJARYWZ3VpY2hAc3VwZXJ3YWJhLmNvbS5i"+Convert.CRLF+
      "cjAeFw0wNzA3MTcxODAwMjdaFw0xNzA3MTQxODAwMjdaMIHHMQswCQYDVQQGEwJC"+Convert.CRLF+
      "UjEdMBsGA1UECBMUUmlvIGRlIEphbmVpcm8gc3RhdGUxFzAVBgNVBAcTDlJpbyBk"+Convert.CRLF+
      "ZSBKYW5laXJvMRcwFQYDVQQKEw5TdXBlcldhYmEgTHRkYTEiMCAGA1UECxMZU3Vw"+Convert.CRLF+
      "ZXJXYWJhIGRldi4gZGVwYXJ0bWVudDEcMBoGA1UEAxMTU3VwZXJXYWJhIFNhbXBs"+Convert.CRLF+
      "ZSBDQTElMCMGCSqGSIb3DQEJARYWZ3VpY2hAc3VwZXJ3YWJhLmNvbS5icjCBnzAN"+Convert.CRLF+
      "BgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEA4C278Q2Pf73xj72tWiEzPaTdbvME4qbZ"+Convert.CRLF+
      "TfZB4mOwQnovOt5HCpXebbvYdlRwc+JJ6L5/aE+yjmk62NnS2XwVIhloFE6Q+UPl"+Convert.CRLF+
      "8ZZupS/oa4GfoB5KCHOHwPL7I7jG+/GSlPh5xT0IzKxb1g93zfl5X2vBG26FWwx9"+Convert.CRLF+
      "ONHA45TFuF8CAwEAAaOCATAwggEsMB0GA1UdDgQWBBQIcSSRwXptB6SJQZvuo75U"+Convert.CRLF+
      "hbKGgjCB/AYDVR0jBIH0MIHxgBQIcSSRwXptB6SJQZvuo75UhbKGgqGBzaSByjCB"+Convert.CRLF+
      "xzELMAkGA1UEBhMCQlIxHTAbBgNVBAgTFFJpbyBkZSBKYW5laXJvIHN0YXRlMRcw"+Convert.CRLF+
      "FQYDVQQHEw5SaW8gZGUgSmFuZWlybzEXMBUGA1UEChMOU3VwZXJXYWJhIEx0ZGEx"+Convert.CRLF+
      "IjAgBgNVBAsTGVN1cGVyV2FiYSBkZXYuIGRlcGFydG1lbnQxHDAaBgNVBAMTE1N1"+Convert.CRLF+
      "cGVyV2FiYSBTYW1wbGUgQ0ExJTAjBgkqhkiG9w0BCQEWFmd1aWNoQHN1cGVyd2Fi"+Convert.CRLF+
      "YS5jb20uYnKCCQDG7y+7ZZ+LRjAMBgNVHRMEBTADAQH/MA0GCSqGSIb3DQEBBQUA"+Convert.CRLF+
      "A4GBABZFYm4T7S1NQq1GUoJUEF0vML3xQz922oD7JFxb7JY9AGiYFXHgo/CS8x1J"+Convert.CRLF+
      "oqI/nCJo4CINIceyEsJEbWZgeCWYYYNrG1d7xVlaT0KwdHViAMErYgwAXwSkWqPf"+Convert.CRLF+
      "wI0HtV6DmzY9g6uyiptke965OolmGOeeijQjy773tnyf1DD+"+Convert.CRLF+
      "-----END CERTIFICATE-----"+Convert.CRLF;

   private final static String X509Cert =
      "Certificate:"+Convert.CRLF+
      "    Data:"+Convert.CRLF+
      "        Version: 1 (0x0)"+Convert.CRLF+
      "        Serial Number: 2 (0x2)"+Convert.CRLF+
      "        Signature Algorithm: sha1WithRSAEncryption"+Convert.CRLF+
      "        Issuer: C=BR, ST=Rio de Janeiro state, L=Rio de Janeiro, O=SuperWaba Ltda, OU=SuperWaba dev. department, CN=SuperWaba Sample CA/emailAddress=guich@superwaba.com.br"+Convert.CRLF+
      "        Validity"+Convert.CRLF+
      "            Not Before: Jul 17 18:22:43 2007 GMT"+Convert.CRLF+
      "            Not After : Jul 16 18:22:43 2016 GMT"+Convert.CRLF+
      "        Subject: C=FR, ST=Bas-Rhin, L=Illkirch-Graffenstaden, O=Internet Widgits Pty Ltd, CN=registered customer/emailAddress=customer@google.com"+Convert.CRLF+
      "        Subject Public Key Info:"+Convert.CRLF+
      "            Public Key Algorithm: rsaEncryption"+Convert.CRLF+
      "            RSA Public Key: (512 bit)"+Convert.CRLF+
      "                Modulus (512 bit):"+Convert.CRLF+
      "                    00:bc:bc:c1:be:d2:a6:57:bb:fe:84:bd:b4:ec:86:"+Convert.CRLF+
      "                    45:d4:65:06:e5:01:9a:d1:4f:32:ae:97:ac:a5:3e:"+Convert.CRLF+
      "                    63:39:a3:f3:21:97:9b:6f:a6:13:4e:4e:a4:19:d0:"+Convert.CRLF+
      "                    bf:24:1a:24:c8:f1:1b:2f:5f:75:3a:d1:e4:42:6b:"+Convert.CRLF+
      "                    90:f7:69:49:15"+Convert.CRLF+
      "                Exponent: 65537 (0x10001)"+Convert.CRLF+
      "    Signature Algorithm: sha1WithRSAEncryption"+Convert.CRLF+
      "        d4:95:45:ef:da:f7:81:de:b4:ca:79:63:cd:07:01:44:36:32:"+Convert.CRLF+
      "        a1:bc:2f:60:b4:7a:8c:7f:ec:cb:d7:bf:58:29:3b:1e:e4:69:"+Convert.CRLF+
      "        1f:44:a6:3c:77:61:a9:7a:8d:37:1f:3f:52:94:0a:28:7a:23:"+Convert.CRLF+
      "        f4:13:48:d1:dc:58:ee:76:8f:86:d8:99:c2:c5:75:94:59:5a:"+Convert.CRLF+
      "        eb:45:76:b5:9d:46:ad:88:c8:85:4e:b5:d8:91:0e:b5:34:33:"+Convert.CRLF+
      "        fe:87:7f:ac:59:db:06:8f:fe:89:e4:62:7e:7c:0b:31:b8:f7:"+Convert.CRLF+
      "        13:dc:c5:bb:78:a4:9d:6d:38:6b:5e:e0:ce:af:56:68:f4:9e:"+Convert.CRLF+
      "        6e:03"+Convert.CRLF+
      "-----BEGIN CERTIFICATE-----"+Convert.CRLF+
      "MIICmjCCAgMCAQIwDQYJKoZIhvcNAQEFBQAwgccxCzAJBgNVBAYTAkJSMR0wGwYD"+Convert.CRLF+
      "VQQIExRSaW8gZGUgSmFuZWlybyBzdGF0ZTEXMBUGA1UEBxMOUmlvIGRlIEphbmVp"+Convert.CRLF+
      "cm8xFzAVBgNVBAoTDlN1cGVyV2FiYSBMdGRhMSIwIAYDVQQLExlTdXBlcldhYmEg"+Convert.CRLF+
      "ZGV2LiBkZXBhcnRtZW50MRwwGgYDVQQDExNTdXBlcldhYmEgU2FtcGxlIENBMSUw"+Convert.CRLF+
      "IwYJKoZIhvcNAQkBFhZndWljaEBzdXBlcndhYmEuY29tLmJyMB4XDTA3MDcxNzE4"+Convert.CRLF+
      "MjI0M1oXDTExMDcxNjE4MjI0M1owgaYxCzAJBgNVBAYTAkZSMREwDwYDVQQIEwhC"+Convert.CRLF+
      "YXMtUmhpbjEfMB0GA1UEBxMWSWxsa2lyY2gtR3JhZmZlbnN0YWRlbjEhMB8GA1UE"+Convert.CRLF+
      "ChMYSW50ZXJuZXQgV2lkZ2l0cyBQdHkgTHRkMRwwGgYDVQQDExNyZWdpc3RlcmVk"+Convert.CRLF+
      "IGN1c3RvbWVyMSIwIAYJKoZIhvcNAQkBFhNjdXN0b21lckBnb29nbGUuY29tMFww"+Convert.CRLF+
      "DQYJKoZIhvcNAQEBBQADSwAwSAJBALy8wb7Sple7/oS9tOyGRdRlBuUBmtFPMq6X"+Convert.CRLF+
      "rKU+Yzmj8yGXm2+mE05OpBnQvyQaJMjxGy9fdTrR5EJrkPdpSRUCAwEAATANBgkq"+Convert.CRLF+
      "hkiG9w0BAQUFAAOBgQDUlUXv2veB3rTKeWPNBwFENjKhvC9gtHqMf+zL179YKTse"+Convert.CRLF+
      "5GkfRKY8d2Gpeo03Hz9SlAooeiP0E0jR3Fjudo+G2JnCxXWUWVrrRXa1nUatiMiF"+Convert.CRLF+
      "TrXYkQ61NDP+h3+sWdsGj/6J5GJ+fAsxuPcT3MW7eKSdbThrXuDOr1Zo9J5uAw=="+Convert.CRLF+
      "-----END CERTIFICATE-----"+Convert.CRLF;

   private final static byte PKCS8PrivateKey[] = {
      48,-126,1,86,2,1,0,48,13,6,9,42,-122,72,-122,-9,
      13,1,1,1,5,0,4,-126,1,64,48,-126,1,60,2,1,
      0,2,65,0,-68,-68,-63,-66,-46,-90,87,-69,-2,-124,-67,-76,
      -20,-122,69,-44,101,6,-27,1,-102,-47,79,50,-82,-105,-84,-91,
      62,99,57,-93,-13,33,-105,-101,111,-90,19,78,78,-92,25,-48,
      -65,36,26,36,-56,-15,27,47,95,117,58,-47,-28,66,107,-112,
      -9,105,73,21,2,3,1,0,1,2,65,0,-82,99,17,-7,
      73,21,-99,118,-4,-126,-107,-18,119,-92,-47,28,-96,-124,48,80,
      -67,69,-84,-111,-67,-76,51,55,51,95,-25,62,-75,-70,-80,22,
      22,2,-27,-82,-27,39,-89,-59,-9,45,-122,74,-31,50,-128,-115,
      39,11,-115,-2,54,44,-9,69,-58,-58,6,-71,2,33,0,-7,
      -36,-11,25,-36,24,-77,-15,47,37,75,37,99,-89,80,-57,-120,
      51,63,37,122,-53,-102,-62,-55,-6,-40,10,-108,-83,-116,11,2,
      33,0,-63,95,118,-48,-123,-45,63,-125,127,119,-3,10,37,50,
      -57,-89,-109,-86,62,-113,-110,-105,-61,105,83,-5,86,102,17,-56,
      -109,95,2,32,82,-117,-89,-63,-110,79,-22,-22,-116,11,-61,6,
      -88,36,119,112,99,-45,-94,81,-20,126,101,51,98,-20,-25,0,
      -97,-37,-79,-65,2,33,0,-101,56,3,-23,113,-30,-107,-14,-7,
      -40,-40,-42,-47,-15,38,-18,78,-53,-77,24,15,-11,-98,55,-25,
      -33,-78,4,-112,117,-50,69,2,33,0,-113,-82,123,70,26,22,
      67,-49,100,-17,-66,30,-53,-82,97,42,8,-97,99,-64,-97,-20,
      -123,-40,-7,-81,-36,83,73,92,-31,69
   };
}
