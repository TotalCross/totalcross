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



package totalcross.io;

import totalcross.io.device.bluetooth.SerialPortClient4B;
import totalcross.io.device.bluetooth.SerialPortServer4B;

public class Connector4B
{
   public static Connection open(String url) throws IOException
   {
      try
      {
         javax.microedition.io.Connection conn = javax.microedition.io.Connector.open(url);
         if (url.startsWith("btspp") && conn != null)
         {
            if (conn instanceof javax.microedition.io.StreamConnectionNotifier) // server
               return new SerialPortServer4B((javax.microedition.io.StreamConnectionNotifier) conn);
            else
               return new SerialPortClient4B((javax.microedition.io.StreamConnection) conn);
         }
         return null;
      }
      catch (java.io.IOException e)
      {
         throw new IOException(e.getMessage());
      }
   }
}
