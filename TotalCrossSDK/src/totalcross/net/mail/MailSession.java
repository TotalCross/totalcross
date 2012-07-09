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
 * <TD>Socket connection timeout value in milliseconds. Default is infinite timeout.</TD>
 * </TR>
 * 
 * <TR>
 * <TD>mail.smtp.timeout</TD>
 * <TD>int</TD>
 * <TD>Socket I/O timeout value in milliseconds. Default is infinite timeout.</TD>
 * </TR>
 * 
 * <TR>
 * <TD>mail.smtp.from</TD>
 * <TD>String</TD>
 * <TD>Email address to use for SMTP MAIL command. This sets the envelope return address. Defaults to msg.getFrom()[0] or ConnectionManager.getLocalHost(). NOTE: mail.smtp.user was previously used for this.</TD>
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
public class MailSession extends Properties
{
   private static MailSession defaultInstance = new MailSession();

   /** Default user name for SMTP. */
   public static final String SMTP_USER = "mail.smtp.user";
   /** The SMTP server to connect to. */
   public static final String SMTP_HOST = "mail.smtp.host";
   /** The SMTP server port to connect to, if the connect() method doesn't explicitly specify one. Defaults to 25. */
   public static final String SMTP_PORT = "mail.smtp.port";
   /** Socket connection timeout value in milliseconds. Default is infinite timeout. */
   public static final String SMTP_CONNECTIONTIMEOUT = "mail.smtp.connectiontimeout";
   /** Socket I/O timeout value in milliseconds. Default is infinite timeout. */
   public static final String SMTP_TIMEOUT = "mail.smtp.timeout";
   /** Email address to use for SMTP MAIL command. This sets the envelope return address. Defaults to msg.getFrom()[0] or ConnectionManager.getLocalHost(). NOTE: mail.smtp.user was previously used for this. */
   public static final String SMTP_FROM = "mail.smtp.from";
   /** If true, attempt to authenticate the user using the AUTH command. Defaults to false. */
   public static final String SMTP_AUTH = "mail.smtp.auth";
   public static final String SMTP_STARTTLS = "mail.smtp.starttls.enable";
   public static final String SMTP_PASS = "mail.smtp.password";
   
   public static final String SMTP_STARTTLS_REQUIRED = "abc";
   
   public static final String SMTP_SSL_PORT = "mail.smtp.ssl.port";

   public static final String POP3_USER = "mail.pop3.user";
   public static final String POP3_HOST = "mail.pop3.host";
   public static final String POP3_PORT = "mail.pop3.port";
   public static final String POP3_CONNECTIONTIMEOUT = "mail.pop3.connectiontimeout";
   public static final String POP3_TIMEOUT = "mail.pop3.timeout";
   public static final String POP3_PASS = "mail.pop3.password";

   protected MailSession()
   {
      super();
      put(SMTP_CONNECTIONTIMEOUT, new Int(0));
      put(SMTP_TIMEOUT, new Int(0));
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
   public static MailSession getInstance()
   {
      return new MailSession();
   }

   /**
    * Returns a static instance of MailSession, which may be initialized during the application startup.
    * 
    * @return the static instance of MailSession.
    * @since TotalCross 1.13
    */
   public static MailSession getDefaultInstance()
   {
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
   public Store getStore(String protocol)
   {
      if (protocol.equals("pop3"))
         return new POP3Store(this);
      return null;
   }
   
   public Transport getTransport(String protocol)
   {
      if (protocol.equals("smtp"))
         return new SMTPTransport(this);
      if (protocol.equals("smtps"))
         return new SMTPSSLTransport(this);      
      return null;
   }
}
