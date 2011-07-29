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



public class Level5
{
   public static Level5 instance = android.os.Build.VERSION.SDK_INT >= 5 ? new Level5Impl() : new Level5();
   
   public boolean hasBluetooth()
   {
      return false;
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
