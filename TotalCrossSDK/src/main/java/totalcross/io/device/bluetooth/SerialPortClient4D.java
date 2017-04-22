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

public class SerialPortClient4D extends Stream
{
   Object nativeHandle;

   public SerialPortClient4D(String address, int port, String[] params) throws IOException
   {
      createSerialPortClient(address, port, params);
   }

   native private void createSerialPortClient(String address, int port, String[] params) throws IOException;

   native public int readBytes(byte[] buf, int start, int count) throws IOException;

   native public int writeBytes(byte[] buf, int start, int count) throws IOException;

   native public void close() throws IOException;

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
