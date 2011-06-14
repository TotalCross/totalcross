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

import totalcross.io.ByteArrayStream;
import totalcross.io.IOException;
import totalcross.io.LineReader;
import totalcross.net.AuthenticationException;
import totalcross.net.Base64;
import totalcross.net.Socket;
import totalcross.net.UnknownHostException;
import totalcross.net.ssl.Constants;
import totalcross.net.ssl.SSL;
import totalcross.net.ssl.SSLClient;
import totalcross.net.ssl.SSLReadHolder;
import totalcross.sys.Convert;
import totalcross.sys.InvalidNumberException;
import totalcross.sys.Vm;

/**
 * This class implements the Transport abstract class using SMTP for message submission and transport.
 * 
 * @since TotalCross 1.13
 */
public class SMTPTransport extends Transport
{
   Socket connection;

   LineReader connectionReader; //flsobral@tc123_55: use LineReader for reading.
   
   int authSupported = 0;
   
   boolean supportsTLS;
   
   boolean requiresTLS;
   
   String lastServerResponse;
   
   private static final String ehlo = "EHLO ..." + Convert.CRLF;
   private static final String starttls = "STARTTLS" + Convert.CRLF;

   protected SMTPTransport(MailSession session)
   {
      super(session);
   }

   protected void protocolConnect(String host, int port, String login, String password) throws AuthenticationException, MessagingException
   {
      authenticate(login, password);
   }

   private void authenticate(String login, String password) throws MessagingException, AuthenticationException
   {
      switch (authSupported)
      {
         case 1: // auth with LOGIN
            issueCommand("AUTH LOGIN" + Convert.CRLF, 334);
            issueCommand(Base64.encode(login.getBytes()) + Convert.CRLF, 334);
            issueCommand(Base64.encode(password.getBytes()) + Convert.CRLF, 235);
         break;
         case 2: // auth with PLAIN
            String auth = login + '\0' + login + '\0' + password;
            issueCommand("AUTH PLAIN " + Base64.encode(auth.getBytes()) + Convert.CRLF, 235);
         break;
         case 3: // auth with CRAM-MD5, not supported yet.
         default:
            throw new AuthenticationException("Unsupported authentication type.");
      }
   }

   protected void sendMessage(Message message) throws MessagingException
   {
      try
      {
         // WRITE SENDER ADDRESS
         issueCommand("MAIL FROM:<" + session.get(MailSession.SMTP_USER).toString() + ">" + Convert.CRLF, 250);
         // RCPT TO
         for (int i = message.recipients.size() - 1; i >= 0; i--)
            issueCommand("RCPT TO:<" + ((String) message.recipients.items[i]) + ">" + Convert.CRLF, 250);
         // START DATA
         issueCommand("DATA" + Convert.CRLF, 354);

         // WRITE MESSAGE
         message.writeTo(connection);

         // END DATA
         connection.readTimeout = 40000; //flsobral: some SMTP servers are really slow to reply the message terminator, so we wait a little longer here. This is NOT related to the device connection.
         issueCommand(Convert.CRLF + "." + Convert.CRLF, 250);
         // QUIT
         issueCommand("QUIT" + Convert.CRLF, 221);

         connection.close();
         connectionReader = null;
         connection = null;
      }
      catch (IOException e)
      {
         throw new MessagingException(e.getMessage());
      }
   }

   protected void ehlo() throws MessagingException
   {
      issueCommand(ehlo, 220);
      while (readServerResponse() == 220); // the HELO reply may be followed by textual messages, just ignore them.
      
      authSupported = 0;
      do
      {
         int start = lastServerResponse.indexOf("AUTH");
         if (start != -1)
         {
            if ((start = lastServerResponse.indexOf("LOGIN")) != -1)
               authSupported = 1;
            else if ((start = lastServerResponse.indexOf("PLAIN")) != -1)
               authSupported = 2;
            else if ((start = lastServerResponse.indexOf("CRAM-MD5")) != -1)
               authSupported = 3;
            else
               authSupported = -1;
         }
         else if (lastServerResponse.indexOf("STARTTLS") != -1)
            supportsTLS = true; //flsobral: server supports secure connections            
         readServerResponse();
      } while (lastServerResponse.charAt(3) != ' ');
      
      requiresTLS = supportsTLS && authSupported == 0;
   }

   public void connect(Socket connection) throws MessagingException
   {
      this.connection = connection;
      try
      {
         connectionReader = new LineReader(connection);
      }
      catch (IOException e)
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
      
      SSLStream(Socket connection) throws MessagingException
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
      
      
      SSLStream(String host, int port, int connectionTimeout, int readWriteTimeout) throws UnknownHostException, IOException, MessagingException
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
         return sslConnection.write(buf, count);;
      }

      public void close() throws IOException
      {
         // TODO Auto-generated method stub
         
      }
   }
   
   protected int readServerResponse() throws MessagingException
   {
      try
      {
         lastServerResponse = connectionReader.readLine();
         return Convert.toInt(lastServerResponse.substring(0, 3));
      }
      catch (InvalidNumberException e)
      {
         throw new MessagingException(e.getMessage() + "\n Reply: " + lastServerResponse);
      }      
      catch (IOException e)
      {
         throw new MessagingException(e.getMessage());
      }      
   }

   public void issueCommand(String cmd, int expect) throws MessagingException
   {
      int responseCode = simpleCommand(cmd.getBytes());
      if (expect != -1 && responseCode != expect)
         throw new MessagingException("Unexpected response code. Expected " + expect + ", but received " + responseCode);
   } 
   
   protected int simpleCommand(byte[] command) throws MessagingException
   {
      try
      {
         connection.writeBytes(command);
         return readServerResponse();
      }      
      catch (IOException e)
      {
         throw new MessagingException(e.getMessage());
      }
   }
   
   public int simpleCommand(String command) throws MessagingException
   {
      return simpleCommand(command.getBytes());
   } 
   
   public boolean supportsExtension(String ext)
   {
      if ("STARTTLS".equals(ext))
         return supportsTLS;
      return false;
   }

   public boolean getRequireStartTLS()
   {
      return requiresTLS;
   }

   protected void startTLS() throws MessagingException
   {
      issueCommand(starttls, 220);
      connection = new SSLStream(connection);
      try
      {
         connectionReader.setStream(connection);
      }
      catch (IOException e)
      {
         throw new MessagingException(e.getMessage());
      }      
   }   
}
