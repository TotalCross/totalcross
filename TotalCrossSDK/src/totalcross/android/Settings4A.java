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

// $Id: Settings4A.java,v 1.5 2011-03-28 15:44:38 guich Exp $

package totalcross.android;

import totalcross.Launcher4A;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;

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
   public static boolean isColor;
   public static boolean isHighColor;
   public static int maxColors;
   
   // platform
   public static String deviceId;
   public static int romVersion;
   
   // locale
   public static boolean daylightSavings;
   public static int timeZone;
   public static String timeZoneStr;
   
   // identification
   public static String userName;
   public static String imei;
   public static String esn;   
   public static String iccid;

   // device capabilities
   public static boolean virtualKeyboard;
   public static boolean keypadOnly;

   public static void refresh()
   {
      settingsRefresh();
   }
   
	// this class can't be instantiated
	private Settings4A()
	{
	}
	
	static void fillSettings()
	{
      // platform
      romVersion = Build.VERSION.SDK_INT;    
      deviceId = Build.MANUFACTURER + " " + Build.MODEL;
      
      // userName
      userName = null; // still looking for a way to retrieve this on droid.	   
	   
      // imei
      TelephonyManager telephonyMgr = (TelephonyManager) Launcher4A.getAppContext().getSystemService(Context.TELEPHONY_SERVICE); 
      imei = telephonyMgr.getDeviceId(); 
      iccid = telephonyMgr.getSimSerialNumber();
      
      // virtualKeyboard
      virtualKeyboard = true; // always available on droid?
      
      // number representation
      java.text.DecimalFormatSymbols dfs = new java.text.DecimalFormatSymbols();
      thousandsSeparator = dfs.getGroupingSeparator();
      decimalSeparator = dfs.getDecimalSeparator();	   
	   
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
      
      settingsRefresh();
	   
	   if (Build.MODEL.equals("sdk") && Build.PRODUCT.equals("sdk") && Build.TYPE.equals("eng"))
	   {
	      // running on emulator, right now there's no way to retrieve more settings from it.
	   }
	   else
	   {
	      ContentResolver cr = Launcher4A.getAppContext().getContentResolver();
	      
	      Configuration config = new Configuration();
	      Settings.System.getConfiguration(cr, config);
	      if (config.keyboard == Configuration.KEYBOARD_12KEY)
	         keypadOnly = true;
	      
	      // is24Hour
	      is24Hour = Settings.System.getInt(cr, Settings.System.TIME_12_24, is24Hour ? 1 : 0) == 1;
	      
	      // date format
	      String format = Settings.System.getString(cr, Settings.System.DATE_FORMAT);
	      if (format != null)
	      {
	         char firstChar = format.charAt(0);
	         dateFormat = firstChar == 'd' ? DATE_DMY
                  : firstChar == 'M' ? DATE_MDY
                  : DATE_YMD;
	      }
	   }
	}
	
   public static void settingsRefresh()
   {
      java.util.Calendar cal = java.util.Calendar.getInstance();
      daylightSavings = cal.get(java.util.Calendar.DST_OFFSET) != 0;
      java.util.TimeZone tz = java.util.TimeZone.getDefault();
      timeZone = tz.getRawOffset() / (60*60*1000);
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
}