// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.net.mail;

import totalcross.io.IOException;
import totalcross.net.Socket;
import totalcross.net.ssl.SSLSocket;
import totalcross.util.Properties;

/**
 * This class implements the Transport abstract class using SMTP for message submission and transport over secure sockets.
 */
public class SMTPSSLTransport extends SMTPTransport {
  protected SMTPSSLTransport(MailSession session) {
    super(session);
  }

  @Override
  public void connect(Socket connection) throws MessagingException {
    super.connect(connection);

    boolean tlsRequired = ((Properties.Boolean) session.get(MailSession.SMTP_STARTTLS_REQUIRED)).value;
    if (tlsRequired && !supportsTLS) {
      throw new MessagingException(
          "MailSession.SMTP_STARTTLS_REQUIRED is enabled, but server doesn't support secure connections");
    }
  }

  @Override
  protected void startTLS() throws MessagingException {
    try {
      issueCommand(starttls, 220);
      ((SSLSocket) connection).startHandshake();
    } catch (IOException e) {
      throw new MessagingException(e);
    }
  }
}
