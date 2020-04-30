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
import totalcross.net.SocketFactory;
import totalcross.net.UnknownHostException;
import totalcross.sys.Convert;
import totalcross.util.Properties;

/**
 * A POP3 Message Store. Contains only one folder, "INBOX".
 * 
 * @since TotalCross 1.13
 */
public class POP3Store extends Store {
  private Folder inbox;

  private String host;
  private int port;
  private String user;
  private String pass;

  public static final String ROOT = "INBOX";

  static final String USER = "USER ";
  static final String PASS = "PASS ";
  static final String STAT = "STAT ";
  static final String LIST = "LIST ";
  static final String TOP = "TOP ";
  static final String UIDL = "UIDL ";
  static final String QUIT = "QUIT \r\n";

  protected POP3Store(MailSession session) {
    super(session);
    host = session.get(MailSession.POP3_HOST).toString();
    port = ((Properties.Int) session.get(MailSession.POP3_PORT)).value;
    user = session.get(MailSession.POP3_USER).toString();
    pass = session.get(MailSession.POP3_PASS).toString();
  }

  @Override
  public void connect() throws AuthenticationException, MessagingException {
    try {
      socketFactory = (SocketFactory) Class.forName("totalcross.net.SocketFactory").newInstance();
      connection = socketFactory.createSocket(host, port);
      connection.readLine();

      authenticate(user, pass);
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

  @Override
  public Folder getDefaultFolder() {
    return new POP3Folder(this);
  }

  /**
   * Only the name "INBOX" is supported.
   */
  @Override
  public Folder getFolder(String name) {
    if (!name.equals(ROOT)) {
      return null;
    }
    return new POP3Folder(this);
  }

  @Override
  public void close() throws MessagingException {
    if (inbox != null) {
      inbox.close(false);
    }

    try {
      connection.writeBytes(QUIT);
      connection.readLine();
    } catch (IOException e) {
      throw new MessagingException(e);
    }
  }

  private void authenticate(String user, String pass) throws IOException, AuthenticationException {
    String reply;
    connection.writeBytes(USER + user + Convert.CRLF);
    reply = connection.readLine();
    if (!reply.startsWith("+OK")) {
      throw new AuthenticationException();
    }

    connection.writeBytes(PASS + pass + Convert.CRLF);
    reply = connection.readLine();
    if (!reply.startsWith("+OK")) {
      throw new AuthenticationException();
    }
  }
}
