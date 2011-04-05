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

import totalcross.io.IOException;
import totalcross.io.LineReader;
import totalcross.net.AuthenticationException;
import totalcross.net.Base64;
import totalcross.net.Socket;
import totalcross.net.SocketFactory;
import totalcross.net.UnknownHostException;
import totalcross.sys.Convert;
import totalcross.sys.InvalidNumberException;
import totalcross.util.Properties;

/**
 * This class implements the Transport abstract class using SMTP for message submission and transport.
 * 
 * @since TotalCross 1.13
 */
public class SMTPTransport extends Transport
{
   Socket connection;

   LineReader connectionReader; //flsobral@tc123_55: use LineReader for reading.

   private static final String ehlo = "EHLO ..." + Convert.CRLF;

   protected SMTPTransport(MailSession session)
   {
      super(session);
   }

   protected void protocolConnect(String host, int port, String login, String password) throws AuthenticationException, MessagingException
   {
      try
      {
         SocketFactory sf = (SocketFactory) Class.forName("totalcross.net.SocketFactory").newInstance();
         if (session == null)
            connection = sf.createSocket(host, port);
         else
         {
            Properties.Int connectionTimeout = (Properties.Int) session.get(MailSession.SMTP_CONNECTIONTIMEOUT);
            connection = sf.createSocket(host, port, connectionTimeout != null ? connectionTimeout.value : Socket.DEFAULT_OPEN_TIMEOUT);
            Properties.Int timeout = (Properties.Int) session.get(MailSession.SMTP_TIMEOUT);
            if (timeout != null)
               connection.readTimeout = connection.writeTimeout = timeout.value;
            connectionReader = new LineReader(connection);
         }
         authenticate(login, password);
      }
      catch (InstantiationException e)
      {
         throw new MessagingException(e.getMessage());
      }
      catch (IllegalAccessException e)
      {
         throw new MessagingException(e.getMessage());
      }
      catch (ClassNotFoundException e)
      {
         throw new MessagingException(e.getMessage());
      }
      catch (InvalidNumberException e)
      {
         throw new MessagingException(e.getMessage());
      }
      catch (UnknownHostException e)
      {
         throw new MessagingException(e.getMessage());
      }
      catch (IOException e)
      {
         throw new MessagingException(e.getMessage());
      }
   }

   private void authenticate(String login, String password) throws IOException, MessagingException, InvalidNumberException,
         AuthenticationException
   {
      connection.writeBytes(ehlo);
      String reply = connectionReader.readLine();
      int receivedCode = Convert.toInt(reply.substring(0, 3));

      if (receivedCode != 220)
         throw new AuthenticationException(reply);

      String serverConfiguration;
      int authSupported = 0; // no auth required
      do
      {
         serverConfiguration = connectionReader.readLine();
         int start = serverConfiguration.indexOf("AUTH");
         if (start != -1)
         {
            if ((start = serverConfiguration.indexOf("LOGIN")) != -1)
               authSupported = 1;
            else if ((start = serverConfiguration.indexOf("PLAIN")) != -1)
               authSupported = 2;
            else if ((start = serverConfiguration.indexOf("CRAM-MD5")) != -1)
               authSupported = 3;
            else
            {
               authSupported = -1;
               reply = serverConfiguration;
            }
         }
      } while (serverConfiguration.charAt(3) != ' ');

      switch (authSupported)
      {
         case 1: // auth with LOGIN
            connection.writeBytes("AUTH LOGIN" + Convert.CRLF);
            reply = connectionReader.readLine();
            if ((receivedCode = Convert.toInt(reply.substring(0, 3))) != 334)
               throw new AuthenticationException(reply);
            connection.writeBytes(Base64.encode(login.getBytes()) + Convert.CRLF);
            reply = connectionReader.readLine();
            if ((receivedCode = Convert.toInt(reply.substring(0, 3))) != 334)
               throw new AuthenticationException(reply);
            connection.writeBytes(Base64.encode(password.getBytes()) + Convert.CRLF);
            reply = connectionReader.readLine();
            if ((receivedCode = Convert.toInt(reply.substring(0, 3))) != 235)
               throw new AuthenticationException(reply);
         break;
         case 2: // auth with PLAIN
            String auth = login + '\0' + login + '\0' + password;
            connection.writeBytes("AUTH PLAIN " + Base64.encode(auth.getBytes()) + Convert.CRLF);
            reply = connectionReader.readLine();
            if ((receivedCode = Convert.toInt(reply.substring(0, 3))) != 235)
               throw new AuthenticationException(reply);
         break;
         case 3: // auth with CRAM-MD5, not supported yet.
         default:
            throw new AuthenticationException("Unsupported authentication type: " + reply);
      }
   }

   protected void sendMessage(Message message) throws MessagingException
   {
      try
      {
         // WRITE SENDER ADDRESS
         writeCommand(connection, "MAIL FROM:<" + session.get(MailSession.SMTP_USER).toString() + ">" + Convert.CRLF, 250);
         // RCPT TO
         for (int i = message.recipients.size() - 1; i >= 0; i--)
            writeCommand(connection, "RCPT TO:<" + ((String) message.recipients.items[i]) + ">" + Convert.CRLF, 250);
         // START DATA
         writeCommand(connection, "DATA" + Convert.CRLF, 354);

         // WRITE MESSAGE
         message.writeTo(connection);

         // END DATA
         writeCommand(connection, Convert.CRLF + "." + Convert.CRLF, 250);
         // QUIT
         writeCommand(connection, "QUIT" + Convert.CRLF, 221);

         connection.close();
         connectionReader = null;
         connection = null;
      }
      catch (IOException e)
      {
         throw new MessagingException(e.getMessage());
      }
   }

   private void writeCommand(Socket stream, String command, int expectedCode) throws IOException, MessagingException
   {
      int receivedCode = -1;
      stream.writeBytes(command);
      String reply = connectionReader.readLine();
      try
      {
         receivedCode = Convert.toInt(reply.substring(0, 3));
      }
      catch (InvalidNumberException e)
      {
      }
      finally
      {
         if (receivedCode != expectedCode)
            throw new MessagingException(reply);
      }
   }
}
