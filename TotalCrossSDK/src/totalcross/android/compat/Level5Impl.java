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

import totalcross.*;
import totalcross.android.*;

import java.util.*;

import android.bluetooth.*;
import android.content.*;
import android.os.*;


public class Level5Impl extends Level5
{
   BluetoothAdapter bluetoothAdapter;
   
   public Level5Impl()
   {
      callLoaderAndWait(CREATE_BLUETOOTH);
   }
   
   //// util methods
   
   private void callLoaderAndWait(int callType)
   {
      Message msg = Launcher4A.loader.achandler.obtainMessage();
      Bundle b = new Bundle();
      b.putInt("type",callType);
      msg.setData(b);
      Launcher4A.loader.achandler.sendMessage(msg);
      while (!responseReady)
         try {Thread.sleep(250);} catch (Exception e) {}
   }

   public void createBluetoothAdapter(Loader loader)
   {
      bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
   }
   
   // IS SUPPORTED
   
   public boolean btIsSupported()
   {
      return bluetoothAdapter != null;
   }
   
   // ACTIVATE
   
   public boolean btActivate()
   {
      if (!bluetoothAdapter.isEnabled())
      {
         callLoaderAndWait(REQUEST_ENABLE_BT);
         return responseBoolean;
      }
      return true;
   }
   
   public void btActivateCall(Loader loader)
   {
      loader.startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BT);
   }

   // PAIRED AND UNPAIRED DEVICES
   
   Vector<BTDevice> devices = new Vector<BTDevice>(10);
   
   public BTDevice[] btGetPairedDevices()
   {
      devices.removeAllElements();
      Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
      // If there are paired devices
      if (pairedDevices.size() > 0) 
          for (BluetoothDevice device : pairedDevices)
             devices.add(new BTDevice(device.getName(), device.getAddress()));
      return devices.size() == 0 ? null : (BTDevice[]) devices.toArray();
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
            devices.add(new BTDevice(device.getName(), device.getAddress()));
         }
         else
         if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
            responseReady = true;
      }
   };

   public BTDevice[] getUnpairedDevices()
   {
      callLoaderAndWait(GET_UNPAIRED_DEVICES);
      return devices.size() == 0 ? null : (BTDevice[]) devices.toArray();
   }
   
   public void getUnpairedDevicesCall(Loader loader)
   {
      IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
      loader.registerReceiver(mReceiver, filter);
   }
   
   public void destroy()
   {
      Launcher4A.loader.unregisterReceiver(mReceiver);
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
