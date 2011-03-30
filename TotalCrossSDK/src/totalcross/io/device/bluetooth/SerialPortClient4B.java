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



package totalcross.io.device.bluetooth;

import java.io.InputStream;
import java.io.OutputStream;
import totalcross.io.IOException;
import totalcross.io.Stream;

public class SerialPortClient4B extends Stream
{
   Object nativeHandle;

   private InputStream is;
   private OutputStream os;

   public SerialPortClient4B(String address, int port, String[] params) throws IOException
   {
   }

   public SerialPortClient4B(javax.microedition.io.StreamConnection conn) throws IOException
   {
      nativeHandle = conn;
      try
      {
         is = conn.openInputStream();
         os = conn.openOutputStream();
      }
      catch (java.io.IOException e)
      {
         throw new IOException(e.getMessage());
      }
   }

   public int readBytes(byte[] buf, int start, int count) throws IOException
   {
      try
      {
         return is.read(buf, start, count);
      }
      catch (java.io.IOException e)
      {
         throw new IOException(e.getMessage());
      }
   }

   public int writeBytes(byte[] buf, int start, int count) throws IOException
   {
      try
      {
         os.write(buf, start, count);
         return count;
      }
      catch (java.io.IOException e)
      {
         throw new IOException(e.getMessage());
      }
   }

   public void close() throws IOException
   {
      javax.microedition.io.StreamConnection conn = (javax.microedition.io.StreamConnection) nativeHandle;

      try
      {
         is.close();
         os.close();
         conn.close();
      }
      catch (java.io.IOException e)
      {
         throw new IOException(e.getMessage());
      }
      finally
      {
         nativeHandle = null;
      }
   }

   protected void finalize()
   {
      if (nativeHandle != null)
      {
         try
         {
            this.close();
         }
         catch (IOException e)
         {
         }
      }
   }
}
