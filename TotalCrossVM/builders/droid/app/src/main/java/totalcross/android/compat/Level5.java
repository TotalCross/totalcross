// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

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
   public static final int BT_SERVER_ACCEPT = 112;
   public static final int BT_SERVER_CLOSE = 113;

   public static final int ERROR = -999;
   public static final int INVALID_PASSWORD = -998;
   public static final int NO_ERROR = 0;
   
   public static boolean isResponseReady;
   protected static int responseInt;
   protected static Object responseObject;
   
   public void setResponse(boolean b, Object o)
   {
      setResponse(b ? NO_ERROR : ERROR, o);
   }
   
   public void setResponse(int i, Object o)
   {
      responseObject = o;
      responseInt = i;
      isResponseReady = true;
   }
   
   public static int getResponseInt()
   {
      isResponseReady = false;
      return responseInt;
   }
   
   public static Object getResponseObject()
   {
      isResponseReady = false;
      return responseObject;
   }

   // dumb methods
   public void processMessage(Bundle b) {setResponse(-1,null);}
}
