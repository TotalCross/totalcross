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

package totalcross.android.compat;

import totalcross.android.*;


public class Level5
{
   public static Level5 instance = android.os.Build.VERSION.SDK_INT >= 5 ? new Level5Impl() : new Level5();
   
   public static class BTDevice
   {
      String name, addr;
      
      public BTDevice(String name, String addr)
      {
         this.name = name;
         this.addr = addr;
      }
      
      public String toString()
      {
         return name + " - " + addr;
      }
   }
   
   public boolean responseReady;
   public boolean responseBoolean;
   
   public Object btAdapter;
   
   public static final int REQUEST_ENABLE_BT = 100;
   public static final int GET_UNPAIRED_DEVICES = 101;
   public static final int CREATE_BLUETOOTH = 102;
   
   public void setResponseBoolean(boolean b)
   {
      responseBoolean = b;
      responseReady = true;
   }

   // dumb methods
   
   public boolean btIsSupported() {return false;}
   public boolean btActivate() {return false;}
   public void btActivateCall(Loader loader) {}
   public BTDevice[] btGetPairedDevices() {return null;}
   public BTDevice[] getUnpairedDevices() {return null;}
   public void getUnpairedDevicesCall(Loader loader) {}
   public void createBluetoothAdapter(Loader loader) {}
   public void destroy() {}
}
