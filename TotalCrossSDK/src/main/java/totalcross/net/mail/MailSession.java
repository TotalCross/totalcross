// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.net.mail;

import totalcross.io.DataStream;
import totalcross.io.IOException;
import totalcross.net.Socket;
import totalcross.util.Properties;

/**
 * Used to store properties used by the messaging API.
 *
 * <p>The SMTP properties are stored as string keys on the session, with the value
 * type indicated below:
 * <ul>
 * <li><code>mail.smtp.user</code>: default SMTP user name.</li>
 * <li><code>mail.smtp.password</code>: SMTP password for the default user name.</li>
 * <li><code>mail.smtp.host</code>: SMTP server to connect to.</li>
 * <li><code>mail.smtp.port</code>: SMTP server port, defaulting to 25.</li>
 * <li><code>mail.smtp.connectiontimeout</code>: socket connection timeout in milliseconds.</li>
 * <li><code>mail.smtp.timeout</code>: socket I/O timeout in milliseconds.</li>
 * <li><code>mail.smtp.from</code>: email address to use for SMTP MAIL commands.</li>
 * <li><code>mail.smtp.auth</code>: whether to authenticate with AUTH.</li>
 * <li><code>mail.smtp.starttls.enable</code>: whether to enable STARTTLS.</li>
 * <li><code>mail.smtp.starttls.required</code>: whether STARTTLS is mandatory.</li>
 * <li><code>mail.smtp.ssl.port</code>: SMTP SSL port, defaulting to 587 when STARTTLS is enabled.</li>
 * <li><code>mail.smtp.ssl.socketFactory.class</code>: socket factory class name for SMTP SSL sockets.</li>
 * </ul>
 *
 * <p>The POP3 properties follow the same pattern:
 * <ul>
 * <li><code>mail.pop3.user</code>: default POP3 user name.</li>
 * <li><code>mail.pop3.host</code>: POP3 server to connect to.</li>
 * <li><code>mail.pop3.port</code>: POP3 server port, defaulting to 110.</li>
 * <li><code>mail.pop3.connectiontimeout</code>: socket connection timeout in milliseconds.</li>
 * <li><code>mail.pop3.timeout</code>: socket I/O timeout in milliseconds.</li>
 * <li><code>mail.pop3.password</code>: POP3 password for the default user name.</li>
 * </ul>
 *
 * @since TotalCross 1.13
 */
public class MailSession extends Properties {
  private static MailSession defaultInstance = new MailSession();

  /** Default user name for SMTP. */
  public static final String SMTP_USER = "mail.smtp.user";
  /** SMTP Password for the default user name. */
  public static final String SMTP_PASS = "mail.smtp.password";
  /** The SMTP server to connect to. */
  public static final String SMTP_HOST = "mail.smtp.host";
  /** The SMTP server port to connect to, if the connect() method doesn't explicitly specify one. Defaults to 25. */
  public static final String SMTP_PORT = "mail.smtp.port";
  /** Socket connection timeout value in milliseconds. Default is 2000 milliseconds. */
  public static final String SMTP_CONNECTIONTIMEOUT = "mail.smtp.connectiontimeout";
  /** Socket I/O timeout value in milliseconds. Default is 5000 milliseconds. */
  public static final String SMTP_TIMEOUT = "mail.smtp.timeout";
  /**
   * Email address to use for SMTP MAIL command. This sets the envelope return address. Defaults to msg.getFrom()[0] or
   * ConnectionManager.getLocalHost(). NOTE: mail.smtp.user was previously used for this.
   */
  public static final String SMTP_FROM = "mail.smtp.from";
  /** If true, attempt to authenticate the user using the AUTH command. Defaults to false. */
  public static final String SMTP_AUTH = "mail.smtp.auth";
  /**
   * If true, enables the use of the STARTTLS command (if supported by the server) to switch the connection to a
   * TLS-protected connection before issuing any login commands. Note that an appropriate trust store must configured
   * so that the client will trust the server's certificate. Defaults to false.
   */
  public static final String SMTP_STARTTLS = "mail.smtp.starttls.enable";
  /**
   * If true, requires the use of the STARTTLS command. If the server doesn't support the STARTTLS command, or the
   * command fails, the connect method will fail. Defaults to false.
   */
  public static final String SMTP_STARTTLS_REQUIRED = "mail.smtp.starttls.required";
  /**
   * The SMTP server port to connect to when STARTTLS is enabled, if the connect() method doesn't explicitly specify
   * one. Defaults to 587.
   */
  public static final String SMTP_SSL_PORT = "mail.smtp.ssl.port";
  /**
   * If set, specifies the name of a class that extends the totalcross.net.ssl.SSLSocketFactory class. This class will
   * be used to create SMTP SSL sockets.
   */
  public static final String SMTP_SSL_SOCKET_FACTORY_CLASS = "mail.smtp.ssl.socketFactory.class";

  public static final String POP3_USER = "mail.pop3.user";
  public static final String POP3_HOST = "mail.pop3.host";
  public static final String POP3_PORT = "mail.pop3.port";
  public static final String POP3_CONNECTIONTIMEOUT = "mail.pop3.connectiontimeout";
  public static final String POP3_TIMEOUT = "mail.pop3.timeout";
  public static final String POP3_PASS = "mail.pop3.password";

  protected MailSession() {
    super();
    put(SMTP_CONNECTIONTIMEOUT, new Int(Socket.DEFAULT_OPEN_TIMEOUT));
    put(SMTP_TIMEOUT, new Int(Socket.DEFAULT_WRITE_TIMEOUT));
    put(SMTP_STARTTLS, new Boolean(false));
    put(SMTP_STARTTLS_REQUIRED, new Boolean(false));
    put(SMTP_PORT, new Int(25));
    put(SMTP_SSL_PORT, new Int(587));
  }

  /**
   * Creates a new empty MailSession.
   * 
   * @return the new MailSession
   * @since TotalCross 1.13
   */
  public static MailSession getInstance() {
    return new MailSession();
  }

  /**
   * Returns a static instance of MailSession, which may be initialized during the application startup.
   * 
   * @return the static instance of MailSession.
   * @since TotalCross 1.13
   */
  public static MailSession getDefaultInstance() {
    return defaultInstance;
  }

  /**
   * Get a Store object that implements the specified protocol. If an appropriate Store object cannot be obtained, null
   * is returned.
   * 
   * @param protocol
   * @return a Store object, or null if a provider for the given protocol is not found.
   * @since TotalCross 1.13
   */
  public Store getStore(String protocol) {
    if (protocol.equals("pop3")) {
      return new POP3Store(this);
    }
    return null;
  }

  /**
   * Get a Transport object that implements the specified protocol. If an appropriate Transport object cannot be
   * obtained, null is returned.
   * 
   * @param protocol
   * @return a Transport object
   */
  public Transport getTransport(String protocol) {
    if (protocol.equals("smtp")) {
      return new SMTPTransport(this);
    }
    if (protocol.equals("smtps")) {
      return new SMTPSSLTransport(this);
    }
    return null;
  }

  /**
   * Load properties from the given DataStream, but unlike Properties, the contents of the MailSession are not cleared
   * before reading from the DataStream.
   */
  @Override
  public void load(DataStream ds) throws IOException {
    super.load(ds, false);
  }

  /**
   * Retrieves the value of the property mapped to the given key, throwing a NullPointerException if the key is not
   * mapped to any value.
   * 
   * @param key 
   * @return
   */
  Value getNotNull(String key) {
    Value v = this.get(key);
    if (v == null) {
      throw new NullPointerException("Property '" + key + "' is null");
    }
    return v;
  }
}
