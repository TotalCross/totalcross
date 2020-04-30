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
import totalcross.net.Socket;
import totalcross.util.Properties;

/**
 * Used to store properties used by the messaging API.
 * 
 * <P>
 * The SMTP protocol provider supports the following properties, which may be set in the <code>MailSession</code>
 * object. The properties are always set as strings; the Type column describes how the string is interpreted. For
 * example, use
 * 
 * <PRE>
 * props.put(&quot;mail.smtp.port&quot;, &quot;888&quot;);
 * </PRE>
 * 
 * to set the <CODE>mail.smtp.port</CODE> property, which is of type int.
 * <P>
 * <TABLE BORDER>
 * <TR>
 * <TH>Name</TH>
 * <TH>Type</TH>
 * <TH>Description</TH>
 * </TR>
 * 
 * <TR>
 * <TD>mail.smtp.user</TD>
 * <TD>String</TD>
 * <TD>Default user name for SMTP.</TD>
 * </TR>
 * 
 * <TR>
 * <TD>mail.smtp.password</TD>
 * <TD>String</TD>
 * <TD>SMTP Password for the default user name.</TD>
 * </TR>
 * 
 * <TR>
 * <TD>mail.smtp.host</TD>
 * <TD>String</TD>
 * <TD>The SMTP server to connect to.</TD>
 * </TR>
 * 
 * <TR>
 * <TD>mail.smtp.port</TD>
 * <TD>int</TD>
 * <TD>The SMTP server port to connect to, if the connect() method doesn't explicitly specify one. Defaults to 25.</TD>
 * </TR>
 * 
 * <TR>
 * <TD>mail.smtp.connectiontimeout</TD>
 * <TD>int</TD>
 * <TD>Socket connection timeout value in milliseconds. Default is 5000 milliseconds.</TD>
 * </TR>
 * 
 * <TR>
 * <TD>mail.smtp.timeout</TD>
 * <TD>int</TD>
 * <TD>Socket I/O timeout value in milliseconds. Default is 2000 milliseconds.</TD>
 * </TR>
 * 
 * <TR>
 * <TD>mail.smtp.from</TD>
 * <TD>String</TD>
 * <TD>Email address to use for SMTP MAIL command. This sets the envelope return address. Defaults to msg.getFrom()[0]
 * or ConnectionManager.getLocalHost(). NOTE: mail.smtp.user was previously used for this.</TD>
 * </TR>
 * 
 * <TR>
 * <TD>mail.smtp.auth</TD>
 * <TD>boolean</TD>
 * <TD>If true, attempt to authenticate the user using the AUTH command. Defaults to false.</TD>
 * </TR>
 * 
 * <TR>
 * <TD>mail.smtp.starttls.enable</TD>
 * <TD>boolean</TD>
 * <TD>
 * If true, enables the use of the <code>STARTTLS</code> command (if supported by the server) to switch the connection
 * to a TLS-protected connection before issuing any login commands. Note that an appropriate trust store must configured
 * so that the client will trust the server's certificate. Defaults to false.</TD>
 * </TR>
 * 
 * <TR>
 * <TD>mail.smtp.starttls.required</TD>
 * <TD>boolean</TD>
 * <TD>
 * If true, requires the use of the STARTTLS command. If the server doesn't support the STARTTLS command, or the command
 * fails, the connect method will fail. Defaults to false.</TD>
 * </TR>
 * 
 * <TR>
 * <TD>mail.smtp.ssl.port</TD>
 * <TD>int</TD>
 * <TD>
 * The SMTP server port to connect to when STARTTLS is enabled, if the connect() method doesn't explicitly specify one.
 * Defaults to 587.</TD>
 * </TR>
 * 
 * <TR>
 * <TD>mail.smtp.ssl.socketFactory.class</TD>
 * <TD>String</TD>
 * <TD>
 * If set, specifies the name of a class that extends the totalcross.net.ssl.SSLSocketFactory class. This class will be
 * used to create SMTP SSL sockets.</TD>
 * </TR>
 * 
 * </TABLE>
 * <P>
 * 
 * <P>
 * The POP3 protocol provider supports the following properties, which may be set in the <code>MailSession</code>
 * object. The properties are always set as strings; the Type column describes how the string is interpreted. For
 * example, use
 * 
 * <PRE>
 * props.put(&quot;mail.pop3.port&quot;, &quot;888&quot;);
 * </PRE>
 * 
 * to set the <CODE>mail.pop3.port</CODE> property, which is of type int.
 * <P>
 * <TABLE BORDER>
 * <TR>
 * <TH>Name</TH>
 * <TH>Type</TH>
 * <TH>Description</TH>
 * </TR>
 * 
 * <TR>
 * <TD>mail.pop3.user</TD>
 * <TD>String</TD>
 * <TD>Default user name for POP3.</TD>
 * </TR>
 * 
 * <TR>
 * <TD>mail.pop3.host</TD>
 * <TD>String</TD>
 * <TD>The POP3 server to connect to.</TD>
 * </TR>
 * 
 * <TR>
 * <TD>mail.pop3.port</TD>
 * <TD>int</TD>
 * <TD>The POP3 server port to connect to, if the connect() method doesn't explicitly specify one. Defaults to 110.</TD>
 * </TR>
 * 
 * <TR>
 * <TD>mail.pop3.connectiontimeout</TD>
 * <TD>int</TD>
 * <TD>Socket connection timeout value in milliseconds. Default is infinite timeout.</TD>
 * </TR>
 * 
 * <TR>
 * <TD>mail.pop3.timeout</TD>
 * <TD>int</TD>
 * <TD>Socket I/O timeout value in milliseconds. Default is infinite timeout.</TD>
 * </TR>
 * 
 * </TABLE>
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
