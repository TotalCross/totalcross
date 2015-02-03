package totalcross.android.scanners;

import android.content.*;
import android.view.*;

import totalcross.*;

public class MotorolaScanner implements IScanner
{
   // This intent string contains the source of the data as a string
   static final String SOURCE_TAG = "com.motorolasolutions.emdk.datawedge.source";
   // This intent string contains the barcode symbology as a string
   static final String LABEL_TYPE_TAG = "com.motorolasolutions.emdk.datawedge.label_type";
   // This intent string contains the barcode data as a byte array list
   static final String DECODE_DATA_TAG = "com.motorolasolutions.emdk.datawedge.decode_data";

   // This intent string contains the captured data as a string
   // (in the case of MSR this data string contains a concatenation of the track data)
   static final String DATA_STRING_TAG = "com.motorolasolutions.emdk.datawedge.data_string";

   // Let's define the MSR intent strings (in case we want to use these in the future)
   static final String MSR_DATA_TAG = "com.motorolasolutions.emdk.datawedge.msr_data";
   static final String MSR_TRACK1_TAG = "com.motorolasolutions.emdk.datawedge.msr_track1";
   static final String MSR_TRACK2_TAG = "com.motorolasolutions.emdk.datawedge.msr_track2";
   static final String MSR_TRACK3_TAG = "com.motorolasolutions.emdk.datawedge.msr_track3";
   static final String MSR_TRACK1_STATUS_TAG = "com.motorolasolutions.emdk.datawedge.msr_track1_status";
   static final String MSR_TRACK2_STATUS_TAG = "com.motorolasolutions.emdk.datawedge.msr_track2_status";
   static final String MSR_TRACK3_STATUS_TAG = "com.motorolasolutions.emdk.datawedge.msr_track3_status";

   // Let's define the API intent strings for the soft scan trigger
   static final String ACTION_SOFTSCANTRIGGER = "com.motorolasolutions.emdk.datawedge.api.ACTION_SOFTSCANTRIGGER";
   static final String EXTRA_PARAM = "com.motorolasolutions.emdk.datawedge.api.EXTRA_PARAMETER";
   static final String DWAPI_START_SCANNING = "START_SCANNING";
   static final String DWAPI_STOP_SCANNING = "STOP_SCANNING";
   static final String DWAPI_TOGGLE_SCANNING = "TOGGLE_SCANNING";
   
   ///private static String ourIntentAction = "";
   private static boolean isActive;
   private boolean scanning;
   private String barcode;
   
   public boolean scannerActivate()
   {
      // TODO create/enable the profile
      return isActive = true;
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
      // TODO disable the profile
      return isActive = false;
   }
   
   void triggerSoftScan()
   {
      Intent i = new Intent();
      // set the intent action using soft scan trigger action string declared earlier
      i.setAction(ACTION_SOFTSCANTRIGGER);
      // add a string parameter to tell DW that we want to toggle the soft scan trigger
      i.putExtra(EXTRA_PARAM, DWAPI_TOGGLE_SCANNING);
      // now broadcast the intent
      Launcher4A.loader.sendBroadcast(i);
   }

   public boolean checkScanner(KeyEvent event)
   {
      if (!isActive) return false;
      
      if (event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_L1)
      {
         scanning = event.getAction() == KeyEvent.ACTION_DOWN;
         if (scanning && event.getRepeatCount() == 0) barcode = "";
         else
         if (event.getAction() == KeyEvent.ACTION_UP && barcode != null && barcode.length() > 0)
            Launcher4A.instance._postEvent(Launcher4A.BARCODE_READ, 0, 0, 0, 0, 0);
      }
      else
      if (scanning && event.getAction() == KeyEvent.ACTION_DOWN)
      {
         char c = (char)event.getUnicodeChar();
         if (c > 0)
            barcode += (char)event.getUnicodeChar();
      }
      
      return scanning;
   }

   
}
