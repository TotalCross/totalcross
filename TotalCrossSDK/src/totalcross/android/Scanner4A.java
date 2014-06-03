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

package totalcross.android;

import totalcross.android.scanners.*;

/**
 * Scanner class for Android.
 */
public class Scanner4A
{ 
   public static IScanner scanner;
   
   static boolean scannerActivate()
   {
      String id = Settings4A.deviceId.toLowerCase();
      if (id.equalsIgnoreCase("intermec") || id.contains("foxconn"))
         scanner = new IntermecScanner();
      else
      if (id.contains("motorola"))
         scanner = new MotorolaScanner();
      return scanner.scannerActivate();
   }
   
   static boolean setBarcodeParam(int barcodeType, boolean enable)
   {
      return scanner != null && scanner.setBarcodeParam(barcodeType, enable);
   }
   
   static String getData()
   {
      return scanner == null ? null : scanner.getData();
   }
   
   static boolean deactivate()
   {
      return scanner != null && scanner.deactivate();
   }
}
