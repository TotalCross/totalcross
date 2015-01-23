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



package totalcross.io.device.bluetooth;

import totalcross.io.IOException;
import totalcross.io.Stream;
import totalcross.io.StreamConnectionNotifier;

public class SerialPortServer extends StreamConnectionNotifier
{
   Object nativeHandle;

   public SerialPortServer(String uuid, String[] params) throws IOException
   {
      createSerialPortServer(uuid, params);
   }

   private void createSerialPortServer(String uuid, String[] params) throws IOException
   {
   }

   public Stream accept() throws IOException
   {
      return null;
   }

   public void close() throws IOException
   {
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
