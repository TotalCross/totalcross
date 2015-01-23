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



package totalcross.net;

import totalcross.io.IOException;
import totalcross.util.Hashtable;
import totalcross.util.Vector;

public class ConnectionManager4D
{
   static Object connRef;

   static ConnectionManager4D CmInstance = new ConnectionManager4D();

   /** keep track of all open connections */
   static Hashtable openConnections;

   public static final int CRADLE = 1;
   public static final int WIFI = 2;
   public static final int CELLULAR = 3;
   public static final int GPRS = CELLULAR;

   private ConnectionManager4D()
   {
      ConnectionManager4D.openConnections = new Hashtable(16);
      loadResources();
   }

   native private void loadResources();

   native public static void setDefaultConfiguration(int type, String cfg) throws IOException;

   native public static boolean isAvailable(int type) throws IOException;

   public static void open() throws IOException
   {
      IOException firstEx = null;
      int[] conn = new int[] { CRADLE, WIFI, CELLULAR };

      for (int i = 0; i < conn.length; i++)
      {
         try
         {
            if (isAvailable(conn[i]))
            {
               open(conn[i]);
               return; // successfully opened, so just return
            }
         }
         catch (IOException ex)
         {
            if (firstEx == null) // store only the first exception
               firstEx = ex;
         }
      }

      throw firstEx == null ? new IOException("No connections available") : firstEx;
   }

   native public static void open(int type) throws IOException;

   /*
    * flsobral@tc123_20
    *    fixed bug in ConnectionManager.close implementation for PalmOS: Using this method without first closing all
    *    open socket connections would eventually lead to an unrecoverable error, usually causing the device to reset.
    */
   public static void close() throws IOException
   {
      if (openConnections.size() > 0)
      {
         Vector values = openConnections.getValues();
         for (int i = values.size() - 1; i >= 0; i--)
         {
            Socket connection = ((Socket) values.items[i]);
            connection.close();
            openConnections.remove(connection);
         }
      }
      nativeClose();
   }

   native private static void nativeClose() throws IOException;

   native private void releaseResources();

   protected void finalize()
   {
      releaseResources();
   }

   native public static String getHostAddress(String host) throws UnknownHostException;

   native public static String getHostName(String host) throws UnknownHostException;

   native public static String getLocalHost() throws UnknownHostException;
   
   public static boolean isInternetAccessible()
   {
      try
      {
         Socket s = new Socket("www.google.com",80,30*1000);
         s.close();
         return true;
      }
      catch (Exception e)
      {
         return false;
      }
   }
}
