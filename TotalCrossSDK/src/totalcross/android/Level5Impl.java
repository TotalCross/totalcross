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

package totalcross.android;

import android.bluetooth.*;


public class Level5Impl extends Level5
{
   public boolean hasBluetooth()
   {
      BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
      return bluetoothAdapter != null;
   }
/*   private static ClipboardManager clipboard;
   
   public static String paste()
   {
      if (clipboard == null)
         clipboard = (ClipboardManager)Launcher4A.loader.getSystemService(Context.CLIPBOARD_SERVICE);
      return "";
      
   }
   
   public static void copy(String s)
   {
      ClipData clip = ClipData.newPlainText("simple text","Hello, World!");
   }
*/
   
}
