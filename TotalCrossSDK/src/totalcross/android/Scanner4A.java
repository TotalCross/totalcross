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

package totalcross.android;

import totalcross.*;
import android.content.*;
import com.intermec.aidc.*; 

/**
 * Scanner class for Android.
 *
 */
public class Scanner4A
{ 
   private static BarcodeReader bcr;
   private static VirtualWedge wedg;
   
   static boolean scannerActivate()
   {
      // Make sure the BarcodeReader dependent service is connected and
      // register a callback to service connect and disconnect events.
      AidcManager.connectService(Launcher4A.loader, new AidcManager.IServiceListener() 
      {
         public void onConnect()
         {            
            doBarcodReader(); // The dependent service is connected and it is ready to receive barcode requests.
         }

         public void onDisconnect() {}

      });
      return true;
   }
   
   static String getData()
   {
      return Launcher4A.loader.strBarcodeData;
   }
   
   static boolean deactivate()
   {
      try
      { 
         if (bcr != null)
         {             
             bcr.removeBarcodeReadListener(Launcher4A.loader);             
             bcr.setScannerEnable(false);
             bcr.close();
             bcr = null;
         }
         
         if (wedg != null)
         {
            wedg.setEnable(true);
            wedg = null;
         }
         wedg = null;
         
         //disconnect from data collection service
         AidcManager.disconnectService();
         return true;
      }
      catch (Exception exception)
      {
         exception.printStackTrace();
         return false;
      }
   }
   
   public static void doBarcodReader()
   {
      try
      { 
         //disable virtual wedge
         (wedg = new VirtualWedge()).setEnable(false);  
                     
         //set barcode reader object for internal scanner
         (bcr = new BarcodeReader()).setScannerEnable(true);

         //add barcode reader listener
         bcr.addBarcodeReadListener(Launcher4A.loader);
      }
      catch (Exception exception)
      {
         exception.printStackTrace();
      }
   }
}