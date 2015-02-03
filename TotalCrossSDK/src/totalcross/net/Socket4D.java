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



package totalcross.net;

import totalcross.io.Stream;
import totalcross.sys.*;

public class Socket4D extends Stream
{
   Object socketRef;
   
   public int readTimeout = DEFAULT_READ_TIMEOUT;
   public int writeTimeout = DEFAULT_WRITE_TIMEOUT;
   boolean dontFinalize;
   private String host;
   private int port;

   public static final int DEFAULT_OPEN_TIMEOUT = 2000;
   public static final int DEFAULT_READ_TIMEOUT = 5000;
   public static final int DEFAULT_WRITE_TIMEOUT = 2000;

   native void socketCreate(final String host, final int port, final int timeout,
         final boolean noLinger);

   native private void nativeClose() throws totalcross.io.IOException;

   native private int readWriteBytes(byte buf[], int start, int count, boolean isRead)
         throws totalcross.io.IOException;

   protected Socket4D()
   {
   }

   public Socket4D(String host, int port)
         throws totalcross.net.UnknownHostException, totalcross.io.IOException
   {
      this(host, port, DEFAULT_OPEN_TIMEOUT, null);
   }

   public Socket4D(String host, int port, int timeout)
         throws totalcross.net.UnknownHostException, totalcross.io.IOException
   {
      this(host, port, timeout, null);
   }

   public Socket4D(String host, int port, int timeout, boolean noLinger) throws totalcross.net.UnknownHostException, totalcross.io.IOException
   {
      this(host, port, timeout, "nolinger="+noLinger);
   }

   public Socket4D(String host, int port, int timeout, String params)
         throws totalcross.net.UnknownHostException, totalcross.io.IOException
   {
      if (port < 0 || port > 65535)
         throw new java.lang.IllegalArgumentException("Invalid value for argument 'port': " + port);
      if (timeout < 0)
         throw new java.lang.IllegalArgumentException("Invalid value for argument 'timeout': " + timeout);

      boolean noLinger = params != null && params.toLowerCase().indexOf("nolinger=true") >= 0;
      this.host = host;
      this.port = port;

      socketCreate(host, port, timeout, noLinger);
      ConnectionManager4D.openConnections.put(this, this); //flsobral@tc123_20: register the newly created connection.
   }

   public void close() throws totalcross.io.IOException
   {
      if (socketRef == null)
         throw new totalcross.io.IOException("The socket is already closed.");
      nativeClose();
      ConnectionManager4D.openConnections.remove(this); //flsobral@tc123_20: unregister the connection after closing it.
   }

   public int readBytes(byte buf[], int start, int count) throws totalcross.io.IOException
   {
      if (socketRef == null)
         throw new totalcross.io.IOException("The socket is closed.");
      if (buf == null)
         throw new NullPointerException();
      if (start < 0 || count < 0 || start + count > buf.length)
         throw new IndexOutOfBoundsException();
      if (count == 0)
         return 0;

      return readWriteBytes(buf, start, count, true);
   }

   public int readBytes(byte buf[]) throws totalcross.io.IOException
   {
      if (socketRef == null)
         throw new totalcross.io.IOException("The socket is closed.");
      if (buf == null)
         throw new java.lang.NullPointerException();
      if (buf.length == 0)
         return 0;

      return readWriteBytes(buf, 0, buf.length, true);
   }

   public int writeBytes(byte buf[], int start, int count) throws totalcross.io.IOException
   {
      if (socketRef == null)
         throw new totalcross.io.IOException("The socket is closed.");
      if (buf == null)
         throw new NullPointerException();
      if (start < 0 || count < 0 || start + count > buf.length)
         throw new IndexOutOfBoundsException();
      if (count == 0)
         return 0;

      return readWriteBytes(buf, start, count, false);
   }

   private byte[] rlbuf;

   public String readLine() throws totalcross.io.IOException
   {
      if (socketRef == null)
         throw new totalcross.io.IOException("The socket is closed.");

      if (rlbuf == null) // guich@tc123_31: no longer static and initialized on first use
         rlbuf = new byte[256];

      byte[] buf = rlbuf;
      int pos = 0;
      int r;
      while ((r = readBytes(buf, pos, 1)) == 1)
      {
         if (buf[pos] == '\n') // guich@tc123_47
         {
            if (pos > 0 && buf[pos-1] == '\r') // cut last \r
               pos--;
            // note that pos must be same of length, otherwise the String will be constructed with one less character
            break;
         }
         if (++pos == buf.length) // reached buffer size?
         {
            byte[] temp = new byte[buf.length+256];
            Vm.arrayCopy(buf, 0, temp, 0, pos);
            rlbuf = buf = temp;
         }
      }
      return (pos > 0 || r == 1) ? new String(buf, 0, pos) : null; // brunosoares@582_11 - charConverter is already used
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
   
   public String getHost()
   {
      return host;
   }
   
   public int getPort()
   {
      return port;
   }
}
