/*********************************************************************************
 * TotalCross Software Development Kit * Copyright (C) 2000-2012 SuperWaba Ltda. * All Rights Reserved * * This library
 * and virtual machine is distributed in the hope that it will * be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. * * This file is covered by the GNU LESSER
 * GENERAL PUBLIC LICENSE VERSION 3.0 * A copy of this license is located in file license.txt at the root of this * SDK
 * or can be downloaded here: * http://www.gnu.org/licenses/lgpl-3.0.txt * *
 *********************************************************************************/

package totalcross.android.scanners;

import totalcross.*;

import android.view.*;
import com.honeywell.aidc.*;
import com.honeywell.aidc.AidcManager.*;
import java.util.*;

/**
 * Scanner class for Android.
 */
public class HoneywellScanner implements IScanner, BarcodeReader.BarcodeListener, CreatedCallback
{
   /** Starts the definition of the barcode parameters. */
   public static final String START_BATCH = "***START_BATCH***";
   /** Ends the definition of the barcode parameters. */
   public static final String END_BATCH = "***END_BATCH***";

   private static Map<String, Object> properties;
   private static BarcodeReader barcodeReader;
   private static AidcManager manager;
   private static String barcode;

   @Override
   public boolean scannerActivate()
   {
      if (barcodeReader == null)
      {
         // get bar code instance from MainActivity
         AidcManager.create(Launcher4A.loader, this);
         long timeout = System.currentTimeMillis() + 20*1000;
         while (barcodeReader == null && System.currentTimeMillis() < timeout)
            try {Thread.sleep(40);} catch (Exception e) {}
         if (barcodeReader != null)
            try
            {
               barcodeReader.addBarcodeListener(this);
               barcodeReader.setProperty(BarcodeReader.PROPERTY_TRIGGER_CONTROL_MODE, BarcodeReader.TRIGGER_CONTROL_MODE_AUTO_CONTROL);
               barcodeReader.claim(); // required to start notifications!
            }
            catch (Exception e) {AndroidUtils.handleException(e,false);}
      }
      return barcodeReader != null;
   }

   @Override
   public void onCreated(AidcManager aidcManager) 
   {
      manager = aidcManager;
      barcodeReader = manager.createBarcodeReader();
   }

   @Override
   public void onBarcodeEvent(final BarcodeReadEvent event)
   {
      barcode = event.getBarcodeData();
      Launcher4A.instance._postEvent(Launcher4A.BARCODE_READ, 0, 0, 0, 0, 0);
   }

   @Override
   public void onFailureEvent(BarcodeFailureEvent event)
   {
   }

   public boolean setBarcodeParam(int barcodeType, boolean enable)
   {
      return true;
   }

   public String getData()
   {
      String b = barcode;
      barcode = "";
      return b;
   }

   public boolean deactivate()
   {
      if (barcodeReader != null)
      {
         barcodeReader.removeBarcodeListener(this);
         barcodeReader.release();
         barcodeReader.close();
         barcodeReader = null;
      }
      manager.close();
      manager = null;
      return true;
   }

   public boolean checkScanner(KeyEvent event)
   {
      return false;
   }

   public void setParam(String what, String value)
   {
      if (what.equals(START_BATCH))
         properties = new HashMap<String, Object>();
      else
      if (barcodeReader == null)
         ;
      else
      if (what.equals(END_BATCH))
      {
         barcodeReader.setProperties(properties);
         AndroidUtils.debug("Scanner parameters: "+properties);
         properties = null;
      }
      else
      {
         if (value.equalsIgnoreCase("true"))
            properties.put(what, true);
         else
         if (value.equalsIgnoreCase("false"))
            properties.put(what, false);
         else
            try
            {
               properties.put(what, Integer.parseInt(value)); // try as integer
            }
            catch (Exception e)
            {
               properties.put(what, value);
            }
      }
   }
}
