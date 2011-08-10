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

import android.os.*;


// must be called from totalcross.android.Loader thread

public class Level5
{
   private static Level5 instance;
   
   public static Level5 getInstance()
   {
      if (instance == null)
         instance = android.os.Build.VERSION.SDK_INT >= 5 ? new Level5Impl() : new Level5();
      return instance;
   }
   
   public static final int BT_IS_SUPPORTED = 100;
   public static final int BT_ACTIVATE = 101;
   public static final int BT_GET_PAIRED_DEVICES = 102;
   public static final int BT_GET_UNPAIRED_DEVICES = 103;
   public static final int BT_MAKE_DISCOVERABLE = 104;
   public static final int BT_CONNECT = 105;
   public static final int BT_DEACTIVATE = 106;
   public static final int BT_WRITE = 107;
   public static final int BT_READ = 108;
   public static final int BT_CLOSE = 109;
   public static final int BT_IS_RADIO_ON = 110;
   public static final int BT_IS_DISCOVERABLE = 111;
   
   public static boolean isResponseReady;
   protected static boolean responseBoolean;
   protected static Object responseObject;
   
   public void setResponse(boolean b, Object o)
   {
      responseObject = o;
      responseBoolean = b;
      isResponseReady = true;
   }
   
   public static boolean getResponseBoolean()
   {
      isResponseReady = false;
      return responseBoolean;
   }
   
   public static Object getResponseObject()
   {
      isResponseReady = false;
      return responseObject;
   }

   // dumb methods
   public void processMessage(Bundle b) {setResponse(false,null);}
}
