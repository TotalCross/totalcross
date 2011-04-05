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



package totalcross.io.device.bluetooth;

import totalcross.io.IOException;
import totalcross.util.Hashtable;

public class DiscoveryAgent4B
{
   javax.bluetooth.DiscoveryAgent nativeInstance;
   Hashtable inquiryListeners = new Hashtable(5);

   public static final int CACHED = 0x00;
   public static final int GIAC = 0x9E8B33;
   public static final int LIAC = 0x9E8B00;
   public static final int NOT_DISCOVERABLE = 0x00;
   public static final int PREKNOWN = 0x01;

   DiscoveryAgent4B()
   {
   }

   DiscoveryAgent4B(javax.bluetooth.DiscoveryAgent nativeInstance)
   {
      this.nativeInstance = nativeInstance;
   }

   public boolean cancelInquiry(DiscoveryListener4B listener)
   {
      if (listener == null)
         throw new NullPointerException();
      javax.bluetooth.DiscoveryListener nativeListener = (javax.bluetooth.DiscoveryListener) inquiryListeners.remove(listener);
      if (nativeListener == null)
         return false;

      return nativeInstance.cancelInquiry(nativeListener);
   }

   public boolean cancelServiceSearch(int transID)
   {
      return nativeInstance.cancelServiceSearch(transID);
   }

   public RemoteDevice4B[] retrieveDevices(int option)
   {
      javax.bluetooth.RemoteDevice[] nativeRemoteDevices = nativeInstance.retrieveDevices(option);
      if (nativeRemoteDevices == null)
         return null;

      RemoteDevice4B[] remoteDevices = new RemoteDevice4B[nativeRemoteDevices.length];
      for (int i = nativeRemoteDevices.length - 1; i >= 0; i--)
         remoteDevices[i] = new RemoteDevice4B(nativeRemoteDevices[i]);

      return remoteDevices;
   }

   public int searchServices(int[] attrSet, UUID4B[] uuidSet, RemoteDevice4B btDev, DiscoveryListener4B listener) throws IOException
   {
      MyDiscoveryListener nativeListener = new MyDiscoveryListener(listener);

      // get native UUID
      javax.bluetooth.UUID[] uuids = new javax.bluetooth.UUID[uuidSet.length];
      for (int i = uuidSet.length - 1; i >= 0; i--)
         uuids[i] = uuidSet[i].nativeInstance;

      try
      {
         return nativeInstance.searchServices(attrSet, uuids, btDev.nativeInstance, nativeListener);
      }
      catch (javax.bluetooth.BluetoothStateException e)
      {
         throw new IOException(e.getMessage());
      }
   }

   public String selectService(UUID4B uuid, int security, boolean master) throws IOException
   {
      try
      {
         return nativeInstance.selectService(uuid.nativeInstance, javax.bluetooth.ServiceRecord.NOAUTHENTICATE_NOENCRYPT, master);
      }
      catch (javax.bluetooth.BluetoothStateException e)
      {
         throw new IOException(e.getMessage());
      }
   }

   public boolean startInquiry(int accessCode, DiscoveryListener4B listener) throws IOException
   {
      MyDiscoveryListener nativeListener = new MyDiscoveryListener(listener);

      // we'll need a way to get to the nativeListener on cancelInquiry
      inquiryListeners.put(listener, nativeListener);

      try
      {
         return nativeInstance.startInquiry(accessCode, nativeListener);
      }
      catch (javax.bluetooth.BluetoothStateException e)
      {
         throw new IOException(e.getMessage());
      }
   }

   class MyDiscoveryListener implements javax.bluetooth.DiscoveryListener
   {
      DiscoveryListener4B tcListener;

      MyDiscoveryListener(DiscoveryListener4B tcListener)
      {
         this.tcListener = tcListener;
      }

      public void deviceDiscovered(javax.bluetooth.RemoteDevice btDevice, javax.bluetooth.DeviceClass cod)
      {
         tcListener.deviceDiscovered(new RemoteDevice4B(btDevice), new DeviceClass4B(cod));
      }

      public void inquiryCompleted(int discType)
      {
         tcListener.inquiryCompleted(discType);
      }

      public void serviceSearchCompleted(int transID, int respCode)
      {
         tcListener.serviceSearchCompleted(transID, respCode);
      }

      public void servicesDiscovered(int transID, javax.bluetooth.ServiceRecord[] servRecord)
      {
         ServiceRecord4B[] serviceRecords = new ServiceRecord4B[servRecord.length];
         for (int i = servRecord.length - 1; i >= 0; i--)
            serviceRecords[i] = new ServiceRecord4B(servRecord[i]);

         tcListener.servicesDiscovered(transID, serviceRecords);
      }
   }
}
