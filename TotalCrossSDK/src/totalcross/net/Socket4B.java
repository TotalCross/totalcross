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

import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.SocketConnection;

import net.rim.device.api.applicationcontrol.ApplicationPermissions;
import net.rim.device.api.io.SocketConnectionEnhanced;
import totalcross.Launcher4B;
import totalcross.io.IOException;
import totalcross.io.Stream;
import totalcross.net.ConnectionManager4B.SocketConnectionHolder;
import totalcross.sys.Convert;
import totalcross.sys.Vm;
import totalcross.util.Logger;

public class Socket4B extends Stream
{
   public int openTimeout;
   public int readTimeout = DEFAULT_READ_TIMEOUT;
   public int writeTimeout = DEFAULT_WRITE_TIMEOUT;
   
   Object socketRef;
   public SocketConnection conn;
   public String url;
   
   private String host;
   private int port;
   private InputStream is;
   private OutputStream os;
   private boolean isEnhancedConn;
   private int lastTimeout;
   private byte[] firstRead = new byte[1];
   boolean dontFinalize;

   private static Logger logger = Logger.getLogger("totalcross.net", Logger.ALL);
   
   public static final int DEFAULT_OPEN_TIMEOUT = 2000;
   public static final int DEFAULT_READ_TIMEOUT = 5000;
   public static final int DEFAULT_WRITE_TIMEOUT = 2000;
   
   static
   {
      Launcher4B.requestAppPermissions(new int[] { ApplicationPermissions.PERMISSION_EXTERNAL_CONNECTIONS, ApplicationPermissions.PERMISSION_WIFI });
   }

   protected Socket4B()
   {
   }

   public Socket4B(String host, int port) throws UnknownHostException, IOException
   {
      this(host, port, DEFAULT_OPEN_TIMEOUT, null);
   }

   public Socket4B(String host, int port, int timeout) throws UnknownHostException, IOException
   {
      this(host, port, timeout, null);
   }

   public Socket4B(String host, int port, int timeout, boolean noLinger) throws totalcross.net.UnknownHostException, totalcross.io.IOException
   {
      this(host, port, timeout, null); //flsobral@tc115_88: ignore noLinger on BlackBerry. It wasn't being used anyway.
   }

   public Socket4B(String host, int port, int timeout, String params) throws UnknownHostException, IOException
   {
      this.host = host;
      this.port = port;
      this.openTimeout = timeout;

      url = "socket://" + host + ":" + port;
      params = ConnectionManager4B.parseParameters(params);
      
      final boolean useConnMgr = params == null || params.length() == 0;
      if (!useConnMgr)
      {
         if (!ConnectionManager4B.getParameterValue(params, "deviceside", "false").equalsIgnoreCase("true"))
            url += ";ConnectionTimeout=" + openTimeout;
         url += ";" + params;
      }
      
      final Exception[] innerEx = new Exception[1];
      conn = null;
      
      Thread opener = new Thread()
      {
         public void run()
         {
            try
            {
               if (useConnMgr)
               {
                  SocketConnectionHolder connHolder = ConnectionManager4B.getConnection(url, openTimeout);
                  
                  url = connHolder.getURL(); // URL may have changed
                  conn = connHolder.getConnection();
                  is = connHolder.getInputStream();
                  os = connHolder.getOutputStream();
                  
                  logger.info("Socket opened at " + url + " (" + connHolder.getTransportName() + ", " + conn.getLocalAddress() + ":" + conn.getLocalPort() + ")");
               }
               else
               {
                  conn = (SocketConnection)Connector.open(url, Connector.READ_WRITE, false);
                  is = conn.openInputStream();
                  os = conn.openOutputStream();
                  
                  logger.info("Socket opened at " + url + " (" + conn.getLocalAddress() + ":" + conn.getLocalPort() + ")");
               }
               
               try
               {
                  if ((isEnhancedConn = conn instanceof SocketConnectionEnhanced))
                     ((SocketConnectionEnhanced)conn).setSocketOptionEx(SocketConnectionEnhanced.READ_TIMEOUT, lastTimeout = 500);
                     
                  Launcher4B.readNonBlocking(is, 500, firstRead, 0, 1); // needed to force the socket to be opened in some weird situations
               }
               catch (SocketTimeoutException ex)
               {
                  firstRead = null; // nothing was read
               }
            }
            catch (InterruptedIOException ex)
            {
            }
            catch (Exception ex)
            {
               innerEx[0] = ex;
            }
         }
      };

      opener.start();
      long t = System.currentTimeMillis();
      while (opener.isAlive() && (System.currentTimeMillis() - t) <= timeout)
      {
         try
         {
            Thread.sleep(100);
         }
         catch (InterruptedException ex) {}
      }

      if (opener.isAlive()) // timeout! interrupt opener and wait until it finishes
      {
         opener.interrupt();
         try
         {
            opener.join();
         }
         catch (InterruptedException ex) {}
      }
      
      Exception ex = innerEx[0];
      if (ex != null) // Exception inside opener
      {
         if (ex instanceof RuntimeException)
            throw (RuntimeException)ex;
         else
            throw new IOException(ex.getMessage());
      }
      else if (conn == null) // Timeout
         throw new SocketTimeoutException("Connection timed out");
      else // Connection opened
         socketRef = conn;
   }
   
