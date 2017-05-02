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
import totalcross.net.Socket;
import totalcross.net.ssl.SSLSocket;
import totalcross.util.Properties;

/**
 * This class implements the Transport abstract class using SMTP for message submission and transport over secure sockets.
 */
public class SMTPSSLTransport extends SMTPTransport
{
   protected SMTPSSLTransport(MailSession session)
   {
      super(session);
   }

   public void connect(Socket connection) throws MessagingException
   {
      super.connect(connection);

      boolean tlsRequired = ((Properties.Boolean) session.get(MailSession.SMTP_STARTTLS_REQUIRED)).value;
      if (tlsRequired && !supportsTLS)
         throw new MessagingException(
               "MailSession.SMTP_STARTTLS_REQUIRED is enabled, but server doesn't support secure connections");
   }

   protected void startTLS() throws MessagingException
   {
      try
      {
         issueCommand(starttls, 220);
         ((SSLSocket) connection).startHandshake();
      }
      catch (IOException e)
      {
         throw new MessagingException(e);
      }
   }
}
