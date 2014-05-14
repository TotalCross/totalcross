/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2001 Andrew Chitty                                             *
 *  Copyright (C) 2001-2012 SuperWaba Ltda.                                      *
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



package tc.samples.api.io.device;

import tc.samples.api.*;

import totalcross.io.device.scanner.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.event.*;

/** As several people have suggested you should implement a timer to deactivate
 the scanner when it has been inactive for a specified amount of time, disable
 controls on the form, and allow the user to reactivate with a button, etc.
 <p>Here's a sample of how to catch a Scanner event on a Window:
 <pre>
   class ScanWin extends Window
   {
        Label l;
        public ScanWin()
        {
           super("ScanWin",RECT_BORDER);
           setRect(CENTER,CENTER,Settings.screenWidth/2,Settings.screenHeight/2);
           Scanner.listener = this;
           add(l = new Label(""), LEFT,CENTER);
        }
        public void onEvent(Event e)
        {
           if (e.type == ScanEvent.SCANNED)
           {
              String data = ((ScanEvent)e).data;
              l.setText("data: "+data);
              l.repaintNow();
           }
        }
   }
 </pre>
 */

public class ScannerInternal extends BaseContainer
{
   // by making the members private, the compiler can optimize them.
   private String barCode;
   private Check chkScanner;
   private Button trigger;

   private Label lblScanManagerVersion;
   private Label lblPortDriverVersion;
   private Label lblRomSerialNumber;
   ListBox lb;
   
   public void initUI()
   {
      super.initUI();
      if (!Settings.platform.equals(Settings.ANDROID) && !Settings.onJavaSE)
      {
         add(new Label("This sample works only on Android"),CENTER,CENTER);
         return;
      }
      add(new Label("Scan manager version:"), CENTER, TOP);
      add(lblScanManagerVersion = new Label("", CENTER), LEFT, AFTER);
      add(new Label("Port driver version:"), CENTER, AFTER);
      add(lblPortDriverVersion = new Label("", CENTER), LEFT, AFTER);
      add(new Label("Rom serial number:"), CENTER, AFTER);
      add(lblRomSerialNumber = new Label("", CENTER), LEFT, AFTER);
      add(chkScanner = new Check("Scan"), LEFT, AFTER + 10);
      add(lb= new ListBox(),LEFT,AFTER + 10,FILL,FILL); 

      // tell scanner that we are the control that is listening the events.
      // if this is not done, all events will be sent to the top most window.
      // Scanner.listener = this;
      if (scannerStart())
      {
         // Versions can only be get after the Scanner is initialized
         lblScanManagerVersion.setText(Scanner.getScanManagerVersion());
         lblPortDriverVersion.setText(Scanner.getScanPortDriverVersion());
         lblRomSerialNumber.setText(Settings.romSerialNumber != null ? Settings.romSerialNumber : "Not available");
         
         if (Scanner.isPassive())
            add(trigger = new Button(" TRIGGER "), CENTER, BOTTOM); // support SocketCom              
         scannerStop();
      }
   }

   private void log(String s)
   {
      Vm.debug(s);
      lb.add(new Time()+" - "+s);
      lb.selectLast();
   }
   
   private void setStatus(String status)
   {
      log(status);
      // the mainwindow is painted when a normal event is sent by the system
      // since the ScanEvent isnt a normal event, we must explicitly repaint
      // the screen
      repaintNow(); // at startup it will be ugly but this is faster than repainting the whole screen
   }

   private boolean scannerStart()
   {
      if (Scanner.activate())
      {
         // only using BARUPCE for demo - use initializeScanner(String args[]) for your requirements
         if ((Settings.platform.equals(Settings.ANDROID) && Scanner.setBarcodeParam(Scanner.INTERMEC_CODE_128, true)
          && Scanner.setBarcodeParam(Scanner.INTERMEC_CODABAR, true) && Scanner.setBarcodeParam(Scanner.INTERMEC_EAN_UPC_UPC_E, true)
          && Scanner.setBarcodeParam(Scanner.INTERMEC_EAN_UPC_EAN_13, true) && Scanner.setBarcodeParam(Scanner.INTERMEC_EAN_UPC_EAN_8, true))
         || (!Settings.platform.equals(Settings.ANDROID) && Scanner.setBarcodeParam(Scanner.BARUPCE, true)))
         {   
            setStatus("Initializing scanner ...");
            if (Scanner.commitBarcodeParams())
            {
               setStatus("Scanner ready.");
               chkScanner.setChecked(true);
               return true;
            }
            else
            {
               setStatus("Scanner not initialized.");
               scannerStop();
            }
         }
      }
      else 
         setStatus("Scanner not activated.");
      return false;
   }

   private void scannerStop()
   {
      if (Scanner.deactivate())
         setStatus("Scanner deactivated.");
   }

   public void onExit() // there is no need to do this; the Scanner lib stops the Scanner for us.
   {
      scannerStop();
   }

   public void onEvent(Event event)
   {
      switch (event.type)
      {
         case ControlEvent.PRESSED:
         {
            if (event.target == trigger)
            {
               Scanner.trigger();
            }
            else
            if (event.target == chkScanner)
            {
               if (chkScanner.isChecked())
                  scannerStart();
               else
                  scannerStop();
            }
         }
         break;
         // note that in ScanEvents the target is the listener or the top most window.
         // caution: if you're going to popup a window when handling these events, do NOT
         // call popup or the application will hang. Use popupNonBlocking instead.
         case ScanEvent.SCANNED:
         {
            barCode = ((ScanEvent)event).data;
            String errorCode = "NR";
            final String status = (barCode == null? "": barCode.equals(errorCode)? "Try scanning a barcode ..." : barCode);
            MainWindow.getMainWindow().runOnMainThread(new Runnable()
            {
               public void run()
               {
                  log(status);
               }
            });
         }
         break;
         case ScanEvent.BATTERY_ERROR:
            setStatus("Replace Batteries");
            break;
         case ScanEvent.TRIGGERED:
            setStatus("Event being received from Scanner...");
            break;
      }
   }
}
