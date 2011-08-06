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

import android.bluetooth.*;
import android.content.*;
import android.os.*;
import java.io.*;
import java.util.*;

import totalcross.*;
import totalcross.android.Bluetooth4A.BTDevice;

public class Level5Impl extends Level5
{
   //private static final UUID MY_UUID  = UUID.fromString("e3b9f92c-3226-4ccb-9e0a-218695bd402c");
   private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
   BluetoothAdapter btAdapter;
   
   public Level5Impl()
   {
      btAdapter = BluetoothAdapter.getDefaultAdapter();
   }

   public void processMessage(Bundle b) 
   {
      switch (b.getInt("subtype"))
      {
         case BT_IS_SUPPORTED:
            setResponse(btAdapter != null, null);
            break;
         case BT_ACTIVATE:
            if (!btAdapter.isEnabled())
               Launcher4A.loader.startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), BT_ACTIVATE);
            else
               setResponse(true,null);
            break;
         case BT_GET_UNPAIRED_DEVICES:
            btGetUnpairedDevices();
            break;
         case BT_GET_PAIRED_DEVICES:
            btGetPairedDevices();
            break;
         case BT_MAKE_DISCOVERABLE:
            btMakeDiscoverable();
            break;
         case BT_CONNECT:
            btConnect((BTDevice)b.getParcelable("param"));
      }
   }      

   private String getBTClassServ(BluetoothClass btc) 
   {
      if (btc == null) return "no information available";
      String temp = "Class: "+Integer.toHexString(btc.getDeviceClass())+"/"+Integer.toHexString(btc.getMajorDeviceClass())+": ";
      if (btc.hasService(BluetoothClass.Service.AUDIO))
         temp += "Audio, ";
      if (btc.hasService(BluetoothClass.Service.TELEPHONY))
         temp += "Telophony, ";
      if (btc.hasService(BluetoothClass.Service.INFORMATION))
         temp += "Information, ";
      if (btc.hasService(BluetoothClass.Service.LIMITED_DISCOVERABILITY))
         temp += "Limited Discoverability, ";
      if (btc.hasService(BluetoothClass.Service.NETWORKING))
         temp += "Networking, ";
      if (btc.hasService(BluetoothClass.Service.OBJECT_TRANSFER))
         temp += "Object Transfer, ";
      if (btc.hasService(BluetoothClass.Service.POSITIONING))
         temp += "Positioning, ";
      if (btc.hasService(BluetoothClass.Service.RENDER))
         temp += "Render, ";
      if (btc.hasService(BluetoothClass.Service.CAPTURE))
         temp += "Capture, ";
      // trim off the extra comma and space
      temp = temp.substring(0, temp.length() - 2);
      return temp;
   }      

   private void btConnect(BTDevice btd)
   {
      BluetoothSocket sock = null;
      try
      {
         if (btAdapter.isDiscovering()) btAdapter.cancelDiscovery();
         AndroidUtils.debug("btConnect to "+btd);
         BluetoothDevice device = btAdapter.getRemoteDevice(btd.addr);
         AndroidUtils.debug(getBTClassServ(device.getBluetoothClass()));
         AndroidUtils.debug("Creating rfcomm...");
         sock = device.createRfcommSocketToServiceRecord(SPP_UUID);
         AndroidUtils.debug("Sock returned. Connecting");
         sock.connect();
         AndroidUtils.debug("Connected!");
         OutputStream os = sock.getOutputStream();
         AndroidUtils.debug("Streams retrieved");
         byte[] by = "! 0 200 200 210 1\r\nTEXT 4 0 30 40 Gui S2 Nak\r\nFORM\r\nPRINT\r\n".getBytes();
         os.write(by);
         AndroidUtils.debug("Written");
         os.close();
         sock.close();
         AndroidUtils.debug("Everything closed");
      }
      catch (IOException e)
      {
         AndroidUtils.debug("Connection failed. Exception:");
         e.printStackTrace();
      }
      if (sock != null) try {sock.close();} catch (Exception e) {}
      setResponse(true,null);
   }

   private void btMakeDiscoverable()
   {
      if (btAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) 
      {
         Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
         discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
         Launcher4A.loader.startActivityForResult(discoverableIntent, BT_MAKE_DISCOVERABLE);
      }
      else setResponse(true,null);
   }

   Vector<BTDevice> devices = new Vector<BTDevice>(10);
   
   private void btGetPairedDevices()
   {
      devices.removeAllElements();
      Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
      // If there are paired devices
      if (pairedDevices.size() > 0) 
          for (BluetoothDevice device : pairedDevices)
             devices.add(new BTDevice(device.getName(), device.getAddress()));
      BTDevice[] ret = null;
      if (devices.size() > 0) devices.copyInto(ret = new BTDevice[devices.size()]);
      setResponse(true,ret);
   }
   
   private final BroadcastReceiver mReceiver = new BroadcastReceiver() 
   {
      public void onReceive(Context context, Intent intent) 
      {
         String action = intent.getAction();
         // When discovery finds a device
         if (BluetoothDevice.ACTION_FOUND.equals(action)) 
         {
            // Get the BluetoothDevice object from the Intent
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            BTDevice btd = new BTDevice(device.getName(), device.getAddress());
            if (btd.name != null && !devices.contains(btd))
               devices.add(btd);
         }
         else
         if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
         {
            Launcher4A.loader.unregisterReceiver(this);
            BTDevice[] ret = null;
            if (devices.size() > 0) devices.copyInto(ret = new BTDevice[devices.size()]);
            setResponse(true,ret);
         }
      }
   };

   private void btGetUnpairedDevices()
   {
      devices.removeAllElements();
      Launcher4A.loader.registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
      Launcher4A.loader.registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
      btAdapter.startDiscovery();
   }
   
   public void destroy()
   {
   }
}
