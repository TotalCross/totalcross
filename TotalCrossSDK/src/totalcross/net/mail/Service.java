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

import totalcross.net.AuthenticationException;

/**
 * An abstract class that models a message store and its access protocol, for storing and retrieving messages.
 * Subclasses provide actual implementations.
 * 
 * @since TotalCross 1.13
 */
public abstract class Service
{
   protected MailSession session;

   protected String host;

   protected int port;

   protected String user;

   protected String password;

   protected Service(MailSession session)
   {
      this.session = session;
   }

   /**
    * A generic connect method that takes no parameters. Subclasses can implement the appropriate authentication
    * schemes. Subclasses that need additional information might want to use some properties or might get it
    * interactively.
    * 
    * Most clients should just call this method to connect to the store.
    * 
    * @throws AuthenticationException
    * @throws MessagingException
    * @since TotalCross 1.13
    */
   public abstract void connect() throws AuthenticationException, MessagingException;

   public abstract void connect(String host, int port, String user, String password) throws AuthenticationException,
         MessagingException;

   /**
    * Close this service and terminate its connection. Any Messaging components (Folders, Messages, etc.) belonging to
    * this service are invalid after this store is closed. Note that the store is closed even if this method terminates
    * abnormally by throwing a MessagingException.
    * 
    * @throws MessagingException
    * @since TotalCross 1.13
    */
   public abstract void close() throws MessagingException;

   protected void protocolConnect(String host, int port, String login, String password) throws AuthenticationException,
         MessagingException
   {}
}
