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

import android.content.*;
import android.content.res.*;
import android.net.wifi.*;
import android.os.*;
import android.provider.*;
import android.telephony.*;
import android.content.pm.PackageManager;
import java.lang.reflect.*;
import java.util.*;
import java.net.NetworkInterface;
import android.support.v4.content.ContextCompat;
import android.support.v4.app.ActivityCompat;
import android.Manifest;

import totalcross.*;

public final class Settings4A
{
   public static final byte DATE_MDY = 1;
   public static final byte DATE_DMY = 2;
   public static final byte DATE_YMD = 3;   
   
   // date format
   public static byte dateFormat;
   public static char dateSeparator;
   public static byte weekStart;

   // time format
   public static boolean is24Hour;
   public static char timeSeparator;

   // number format
   public static char thousandsSeparator;
   public static char decimalSeparator;

   // graphics
   public static int screenWidth;
   public static int screenHeight;
   public static int screenBPP;
   public static double screenDensity;
   
   // platform
   public static String deviceId;
   public static int romVersion;
   
   // locale
   public static boolean daylightSavings;
   public static int timeZone;
   public static String timeZoneStr;
   
   // identification
   public static String userName;
   public static String imei,imei2;
   public static String esn;   
   public static String iccid;
   public static String serialNumber;
   public static String macAddress;
   public static String ANDROID_ID;

   // device capabilities
   public static boolean virtualKeyboard;
   public static boolean keypadOnly;
   
   public static String lineNumber;
   
   public static int buildNumber = 000;
   
   private static boolean telephonyInitialized = false;

   public static void refresh()
   {
      settingsRefresh();
   }
   
	// this class can't be instantiated
	private Settings4A()
	{
	   
	}
	
