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

// $Id: SerialPortServer4B.java,v 1.4 2011-01-04 13:18:57 guich Exp $

package totalcross.io.device.bluetooth;

import totalcross.io.IOException;
import totalcross.io.Stream;
import totalcross.io.StreamConnectionNotifier;

public class SerialPortServer4B extends StreamConnectionNotifier
{
   Object nativeHandle;

   public SerialPortServer4B(String uuid, String[] params) throws IOException
   {
   }

   public SerialPortServer4B(javax.microedition.io.StreamConnectionNotifier conn)
   {
      nativeHandle = conn;
   }

   public Stream accept() throws IOException
   {
      javax.microedition.io.StreamConnectionNotifier server = (javax.microedition.io.StreamConnectionNotifier) nativeHandle;
      try
      {
         javax.microedition.io.StreamConnection conn = server.acceptAndOpen();
         if (conn != null)
            return new SerialPortClient4B(conn);
         return null;
      }
      catch (java.io.IOException e)
      {
         throw new IOException(e.getMessage());
      }
   }

   public void close() throws IOException
   {
      if (nativeHandle != null)
      {
         javax.microedition.io.StreamConnectionNotifier server = (javax.microedition.io.StreamConnectionNotifier) nativeHandle;
         try
         {
            server.close();
         }
         catch (java.io.IOException e)
         {
            throw new IOException(e.getMessage());
         }
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
