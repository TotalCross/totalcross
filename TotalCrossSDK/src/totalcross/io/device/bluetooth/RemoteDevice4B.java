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



package totalcross.io.device.bluetooth;

import totalcross.io.IOException;

public class RemoteDevice4B
{
   String friendlyName;

   String address;

   javax.bluetooth.RemoteDevice nativeInstance;

   protected RemoteDevice4B(String address)
   {
      this.nativeInstance = new nativeRemoteDevice(address);
      this.address = address;
   }

   RemoteDevice4B(javax.bluetooth.RemoteDevice nativeInstance)
   {
      this.nativeInstance = nativeInstance;
   }

   public String getFriendlyName() throws IOException
   {
      try
      {
         if (friendlyName == null)
            friendlyName = nativeInstance.getFriendlyName(true);
         return friendlyName;
      }
      catch (java.io.IOException e)
      {
         throw new IOException(e.getMessage());
      }
   }

   public String getBluetoothAddress()
   {
      if (address == null)
         address = nativeInstance.getBluetoothAddress();
      return address;
   }

   static class nativeRemoteDevice extends javax.bluetooth.RemoteDevice
   {
      public nativeRemoteDevice(String address)
      {
         super(address);
      }
   }
   
   public String toString()
   {
      return address + " - " + friendlyName;
   }   
}
