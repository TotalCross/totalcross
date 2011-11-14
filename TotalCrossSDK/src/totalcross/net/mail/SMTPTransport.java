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
import totalcross.net.*;
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
   
   protected void protocolConnect(String host, int port, String user, String password) throws AuthenticationException, MessagingException
   {
      this.host = host;
      if (port != -1)
         this.port = port;
      this.user = user;
      this.password = password;    
      
      try
      {
         connect(new Socket(host, port));
         ehlo();
      }
      catch (UnknownHostException e)
      {
         throw new MessagingException(e.getMessage());
      }
      catch (IOException e)
      {
         throw new MessagingException(e.getMessage());
      }
      
      switch (authSupported)
      {
         case 1: // auth with LOGIN
            issueCommand("AUTH LOGIN" + Convert.CRLF, 334);
            issueCommand(Base64.encode(user.getBytes()) + Convert.CRLF, 334);
            issueCommand(Base64.encode(password.getBytes()) + Convert.CRLF, 235);
         break;
         case 2: // auth with PLAIN
            String auth = user + '\0' + user + '\0' + password;
            issueCommand("AUTH PLAIN " + Base64.encode(auth.getBytes()) + Convert.CRLF, 235);
         break;
         case 3: // auth with CRAM-MD5, not supported yet.
         default:
            throw new AuthenticationException("Unsupported authentication type.");
      }
   }

   public void sendMessage(Message message) throws MessagingException
   {
      try
      {
         // WRITE RETURN PATH
         Properties.Str smtpFrom = (Properties.Str) session.get(MailSession.SMTP_FROM);
         String returnPath = null;
         if (smtpFrom != null && smtpFrom.value != null)
            returnPath = smtpFrom.value;
         else
         {
            Address[] from = message.getFrom();
            if (from != null && from.length > 0 && from[0].address != null)
               returnPath = from[0].address;
            else
               returnPath = ConnectionManager.getLocalHost();
         }
 
         issueCommand("MAIL FROM:<" + returnPath + ">" + Convert.CRLF, 250);
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

   public void connect() throws AuthenticationException, MessagingException
   {
      // TODO Auto-generated method stub
      
   }

   public void close() throws MessagingException
   {
      // TODO Auto-generated method stub
      
   }

   public void connect(String host, int port, String user, String password) throws AuthenticationException,
         MessagingException
   {
      // TODO Auto-generated method stub
      
   }   
}