	public static String getMacAddr() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) {
                    continue;
                }
 
                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }
 
                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(Integer.toHexString(b & 0xFF) + ":");
                }
 
                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
        }
        return "02:00:00:00:00:00";
    }
	
	static void fillSettings()
	{
	   Context ctx = Launcher4A.instance.getContext();
	   
      // platform
      String v = Build.VERSION.RELEASE;
      // guich@tc200: java was rounding 2.3 to 2.29...
      if (v.length() == 5 && v.charAt(1) == '.' && v.charAt(3) == '.') // 2.3.4
         romVersion = Integer.parseInt(v.replace(".",""));
      else
      if (v.length() == 3 && v.charAt(1) == '.') // 2.3
         romVersion = Integer.parseInt(v.replace(".","")) * 10; // 23 * 10 = 230
      else
      {      
         double vd = 0;
         while (vd == 0 && v.length() > 0)
            try
            {
               vd = Double.valueOf(v);            
               vd += 0.005; // round up
            }
            catch (Exception e)
            {
               v = v.substring(0,v.length()-1);
            }
         romVersion = (int)vd * 100 + ((int)(vd * 100)) % 100; // 3.16
      }
      deviceId = Build.MANUFACTURER.replaceAll("\\P{ASCII}", " ") + " " + Build.MODEL.replaceAll("\\P{ASCII}", " ");
      
      // userName
      userName = null; // still looking for a way to retrieve this on droid.	   
	   
      fillTelephonySettings();
      while (!telephonyInitialized) {
    	  try {Thread.sleep(10);} catch (Exception e) {}
      }
      
      // if using a new device, get its serial number. otherwise, create one from the mac-address
      if (romVersion >= 9) // gingerbread
         try
         {
            Class<Build> c = android.os.Build.class;
            Field f = c.getField("SERIAL");
            serialNumber = (String)f.get(null);
         }
         catch (NoSuchFieldError nsfe) {}
         catch (Throwable t) {}

      if (!Loader.IS_EMULATOR)
      {
         WifiManager wifiMan = (WifiManager) ctx.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
         if (wifiMan != null) // not sure what happens when device has no connectivity at all
         {
            macAddress = wifiMan.getConnectionInfo().getMacAddress();
            if (macAddress == null) // if wifi never turned on since last boot, turn it on and off to be able to get the mac (on android, the mac is cached by the O.S.)
            {
               wifiMan.setWifiEnabled(true);
               while (!wifiMan.isWifiEnabled()) // wait until its active
                  try {Thread.sleep(10);} catch (Exception e) {}
               wifiMan.setWifiEnabled(false);
               macAddress = wifiMan.getConnectionInfo().getMacAddress();
            }
            
            // Work around for Android 6 mac address, although not reliable...
            if ("02:00:00:00:00:00".equals(macAddress)) {
            	macAddress = getMacAddr();
            }
         }
      }

      if ((serialNumber == null || "unknown".equalsIgnoreCase(serialNumber)) && !Loader.IS_EMULATOR && macAddress != null) // no else here!
         serialNumber = String.valueOf(((long)macAddress.replace(":","").hashCode() & 0xFFFFFFFFFFFFFFL));
      
      ANDROID_ID = Settings.Secure.getString(Launcher4A.loader.getContentResolver(), Settings.Secure.ANDROID_ID);
      
      // virtualKeyboard
      virtualKeyboard = true; // always available on droid?
      
      // number representation
      java.text.DecimalFormatSymbols dfs = new java.text.DecimalFormatSymbols();
      thousandsSeparator = dfs.getGroupingSeparator(); if (thousandsSeparator <= ' ') thousandsSeparator = ',';
      decimalSeparator = dfs.getDecimalSeparator();	 if (decimalSeparator <= ' ') decimalSeparator = '.';
	   
      // date representation
      java.util.Calendar calendar = java.util.Calendar.getInstance();
      calendar.set(2002,11,25,20,0,0);
      java.text.DateFormat df = java.text.DateFormat.getDateInstance(java.text.DateFormat.SHORT);
      String d = df.format(calendar.getTime());
      dateFormat = d.startsWith("25") ? DATE_DMY
                                   : d.startsWith("12") ? DATE_MDY
                                   : DATE_YMD;

      dateSeparator = getFirstSymbol(d);
      
      weekStart = (byte) (calendar.getFirstDayOfWeek() - 1);
      
      // time representation
      df = java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT); // guich@401_32
      d = df.format(calendar.getTime());
      timeSeparator = getFirstSymbol(d);
      is24Hour = d.toLowerCase().indexOf("am") == -1 && d.toLowerCase().indexOf("pm") == -1;
      
      // graphics
      screenDensity = Launcher4A.instance.getResources().getDisplayMetrics().density;
          
      settingsRefresh();
	   
	   if (!Loader.IS_EMULATOR) // running on emulator, right now there's no way to retrieve more settings from it.
	   {
	      ContentResolver cr = Launcher4A.loader.getContentResolver();
	      
	      Configuration config = new Configuration();
	      Settings.System.getConfiguration(cr, config);
	      if (config.keyboard == Configuration.KEYBOARD_12KEY)
	         keypadOnly = true;
	      
	      // is24Hour
	      is24Hour = Settings.System.getInt(cr, Settings.System.TIME_12_24, is24Hour ? 24 : 12) == 24;
	      
	      // date format
	      String format = Settings.System.getString(cr, Settings.System.DATE_FORMAT);
	      if (format != null && format.length() > 0)
	      {
	         char firstChar = format.charAt(0);
	         dateFormat = firstChar == 'd' ? DATE_DMY
                  : firstChar == 'M' ? DATE_MDY
                  : DATE_YMD;
	      }
	   }
	}
	
   public static int timeZoneMinutes;
   public static int daylightSavingsMinutes;
   public static void settingsRefresh()
   {
      java.util.TimeZone tz = java.util.TimeZone.getDefault();
      Calendar cal = Calendar.getInstance();
      int dls = cal.get(Calendar.DST_OFFSET);
      daylightSavingsMinutes = dls / 60000;
      daylightSavings = daylightSavingsMinutes != 0;
      timeZone = tz.getRawOffset() / (60*60000);
      timeZoneMinutes = tz.getRawOffset() / 60000;
      timeZoneStr = java.util.TimeZone.getDefault().getID();
   }
   
   private static char getFirstSymbol(String s)
   {
      char []c = s.toCharArray();
      for (int i =0; i < c.length; i++)
         if (c[i] != ' ' && !('0' <= c[i] && c[i] <= '9'))
            return c[i];
      return ' ';
   }

    public static void fillTelephonySettings() {
        String id1, id2;
        // imei
        TelephonyManager telephonyMgr = (TelephonyManager) Launcher4A.loader
                .getSystemService(Context.TELEPHONY_SERVICE);
        try {
            lineNumber = telephonyMgr.getLine1Number();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        // handle dual-sim phones. Usually, they overload the method with a
        Class<? extends TelephonyManager> cc = telephonyMgr.getClass();
        Method[] mtds = cc.getDeclaredMethods();
        int toFind = 2;
        for (int i = mtds.length; --i >= 0;) {
            Method m = mtds[i];
            String signat = m.toString();
            String name = m.getName();
            if (name.startsWith("getDeviceId") && signat.endsWith("(int)")) {
                try {
                    id1 = (String) m.invoke(telephonyMgr, new Integer(0));
                    id2 = (String) m.invoke(telephonyMgr, new Integer(0));
                    if (id1 != null && id1.equals(id2)) {
                        imei = id1; // some devices return a dumb imei each time getDeviceId is called
                    }
                    // dual-sim support
                    id1 = (String) m.invoke(telephonyMgr, new Integer(1));
                    id2 = (String) m.invoke(telephonyMgr, new Integer(1));
                    if (id1 != null && id1.equals(id2)) {
                        imei2 = id1; // some devices return a dumb imei each time getDeviceId is called
                    }
                    if (--toFind == 0) {
                        break;
                    }
                } catch (Exception ee) {
                    AndroidUtils.handleException(ee, false);
                }
            } else if (name.startsWith("getSimSerialNumber") && signat.endsWith("(int)")) {
                try {
                    id1 = (String) m.invoke(telephonyMgr, new Integer(0));
                    id2 = (String) m.invoke(telephonyMgr, new Integer(0));
                    if (id1 != null && id1.equals(id2)) {
                        iccid = id1; // some devices return a dumb imei each time getDeviceId is called
                    }
                    if (--toFind == 0) {
                        break;
                    }
                } catch (Exception ee) {
                    AndroidUtils.handleException(ee, false);
                }
            }
        }

        if (imei == null) {
            try {
                id1 = telephonyMgr.getDeviceId(); // try to get the imei
                id2 = telephonyMgr.getDeviceId();
                if (id1 != null && id1.equals(id2)) {
                    imei = id1; // some devices return a dumb imei each time getDeviceId is called
                }
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }

        // iccid
        if (iccid == null) {
            try {
                id1 = telephonyMgr.getSimSerialNumber();
                id2 = telephonyMgr.getSimSerialNumber();
                if (id1 != null && id1.equals(id2)) {
                    iccid = id1;
                }
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
        telephonyInitialized = true;
    }
}