   public String getHost()
   {
      return host;
   }
   
   public int getPort()
   {
      return port;
   }
   
   public Object getNativeSocket()
   {
      return null;
   }

   public void close() throws IOException
   {
      if (socketRef == null)
         throw new IOException("The socket is already closed.");

      try
      {
         is.close();
      }
      catch (java.io.IOException ex)
      {
      }
      
      try
      {
         os.close();
      }
      catch (java.io.IOException ex)
      {
      }
      
      try
      {
         ((SocketConnection)socketRef).close();
      }
      catch (java.io.IOException ex)
      {
         throw new IOException(ex.getMessage());
      }
      finally
      {
         socketRef = null;
         dontFinalize = false;
      }
   }

   public int readBytes(byte buf[], int start, int count) throws IOException
   {
      if (socketRef == null)
         throw new IOException("The socket is closed.");
      if (buf == null)
         throw new NullPointerException();
      if (start < 0 || count < 0 || start + count > buf.length)
         throw new IndexOutOfBoundsException();
      if (count == 0)
         return 0;
      
      try
      {
         int r = 0;
         if (firstRead != null)
         {
            System.arraycopy(firstRead, 0, buf, start++, 1);
            firstRead = null;
            
            r = 1;
         }
         
         if ((count - r) > 0)
         {
            if (isEnhancedConn && readTimeout != lastTimeout)
               ((SocketConnectionEnhanced)conn).setSocketOptionEx(SocketConnectionEnhanced.READ_TIMEOUT, lastTimeout = readTimeout);
            
            r += Launcher4B.readNonBlocking(is, readTimeout, buf, start, count - r);
         }
         
         logger.finest("Socket.readBytes returning " + r + " bytes read (requested " + count + " bytes)");
         return r;
      }
      catch (java.io.IOException ex)
      {
         /* //flsobral@tc124_19
          * Some devices (namely model 8350i - NEXTEL) throw an IOException with the message 
          * "Connection closed" instead of returning -1 as it would be expected.
          * 
          * This behavior might be specific to this device or to the iDEN network.  
          */
         if ("Connection closed".equals(ex.getMessage()))
            return -1;      
         throw new IOException(ex.getMessage());
      }
   }
   
   public int readBytes(byte buf[]) throws IOException
   {
      return readBytes(buf, 0, buf.length);
   }

   public int writeBytes(byte buf[], int start, int count) throws IOException
   {
      if (socketRef == null)
         throw new IOException("The socket is closed.");
      if (buf == null)
         throw new NullPointerException();
      if (start < 0 || count < 0 || start + count > buf.length)
         throw new IndexOutOfBoundsException();
      if (count == 0)
         return 0;

      try
      {
         if (isEnhancedConn && writeTimeout != lastTimeout)
            ((SocketConnectionEnhanced)conn).setSocketOptionEx(SocketConnectionEnhanced.READ_TIMEOUT, lastTimeout = writeTimeout);
            
         os.write(buf, start, count);
         os.flush();

         logger.finest("Socket.writeBytes returning " + count + " bytes written (requested " + count + " bytes)");
         return count;
      }
      catch (java.io.IOException ex)
      {
         throw new IOException(ex.getMessage());
      }
   }

   private byte[] rlbuf;

   public String readLine() throws totalcross.io.IOException
   {
      if (socketRef == null)
         throw new IOException("The socket is closed.");

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
            buf = temp;
         }
      }
      return (pos > 0 || r == 1) ? new String(Convert.charConverter.bytes2chars(buf, 0, pos)) : null; // brunosoares@582_11
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
