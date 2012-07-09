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

package totalcross.net.mail;

import totalcross.io.IOException;
import totalcross.net.*;
import totalcross.net.ssl.SSLSocketFactory;
import totalcross.util.Properties;

/**
 * An abstract class that models a message transport. Subclasses provide actual implementations.
 * 
 * @since TotalCross 1.13
 */
public abstract class Transport extends Service
{
   protected Transport(MailSession session)
   {
      super(session);
   }

   /**
    * Send a message. The message will be sent to all recipient addresses specified in the message, using message
    * transports appropriate to each address, as specified on the static instance of MailSession.
    * 
    * NOTE: Only SMTP transport is currently supported.
    * 
    * @param message
    *           the message to send
    * @throws MessagingException
    * @throws AuthenticationException
    * @since TotalCross 1.13
    */
   public static void send(Message message) throws MessagingException, AuthenticationException
   {
      Transport.send(message, MailSession.getDefaultInstance());
   }

   /**
    * Send a message. The message will be sent to all recipient addresses specified in the message, using message
    * transports appropriate to each address, as specified on the given instance of MailSession.
    * 
    * NOTE: Only SMTP transport is currently supported.
    * 
    * @param message
    *           the message to send
    * @param session
    *           the MailSession to be used
    * @throws MessagingException
    * @throws AuthenticationException
    * @since TotalCross 1.13
    */
   public static void send(Message message, MailSession session) throws MessagingException, AuthenticationException
   {
      String host = session.get(MailSession.SMTP_HOST).toString();
      int port = ((Properties.Int) session.get(MailSession.SMTP_PORT)).value;
      int connectionTimeout = ((Properties.Int) session.get(MailSession.SMTP_CONNECTIONTIMEOUT)).value;
      int timeout = ((Properties.Int) (Properties.Int) session.get(MailSession.SMTP_TIMEOUT)).value;
      boolean tlsEnabled = ((Properties.Boolean) session.get(MailSession.SMTP_STARTTLS)).value;
      boolean tlsRequired = ((Properties.Boolean) session.get(MailSession.SMTP_STARTTLS_REQUIRED)).value;
      
      try
      {
         SocketFactory sf = tlsEnabled ? SSLSocketFactory.getDefault() : SocketFactory.getDefault();
         Socket connection = sf.createSocket(host, port, connectionTimeout);
         connection.readTimeout = connection.writeTimeout = timeout;
         
         SMTPTransport smtp = new SMTPTransport(session);
         smtp.connect(connection);
         smtp.ehlo();
         boolean requiresTLS = smtp.getRequireStartTLS();
         if (!tlsRequired && !tlsEnabled && requiresTLS)
            throw new MessagingException("Server requires authentication through a secure connection - See MailSession.SMTP_STARTTLS");         
         boolean supportsTLS = requiresTLS ? true : smtp.supportsExtension("STARTTLS");
         if (tlsRequired && !supportsTLS)
            throw new MessagingException("MailSession.SMTP_STARTTLS_REQUIRED is enabled, but server doesn't support secure connections");
         
         if (supportsTLS && (tlsRequired || tlsEnabled))
         {
            int sslPort = ((Properties.Int) session.get(MailSession.SMTP_SSL_PORT)).value;
            smtp.ehlo();
         }
         String user = session.get(MailSession.SMTP_USER).toString();
         String pass = session.get(MailSession.SMTP_PASS).toString();
         smtp.protocolConnect(host, port, user, pass);            
         smtp.sendMessage(message);         
      }
      catch (UnknownHostException e)
      {
         throw new MessagingException(e);
      }
      catch (IOException e)
      {
         throw new MessagingException(e);
      }
   }

   public abstract void sendMessage(Message message) throws MessagingException;
}
