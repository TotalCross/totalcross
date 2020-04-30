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
import totalcross.net.AuthenticationException;
import totalcross.net.Socket;
import totalcross.net.SocketFactory;
import totalcross.net.UnknownHostException;
import totalcross.net.ssl.SSLSocketFactory;
import totalcross.util.Properties;

/**
 * An abstract class that models a message transport. Subclasses provide actual implementations.
 * 
 * @since TotalCross 1.13
 */
public abstract class Transport extends Service {
  protected Transport(MailSession session) {
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
  public static void send(Message message) throws MessagingException, AuthenticationException {
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
  public static void send(Message message, MailSession session) throws MessagingException, AuthenticationException {
    if (message.recipients == null || message.recipients.isEmpty()) {
      throw new MessagingException("Cannot send a message without recipients!");
    }
    String host = session.getNotNull(MailSession.SMTP_HOST).toString();
    int connectionTimeout = ((Properties.Int) session.getNotNull(MailSession.SMTP_CONNECTIONTIMEOUT)).value;
    int timeout = ((Properties.Int) session.getNotNull(MailSession.SMTP_TIMEOUT)).value;
    boolean tlsEnabled = ((Properties.Boolean) session.getNotNull(MailSession.SMTP_STARTTLS)).value;

    int port = tlsEnabled ? ((Properties.Int) session.getNotNull(MailSession.SMTP_SSL_PORT)).value
        : ((Properties.Int) session.getNotNull(MailSession.SMTP_PORT)).value;
    String user = session.getNotNull(MailSession.SMTP_USER).toString();
    String password = session.getNotNull(MailSession.SMTP_PASS).toString();

    try {
      SocketFactory sf = null;
      if (!tlsEnabled) {
        sf = SocketFactory.getDefault();
      } else {
        Properties.Str sslSocketFactoryClass = (Properties.Str) session.get(MailSession.SMTP_SSL_SOCKET_FACTORY_CLASS);
        if (sslSocketFactoryClass != null && sslSocketFactoryClass.value != null) {
          sf = (SocketFactory) Class.forName(sslSocketFactoryClass.value).newInstance();
        } else {
          sf = SSLSocketFactory.getDefault();
        }
      }
      Socket connection = sf.createSocket(host, port, connectionTimeout);
      connection.readTimeout = connection.writeTimeout = timeout;

      SMTPTransport smtp = (SMTPTransport) session.getTransport(tlsEnabled ? "smtps" : "smtp");
      smtp.connect(connection);

      smtp.protocolConnect(host, port, user, password);
      smtp.sendMessage(message);
    } catch (InstantiationException e) {
      throw new MessagingException(e);
    } catch (IllegalAccessException e) {
      throw new MessagingException(e);
    } catch (ClassNotFoundException e) {
      throw new MessagingException(e);
    } catch (UnknownHostException e) {
      throw new MessagingException(e);
    } catch (IOException e) {
      throw new MessagingException(e);
    }
  }

  public abstract void sendMessage(Message message) throws MessagingException;
}
