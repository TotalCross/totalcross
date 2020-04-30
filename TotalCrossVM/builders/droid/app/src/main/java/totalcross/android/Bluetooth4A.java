// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.android;

import android.os.*;

import totalcross.*;
import totalcross.android.compat.*;

// may be running in tcvm android's thread (static methods in Launcher4A)

public class Bluetooth4A
{
   private static void callLoaderAndWait(int callType, String param, byte[] array, int ofs, int len)
   {
      Message msg = Launcher4A.loader.achandler.obtainMessage();
      Bundle b = new Bundle();
      b.putInt("type", Loader.LEVEL5);
      b.putInt("subtype", callType);
      if (param != null)
         b.putString("param",param);
      if (array != null)
      {
         b.putByteArray("bytes",array);
         b.putInt("ofs",ofs);
         b.putInt("len",len);
      }
      msg.setData(b);
      Launcher4A.loader.achandler.sendMessage(msg);
      while (!Level5.isResponseReady)
      {
         try {Thread.sleep(10);} catch (Exception e) {}
         if (Launcher4A.eventIsAvailable())
            Launcher4A.pumpEvents();
      }
   }
   
   private static void callLoaderAndWait(int callType, String param)
   {
      callLoaderAndWait(callType,param, null,0,0);
   }

   private static void callLoaderAndWait(int callType)
   {
      callLoaderAndWait(callType,null,null,0,0);
   }

   // used to control bluetooth hardware
   
   public static int isSupported()
   {
      callLoaderAndWait(Level5.BT_IS_SUPPORTED);
      return Level5.getResponseInt();
   }
   
   public static int isRadioOn()
   {
      callLoaderAndWait(Level5.BT_IS_RADIO_ON);
      return Level5.getResponseInt();
   }
   
   public static int isDiscoverable()
   {
      callLoaderAndWait(Level5.BT_IS_DISCOVERABLE);
      return Level5.getResponseInt();
   }
   
   public static int activate()
   {
      callLoaderAndWait(Level5.BT_ACTIVATE);
      return Level5.getResponseInt();
   }
   
   public static int deactivate()
   {
      callLoaderAndWait(Level5.BT_DEACTIVATE);
      return Level5.getResponseInt();
   }
   
   public static String[] getPairedDevices()
   {
      callLoaderAndWait(Level5.BT_GET_PAIRED_DEVICES);
      return (String[])Level5.getResponseObject();
   }
   
   public static String[] getUnpairedDevices()
   {
      callLoaderAndWait(Level5.BT_GET_UNPAIRED_DEVICES);
      return (String[])Level5.getResponseObject();
   }
   
   public static int makeDiscoverable()
   {
      callLoaderAndWait(Level5.BT_MAKE_DISCOVERABLE);
      return Level5.getResponseInt();
   }

   public static int connectTo(String addr)
   {
      callLoaderAndWait(Level5.BT_CONNECT,addr);
      return Level5.getResponseInt();
   }
   
   // used for a specific connection
   
   public static void close(String addr)
   {
      callLoaderAndWait(Level5.BT_CLOSE, addr);
   }
   
   public static int read(String addr, byte[] array, int ofs, int len)
   {
      callLoaderAndWait(Level5.BT_READ, addr, array, ofs, len);
      return Level5.getResponseInt();
   }
   
   public static int write(String addr, byte[] array, int ofs, int len)
   {
      callLoaderAndWait(Level5.BT_WRITE, addr, array, ofs, len);
      return Level5.getResponseInt();
   }
   
   // server methods
   
   public static int serverAccept(String uuid)
   {
      callLoaderAndWait(Level5.BT_SERVER_ACCEPT, uuid);
      return Level5.getResponseInt();
   }

   public static void serverClose()
   {
      callLoaderAndWait(Level5.BT_SERVER_CLOSE);
   }
}
