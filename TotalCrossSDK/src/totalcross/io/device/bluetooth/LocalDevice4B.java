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
import totalcross.io.StreamConnectionNotifier;

public class LocalDevice4B
{
   private static LocalDevice4B instance;

   static DiscoveryAgent4B discoveryAgent;
   static DeviceClass4B deviceClass;

   static javax.bluetooth.LocalDevice nativeInstance;

   private LocalDevice4B() throws IOException
   {
      try
      {
         nativeInstance = javax.bluetooth.LocalDevice.getLocalDevice();
         deviceClass = new DeviceClass4B(nativeInstance.getDeviceClass());
         discoveryAgent = new DiscoveryAgent4B(nativeInstance.getDiscoveryAgent());
      }
      catch (javax.bluetooth.BluetoothStateException e)
      {
         throw new IOException(e.getMessage());
      }
   }

   public String getBluetoothAddress()
   {
      return nativeInstance.getBluetoothAddress();
   }

   public DeviceClass4B getDeviceClass()
   {
      return deviceClass;
   }

   public int getDiscoverable()
   {
      return nativeInstance.getDiscoverable();
   }

   public DiscoveryAgent4B getDiscoveryAgent()
   {
      return discoveryAgent;
   }

   public String getFriendlyName()
   {
      return nativeInstance.getFriendlyName();
   }

   public static LocalDevice4B getLocalDevice() throws IOException
   {
      if (instance == null)
         instance = new LocalDevice4B();
      return instance;
   }

   public static String getProperty(String property)
   {
      return null;
   }

   public static boolean isPowerOn()
   {
      return false; // not implemented yet.
   }

   public boolean setDiscoverable(int mode) throws IOException
   {
      if ((mode != DiscoveryAgent.GIAC) && (mode != DiscoveryAgent.LIAC) && (mode != DiscoveryAgent.NOT_DISCOVERABLE)
            && (mode < 0x9E8B00 || mode > 0x9E8B3F))
         throw new IllegalArgumentException("Invalid discoverable mode");
      return false; // not implemented yet
   }

   public void updateRecord(ServiceRecord srvRecord)
   {
   }

   public ServiceRecord getRecord(StreamConnectionNotifier notifier)
   {
      if (notifier == null)
         throw new NullPointerException();
      if (!(notifier instanceof totalcross.io.device.bluetooth.SerialPortServer))
         throw new IllegalArgumentException();
      return null;
   }
}
