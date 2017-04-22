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
import totalcross.net.Socket;
import totalcross.net.SocketFactory;

/**
 * An abstract class that models a message store and its access protocol, for storing and retrieving messages.
 * Subclasses provide actual implementations.
 * 
 * @since TotalCross 1.13
 */
public abstract class Store
{
   protected MailSession session;
   protected SocketFactory socketFactory;
   protected Socket connection;

   protected Store(MailSession session)
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

   /**
    * Close this store and terminate its connection. Any Messaging components (Folders, Messages, etc.) belonging to
    * this service are invalid after this store is closed. Note that the store is closed even if this method terminates
    * abnormally by throwing a MessagingException.
    * 
    * @throws MessagingException
    * @since TotalCross 1.13
    */
   public abstract void close() throws MessagingException;

   /**
    * Return the Folder object corresponding to the given name.
    * 
    * Folder objects are not cached by the Store, so invoking this method on the same name multiple times will return
    * that many distinct Folder objects.
    * 
    * @param name
    *           The name of the Folder. In some Stores, name can be an absolute path if it starts with the hierarchy
    *           delimiter. Else it is interpreted relative to the 'root' of this namespace.
    * @return Folder object
    * @since TotalCross 1.13
    */
   public abstract Folder getFolder(String name);

   /**
    * Returns a Folder object that represents the 'root' of the default namespace presented to the user by the Store.
    * 
    * @return the root Folder
    * @since TotalCross 1.13
    */
   public abstract Folder getDefaultFolder();
}
