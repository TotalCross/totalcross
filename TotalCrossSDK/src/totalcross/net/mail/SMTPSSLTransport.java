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

package totalcross.net.mail;

import totalcross.crypto.CryptoException;
import totalcross.io.*;
import totalcross.net.Socket;
import totalcross.net.UnknownHostException;
import totalcross.net.ssl.*;
import totalcross.sys.Vm;

/**
 * This class implements the Transport abstract class using SMTP for message submission and transport.
 * 
 * @since TotalCross 1.13
 */
public class SMTPSSLTransport extends SMTPTransport
{
   protected SMTPSSLTransport(MailSession session)
   {
      super(session);
   }

   public void connect(Socket connection) throws MessagingException
   {
      try
      {
         this.connection = new SSLStream(connection);
         connectionReader = new LineReader(connection);
      }
      catch (IOException e)
      {
         throw new MessagingException(e.getMessage());
      }
      catch (CryptoException e)
      {
         throw new MessagingException(e.getMessage());
      }
   }

   class SSLStream extends Socket
   {
      private ByteArrayStream buffer = new ByteArrayStream(256);

      private SSLClient sslClient;
      private SSL sslConnection;
      private SSLReadHolder sslReader;

      SSLStream(Socket connection) throws MessagingException, IOException, CryptoException
      {
         sslClient = new SSLClient(Constants.SSL_SERVER_VERIFY_LATER, 0);
         sslConnection = sslClient.connect(connection, null);
         Exception e = sslConnection.getLastException();
         if (e != null)
            throw new MessagingException(e.getMessage());
         int status;
         while ((status = sslConnection.handshakeStatus()) == Constants.SSL_HANDSHAKE_IN_PROGRESS)
            Vm.sleep(25);
         if (status != Constants.SSL_OK)
            throw new MessagingException("SSL handshake failed");
         sslReader = new SSLReadHolder();
         buffer.mark();
      }

      SSLStream(String host, int port, int connectionTimeout, int readWriteTimeout) throws UnknownHostException,
            IOException, MessagingException, CryptoException
      {
         super(host, port, connectionTimeout);
         connection.readTimeout = connection.writeTimeout = readWriteTimeout;
         sslClient = new SSLClient(Constants.SSL_SERVER_VERIFY_LATER, 0);
         sslConnection = sslClient.connect(this, null);
         Exception e = sslConnection.getLastException();
         if (e != null)
            throw new MessagingException(e.getMessage());
         int status;
         while ((status = sslConnection.handshakeStatus()) == Constants.SSL_HANDSHAKE_IN_PROGRESS)
            Vm.sleep(25);
         if (status != Constants.SSL_OK)
            throw new MessagingException("SSL handshake failed");
         sslReader = new SSLReadHolder();
         buffer.mark();
      }

      public int readBytes(byte[] buf, int start, int count) throws IOException
      {
         if (buffer.available() == 0)
         {
            int sslReadBytes = sslConnection.read(sslReader);
            if (sslReadBytes > 0)
               buffer.writeBytes(sslReader.getData(), 0, sslReadBytes);
            buffer.mark();
         }
         int readBytes = buffer.readBytes(buf, start, count);
         buffer.reuse();
         buffer.mark();

         return readBytes;
      }

      public int writeBytes(byte[] buf, int start, int count) throws IOException
      {
         if (start > 0)
         {
            byte[] buf2 = new byte[count];
            Vm.arrayCopy(buf, start, buf2, 0, count);
            buf = buf2;
         }
         return sslConnection.write(buf, count);
         ;
      }

      public void close() throws IOException
      {
         // TODO Auto-generated method stub

      }
   }
}
