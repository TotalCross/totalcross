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



package totalcross.io;

import totalcross.io.device.bluetooth.*;
import totalcross.sys.*;

/**
 * Used to open socket connections over CRADLE, WIFI, MDS, GPRS.
 */
public class Connector
{
   /**
    * Opens a new socket to the given url. The socket will be opened using the first available network connection in the
    * following order: CRADLE, WIFI, MDS, GPRS.
    * 
    * @param url
    *           The socket opened to the given url and network connection.
    * @return The socket opened to the given url and network connection.
    */
   public static Connection open(String url) throws IOException, InvalidNumberException
   {
      // e.g.: "btspp://0050CD00321B:3;authenticate=true;encrypt=false;master=true"
      int targetStart = url.indexOf(':');
      int targetEnd = url.indexOf(';');
      String scheme = url.substring(0, targetStart);
      String target;
      String[] params = null;

      if (targetEnd == -1)
         target = url.substring(targetStart + 1);
      else
      {
         target = url.substring(targetStart + 1, targetEnd);
         params = Convert.tokenizeString(url.substring(targetEnd + 1), ';');
      }

      if (scheme.equals("btspp"))
      {
         // handle bluetooth connection
         int targetColon = target.indexOf(':');
         String address = target.substring(2, targetColon);

         if (address.length() == 0 || address.equals("localhost"))
         {
            // start server
            String uuid = target.substring(targetColon + 1);
            return new SerialPortServer(uuid, params);
         }
         else
         {
            String port = target.substring(targetColon + 1);
            return new SerialPortClient(address, Convert.toInt(port), params);
         }
      }
      else
      {
         // not supported
      }

      return null;
   }
}
