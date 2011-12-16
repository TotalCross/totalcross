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

import totalcross.net.AuthenticationException;
import totalcross.util.Properties;

/**
 * An abstract class that models a message transport. Subclasses provide actual implementations.
 * 
 * @since TotalCross 1.13
 */
public abstract class Transport
{
   protected MailSession session;

   protected Transport(MailSession session)
   {
      this.session = session;
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
      SMTPTransport smtp = new SMTPTransport(session);
      String host = session.get(MailSession.SMTP_HOST).toString();
      int port = ((Properties.Int) session.get(MailSession.SMTP_PORT)).value;
      String user = session.get(MailSession.SMTP_USER).toString();
      String pass = session.get(MailSession.SMTP_PASS).toString();
      smtp.protocolConnect(host, port, user, pass);
      smtp.sendMessage(message);
   }

   protected abstract void sendMessage(Message message) throws MessagingException;
}
