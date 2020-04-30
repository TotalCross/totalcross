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

import totalcross.io.DataStream;
import totalcross.io.IOException;
import totalcross.io.LineReader;
import totalcross.net.AuthenticationException;
import totalcross.net.Base64;
import totalcross.net.ConnectionManager;
import totalcross.net.Socket;
import totalcross.net.UnknownHostException;
import totalcross.sys.Convert;
import totalcross.sys.InvalidNumberException;
import totalcross.util.Properties;

/**
 * This class implements the Transport abstract class using SMTP for message submission and transport.
 * 
 * @since TotalCross 1.13
 */
public class SMTPTransport extends Transport {
  Socket connection;

  DataStream writer;

  LineReader reader;

  int authSupported = 0;

  boolean supportsTLS;

  boolean requiresTLS;

  String lastServerResponse;

  private static final String ehlo = "EHLO localhost" + Convert.CRLF;
  protected static final String starttls = "STARTTLS" + Convert.CRLF;

  protected SMTPTransport(MailSession session) {
    super(session);
  }

  @Override
  protected void protocolConnect(String host, int port, String user, String password)
      throws AuthenticationException, MessagingException {
    if (supportsTLS) {
      startTLS();
    }
    ehlo();

    switch (authSupported) {
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

  @Override
  public void sendMessage(Message message) throws MessagingException {
    try {
      // WRITE RETURN PATH
      Properties.Str smtpFrom = (Properties.Str) session.get(MailSession.SMTP_FROM);
      String returnPath = null;
      if (smtpFrom != null && smtpFrom.value != null) {
        returnPath = smtpFrom.value;
      } else {
        Address[] from = message.getFrom();
        if (from != null && from.length > 0 && from[0].address != null) {
          returnPath = from[0].address;
        } else {
          returnPath = ConnectionManager.getLocalHost();
        }
      }

      issueCommand("MAIL FROM:<" + returnPath + ">" + Convert.CRLF, 250);
      // RCPT TO
      for (int i = message.recipients.size() - 1; i >= 0; i--) {
        issueCommand("RCPT TO:<" + ((String) message.recipients.items[i]) + ">" + Convert.CRLF, 250);
      }
      // START DATA
      issueCommand("DATA" + Convert.CRLF, 354);

      // WRITE MESSAGE
      message.writeTo(writer);

      // END DATA
      connection.readTimeout = 40000; //flsobral: some SMTP servers are really slow to reply the message terminator, so we wait a little longer here. This is NOT related to the device connection.
      issueCommand(Convert.CRLF + "." + Convert.CRLF, 250);
    } catch (IOException e) {
      throw new MessagingException(e);
    }
  }

  protected boolean ehlo() throws MessagingException {
    String actualEhlo;
    try {
      actualEhlo = ehlo.replace("localhost", ConnectionManager.getLocalHost());
    } catch (UnknownHostException e) {
      actualEhlo = ehlo;
    }
    boolean ret = simpleCommand(actualEhlo) == 220;
    while (readServerResponse() == 220) {
      ; // the HELO reply may be followed by textual messages, just ignore them.
    }

    authSupported = 0;
    do {
      int start = lastServerResponse.indexOf("AUTH");
      if (start != -1) {
        if ((start = lastServerResponse.indexOf("LOGIN")) != -1) {
          authSupported = 1;
        } else if ((start = lastServerResponse.indexOf("PLAIN")) != -1) {
          authSupported = 2;
        } else if ((start = lastServerResponse.indexOf("CRAM-MD5")) != -1) {
          authSupported = 3;
        } else {
          authSupported = -1;
        }
      } else if (lastServerResponse.indexOf("STARTTLS") != -1) {
        supportsTLS = true; //flsobral: server supports secure connections            
      }
      readServerResponse();
    } while (lastServerResponse.charAt(3) != ' ');

    requiresTLS = supportsTLS && authSupported == 0;
    return ret;
  }

  public void connect(Socket connection) throws MessagingException {
    boolean tlsEnabled = ((Properties.Boolean) session.get(MailSession.SMTP_STARTTLS)).value;

    this.connection = connection;
    this.writer = new DataStream(connection, true);
    try {
      reader = new LineReader(connection);
      if (!ehlo()) {
        throw new MessagingException("Failed to greet the remote server.");
      }
      if (requiresTLS && !tlsEnabled) {
        throw new MessagingException(
            "Server requires authentication through a secure connection - See MailSession.SMTP_STARTTLS");
      }
    } catch (IOException e) {
      throw new MessagingException(e);
    }
  }

  protected int readServerResponse() throws MessagingException {
    try {
      lastServerResponse = reader.readLine();
      return Convert.toInt(lastServerResponse.substring(0, 3));
    } catch (InvalidNumberException e) {
      throw new MessagingException(e.getMessage() + "\n Reply: " + lastServerResponse);
    } catch (IOException e) {
      throw new MessagingException(e);
    }
  }

  public void issueCommand(String cmd, int expect) throws MessagingException {
    int responseCode = simpleCommand(cmd.getBytes());
    if (expect != -1 && responseCode != expect) {
      throw new MessagingException("Unexpected response code. Expected " + expect + ", but received " + responseCode);
    }
  }

  protected int simpleCommand(byte[] command) throws MessagingException {
    try {
      writer.writeBytes(command);
      return readServerResponse();
    } catch (IOException e) {
      throw new MessagingException(e);
    }
  }

  public int simpleCommand(String command) throws MessagingException {
    return simpleCommand(command.getBytes());
  }

  public boolean supportsExtension(String ext) {
    if ("STARTTLS".equals(ext)) {
      return supportsTLS;
    }
    return false;
  }

  public boolean getRequireStartTLS() {
    return requiresTLS;
  }

  @Override
  public void connect() throws AuthenticationException, MessagingException {
    // TODO Auto-generated method stub

  }

  /*
   * This method MUST ensure the service is completely invalidated and closed, even when an exception is thrown. That's
   * why each command is issued inside a try/catch block. (non-Javadoc)
   * 
   * @see totalcross.net.mail.Service#close()
   */
  @Override
  public void close() throws MessagingException {
    MessagingException exception = null;
    try {
      // QUIT
      issueCommand("QUIT" + Convert.CRLF, 221);
    } catch (MessagingException e) {
      exception = e;
    }

    try {
      writer.close();
    } catch (IOException e) {
      if (exception != null) {
        exception = new MessagingException(e);
      }
    }

    try {
      connection.close();
    } catch (IOException e) {
      if (exception != null) {
        exception = new MessagingException(e);
      }
    }

    reader = null;
    writer = null;
    connection = null;
    if (exception != null) {
      throw exception;
    }
  }

  @Override
  public void connect(String host, int port, String user, String password)
      throws AuthenticationException, MessagingException {
    // TODO Auto-generated method stub

  }

  /**
   * Issue the STARTTLS command and switch the socket to TLS mode if it succeeds.
   * 
   * @throws MessagingException
   */
  protected void startTLS() throws MessagingException {
  }
}
