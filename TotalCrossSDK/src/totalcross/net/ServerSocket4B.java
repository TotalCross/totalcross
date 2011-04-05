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



package totalcross.net;

import javax.microedition.io.Connector;
import javax.microedition.io.ServerSocketConnection;
import javax.microedition.io.SocketConnection;

import totalcross.io.IOException;
import totalcross.io.IllegalArgumentIOException;

public class ServerSocket4B
{
   Object serverRef;
   String addr;
   int port;
   int timeout = DEFAULT_SOTIMEOUT;
   private boolean dontFinalize;

   public static final int DEFAULT_SOTIMEOUT = 10000;
   public static final int DEFAULT_BACKLOG = 100;
   public static final int WAIT_FOREVER = 0;

   protected ServerSocket4B()
   {
   }

   public ServerSocket4B(int port) throws IOException
   {
      this(port, DEFAULT_SOTIMEOUT, DEFAULT_BACKLOG, null);
   }

   public ServerSocket4B(int port, int timeout) throws IOException
   {
      this(port, timeout, DEFAULT_BACKLOG, null);
   }

   public ServerSocket4B(int port, int timeout, String addr) throws IOException
   {
      this(port, timeout, DEFAULT_BACKLOG, addr);
   }

   public ServerSocket4B(int port, int timeout, int backlog, String addr) throws IOException
   {
      if (port < 0 || port > 65535)
         throw new IllegalArgumentIOException("Invalid value for argument 'port': " + port);
      if (timeout < 0)
         throw new IllegalArgumentIOException("Invalid value for argument 'timeout': " + timeout);
      if (backlog <= 0)
         throw new IllegalArgumentIOException("Invalid value for argument 'backlog': " + backlog);

      this.port = port;
      this.timeout = timeout;
      this.addr = addr;
      serversocketCreate(port, backlog, timeout, addr);
   }

   final void serversocketCreate(final int port, int backlog, int timeout, String addr) throws IOException
   {
      final StringBuffer exBuf = new StringBuffer(32);

      Thread thread = new Thread()
      {
         public void run()
         {
            try
            {
               serverRef = (ServerSocketConnection)Connector.open("socket://:" + port);
            }
            catch (java.io.IOException ex)
            {
               exBuf.append(ex.getMessage());
            }
         }

         public void interrupt()
         {
            super.interrupt();

            try
            {
               if (serverRef != null)
                  ((SocketConnection)serverRef).close();
            }
            catch (java.io.IOException e)
            {
            }
         }
      };

      thread.start();

      long t = System.currentTimeMillis();
      while (thread.isAlive() && (System.currentTimeMillis() - t) < timeout)
      {
         try
         {
            Thread.sleep(100);
         }
         catch (InterruptedException ex) {}
      }

      if (thread.isAlive())
      {
         thread.interrupt();
         throw new IOException("Connection timed out");
      }

      if (exBuf.length() > 0)
         throw new IOException(exBuf.toString());
   }

   public String getHost()
   {
      return addr;
   }

   public int getLocalPort()
   {
      return port;
   }

   public Socket4B accept() throws IOException
   {
      if (serverRef == null)
         throw new IOException("The server socket is closed");

      Socket4B client = new Socket4B();

      try
      {
         client.socketRef = (SocketConnection)((ServerSocketConnection)serverRef).acceptAndOpen();
      }
      catch (java.io.IOException ex)
      {
         throw new IOException(ex.getMessage());
      }

      return client;
   }

   /**
    *
    * @throws totalcross.io.IOException
    */
   public void close() throws totalcross.io.IOException
   {
      if (serverRef == null)
         throw new totalcross.io.IOException("The server socket is closed");

      try
      {
         ((ServerSocketConnection)serverRef).close();
      }
      catch (java.io.IOException ex)
      {
         throw new IOException(ex.getMessage());
      }
      finally
      {
         serverRef = null;
         dontFinalize = !dontFinalize;
      }
   }

   protected void finalize()
   {
      try
      {
         close();
      }
      catch (totalcross.io.IOException e)
      {
      }
   }
}
