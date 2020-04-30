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

import totalcross.Launcher4A;
import totalcross.android.compat.*;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;

public class RadioDevice4A
{
    // types
    public static final int WIFI = 0;
    public static final int PHONE = 1;
    public static final int BLUETOOTH = 2;

    // generic states
    public static final int RADIO_STATE_DISABLED = 0;
    public static final int RADIO_STATE_ENABLED = 1;

    // bluetooth states
    public static final int BLUETOOTH_STATE_DISCOVERABLE = 2;

   private RadioDevice4A()
   {
    }

   public static boolean isSupported(int type)
   {
      switch (type)
      {
            case WIFI:
                return ConnectivityManager.isNetworkTypeValid(ConnectivityManager.TYPE_WIFI);
            case PHONE:
                return ConnectivityManager.isNetworkTypeValid(ConnectivityManager.TYPE_MOBILE);
            case BLUETOOTH:
                return Bluetooth4A.isSupported() == Level5.NO_ERROR;
            default:
                return false;
        }
    }

   public static int getState(int type)
   {
      switch (type)
      {
         case WIFI:
         {
                WifiManager wifiMgr = (WifiManager) Launcher4A.loader.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                return wifiMgr.isWifiEnabled() ? RADIO_STATE_ENABLED : RADIO_STATE_DISABLED;
            }
         case PHONE:
         {
                return RADIO_STATE_DISABLED;
            }
         case BLUETOOTH:
         {
                boolean isOn = Bluetooth4A.isRadioOn() == Level5.NO_ERROR;
                boolean isDisc = isOn && Bluetooth4A.isDiscoverable() == Level5.NO_ERROR;
                return isDisc ? BLUETOOTH_STATE_DISCOVERABLE : isOn ? RADIO_STATE_ENABLED : RADIO_STATE_DISABLED;
            }
            default:
                return RADIO_STATE_DISABLED;
        }
    }

   public static void setState(int type, int state)
   {
      switch (type)
      {
            case WIFI:
                WifiManager wifiMgr = (WifiManager) Launcher4A.loader.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                wifiMgr.setWifiEnabled(state == RADIO_STATE_ENABLED);
                break;
            case PHONE:
                break;
            case BLUETOOTH:
            switch (state)
            {
               case BLUETOOTH_STATE_DISCOVERABLE: Bluetooth4A.makeDiscoverable(); break;
               case RADIO_STATE_ENABLED         : Bluetooth4A.activate(); break;
               default                          : Bluetooth4A.deactivate(); break;
                }
                break;
        }
    }
}
