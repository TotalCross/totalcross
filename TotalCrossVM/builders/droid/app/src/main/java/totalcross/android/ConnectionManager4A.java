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
import android.database.*;
import android.net.*;
import android.net.wifi.*;
import android.telephony.*;
import java.net.*;
import java.util.*;

import totalcross.*;

public class ConnectionManager4A
{
   static Object connRef;

   private static String connGPRSPrefix = "";
   private static String connGPRS = connGPRSPrefix;

   private static String connWIFIPrefix = "interface=wifi;";
   private static String connWIFI = connWIFIPrefix;

   public static final int GPRS = 0;
   public static final int WIFI = 1;

   /** Specifies that only wifi should be used */
//   private static final int TRANSPORT_WIFI = 1;
   /** Specifies that only BES (also known as MDS or corporate servers) */
//   private static final int TRANSPORT_BES = 2;
   /** Specifies that only BIS should be used (Basically RIM hosted BES) */
//   private static final int TRANSPORT_BIS = 4;
   /** Specifies that TCP should be used (carrier transport) */
//   private static final int TRANSPORT_DIRECT_TCP = 8;
   /** Specifies that WAP2 should be used (carrier transport) */
//   private static final int TRANSPORT_WAP2 = 16;
   /** The default order in which selected transports will be attempted */
/*   private static final int DEFAULT_TRANSPORT_ORDER[] = { TRANSPORT_WIFI, TRANSPORT_BES, TRANSPORT_BIS,
         TRANSPORT_DIRECT_TCP, TRANSPORT_WAP2 };

   private static int transports[];
   private static int curIndex;
   private static int curSubIndex;
*/
   static final Uri CONTENT_URI = Uri.parse("content://telephony/carriers");
   private static final Uri PREFERRED_APN_URI = Uri.parse("content://telephony/carriers/preferapn");

   private ConnectionManager4A()
   {
   }

   public static void setDefaultConfiguration(int type, String cfg)
   {
      if (cfg == null)
         cfg = "";

      switch (type)
      {
         case GPRS:
         {
            int id = -1;
            ContentResolver contentResolver = Launcher4A.loader.getContentResolver();
            Cursor cursor = contentResolver.query(CONTENT_URI, new String[] { "_id" },
                  "apn = ? and user = ? and password = ?", new String[] { "tim.br", "tim", "tim" }, null);
            if (cursor == null || cursor.getCount() <= 0)
            {
               TelephonyManager tel = (TelephonyManager) Launcher4A.loader.getSystemService(Context.TELEPHONY_SERVICE);
               String networkOperator = tel.getNetworkOperator();

               if (networkOperator != null && networkOperator.length() > 0) 
               {
                   int mcc = Integer.parseInt(networkOperator.substring(0, 3));
                   int mnc = Integer.parseInt(networkOperator.substring(3));
                   ContentValues values = new ContentValues();
                   values.put("apn", "tim.br");
                   values.put("user", "tim");
                   values.put("password", "tim");
                   values.put("mcc", mcc);
                   values.put("mnc", mnc);
                   values.put("numeric", mcc + "" + mnc);
                   contentResolver.insert(CONTENT_URI, values);
                   cursor = contentResolver.query(CONTENT_URI, new String[] { "_id" },
                         "apn = ? and user = ? and password = ?", new String[] { "tim.br", "tim", "tim" }, null);                   
               }               
            }
            if (cursor == null)
               return;
            if (cursor.moveToFirst())
               id = cursor.getInt(0);
            cursor.close();

            if (id > -1)
            {
               ContentValues values = new ContentValues();
               //See /etc/apns-conf.xml. The TelephonyProvider uses this file to provide
               //content://telephony/carriers/preferapn URI mapping
               values.put("apn_id", id);
               contentResolver.update(PREFERRED_APN_URI, values, null, null);
               cursor = contentResolver.query(PREFERRED_APN_URI, new String[] { "name", "apn" }, "_id=" + id, null, null);
               if (cursor != null)
                  cursor.close();
            }
         }
         break;
         case WIFI:
            connWIFI = connWIFIPrefix + cfg;
         break;
         //default:
         //   throw new IllegalArgumentException("Invalid value for argument 'type'");
      }
   }

   public static void open()
   {
      connRef = null;
   }

   public static void open(int type)
   {
      switch (type)
      {
         case GPRS:
            connRef = connGPRS;
         break;
         case WIFI:
            connRef = connWIFI;
         break;
         //default:
         //   throw new IllegalArgumentException("Invalid value for argument 'type'");
      }
   }

   public static void close()
   {
      connRef = null;
   }

   public static String getHostAddress(String hostName)
   {
      try
      {
         return InetAddress.getByName(hostName).getHostAddress();
      }
      catch (Exception e)
      {
         return null;
      }
   }

   public static String getHostName(String hostAddress)
   {
      try
      {
         return InetAddress.getByName(hostAddress).getHostName();
      }
      catch (Exception e)
      {
         return null;
      }
   }
   
   public static String getLocalIpAddress() 
   {
      String ipv4=null;
      try 
      {
         for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) 
         {
            NetworkInterface intf = en.nextElement();
            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) 
            {
               InetAddress inetAddress = enumIpAddr.nextElement();
			   
               // for getting IPV4 format
               if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) 
                  ipv4 = inetAddress.getHostAddress();
                  return ipv4;
            }
         }
      } 
      catch (Exception ex) 
      {
         AndroidUtils.debug("IP Address"+ex.toString());
      }
      return null;
  }

   public static String getLocalHost()
   {
      try
      {
         ConnectivityManager connMgr = (ConnectivityManager) Launcher4A.loader.getSystemService(Context.CONNECTIVITY_SERVICE);
         int type = connMgr.getActiveNetworkInfo().getType();
         if (type == ConnectivityManager.TYPE_WIFI)
         {
            WifiManager wifiMgr = (WifiManager) Launcher4A.loader.getSystemService(Context.WIFI_SERVICE);
            return ipAddressToString(wifiMgr.getDhcpInfo().ipAddress);
         }
         String ip = getLocalIpAddress();
         return ip != null ? ip : InetAddress.getLocalHost().getHostAddress();
      }
      catch (Exception e)
      {
      }
      return "127.0.0.1";
   }

   public static boolean isAvailable(int type)
   {
      switch (type)
      {
         case GPRS:
            return true;
         case WIFI:
            return false;
         default:
            return false;
      }
   }
   
   public static boolean isInternetAccessible()
   {
      try
      {
         InetSocketAddress isa = new InetSocketAddress(InetAddress.getByName("www.google.com"),80);
         Socket s = new Socket();
         s.connect(isa,30*1000);
         s.close();
         return true;
      }
      catch (Exception e)
      {
         return false;
      }
   }
   
   private static String ipAddressToString(int addr)
   {
      StringBuffer buf = new StringBuffer(8);
      buf.append(addr & 0xff).append('.').append((addr >>>= 8) & 0xff).append('.').append((addr >>>= 8) & 0xff).append(
            '.').append((addr >>>= 8) & 0xff);
      return buf.toString();
   }
}
