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

import android.os.*;

import totalcross.*;
import totalcross.android.compat.*;

// may be running in tcvm android's thread (static methods in Launcher4A)

public class Bluetooth4A
{
   public static class BTDevice implements Parcelable
   {
      public String name, addr;
      
      public BTDevice(String name, String addr)
      {
         this.name = name;
         this.addr = addr;
      }
      
      public String toString()
      {
         return name + " - " + addr;
      }

      public boolean equals(Object o)
      {
         return toString().equals(o.toString());
      }
      
      // Parcelable interface
      public int describeContents() 
      {
         return 0;
      }

      public void writeToParcel(Parcel out, int flags) 
      {
         out.writeString(name);
         out.writeString(addr);
      }

      public static final Parcelable.Creator<BTDevice> CREATOR = new Parcelable.Creator<BTDevice>() 
      {
         public BTDevice createFromParcel(Parcel in) 
         {
            return new BTDevice(in);
         }

         public BTDevice[] newArray(int size) 
         {
            return new BTDevice[size];
         }
      };
     
      private BTDevice(Parcel in) 
      {
         name = in.readString();
         addr = in.readString();
      }
   }
   
   private static void callLoaderAndWait(int callType, Parcelable param)
   {
      Message msg = Launcher4A.loader.achandler.obtainMessage();
      Bundle b = new Bundle();
      b.putInt("type", Loader.LEVEL5);
      b.putInt("subtype", callType);
      if (param != null)
         b.putParcelable("param",param);
      msg.setData(b);
      Launcher4A.loader.achandler.sendMessage(msg);
      while (!Level5.isResponseReady)
         try {Thread.sleep(100);} catch (Exception e) {}
   }
   
   private static void callLoaderAndWait(int callType)
   {
      callLoaderAndWait(callType,null);
   }

   public static boolean isSupported()
   {
      callLoaderAndWait(Level5.BT_IS_SUPPORTED);
      return Level5.getResponseBoolean();
   }
   
   public static boolean activate()
   {
      callLoaderAndWait(Level5.BT_ACTIVATE);
      return Level5.getResponseBoolean();
   }
   
   public static BTDevice[] getPairedDevices()
   {
      callLoaderAndWait(Level5.BT_GET_PAIRED_DEVICES);
      return (BTDevice[])Level5.getResponseObject();
   }
   
   public static BTDevice[] getUnpairedDevices()
   {
      callLoaderAndWait(Level5.BT_GET_UNPAIRED_DEVICES);
      return (BTDevice[])Level5.getResponseObject();
   }
   
   public static boolean makeDiscoverable()
   {
      callLoaderAndWait(Level5.BT_MAKE_DISCOVERABLE);
      return Level5.getResponseBoolean();
   }

   public static void connectTo(BTDevice dev)
   {
      callLoaderAndWait(Level5.BT_CONNECT,dev);
   }
}
