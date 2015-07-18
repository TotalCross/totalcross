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

package tc.samples.api.io.device;

import totalcross.io.*;
import totalcross.io.device.*;
import totalcross.io.device.bluetooth.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;
import totalcross.util.*;

import tc.samples.api.*;

public abstract class PrinterSampleBase extends BaseContainer
{
   Button btnRadioOn, btnDiscOn, btnRadioOff, btnListPaired, btnListUnpaired, btnConnect;
   Label lstatus;
   TimerEvent timer;
   Check chUnsec;
   static PrinterSampleBase instance;
   
   private void listDevices(String tit, RemoteDevice[] rd)
   {
      log(tit);
      if (rd == null)
         log("No device found");
      else
         for (int i = 0; i < rd.length; i++)
         {
            log(rd[i]);
            lblog.ihtBackColors.put(lblog.size()-1, Color.GREEN);
         }            
   }

   public void initUI()
   {
      try
      {
         super.initUI();
         instance = this;
         int gap = fmH/2;
         Button.commonGap = fmH/2;
         add(new Label("This sample assumes you know which printer is near by!"),LEFT,TOP+2);
         add(btnRadioOn = new Button("set radio on"),LEFT,AFTER+gap);
         add(btnRadioOff = new Button("set radio off"),AFTER+gap,SAME);
         add(btnDiscOn = new Button("temporarily discoverable"), LEFT,AFTER+gap);
         add(btnListPaired = new Button("list paired"), LEFT,AFTER+gap);
         add(btnListUnpaired = new Button("list unpaired"), AFTER+gap,SAME);
         add(btnConnect = new Button("connect to selected device"), LEFT,AFTER+gap);
         add(chUnsec = new Check("Unsecure connection"),LEFT,AFTER+gap,FILL,SAME);
         Button.commonGap = 0;
         btnConnect.setEnabled(false);
         add(lstatus = new Label("",CENTER),LEFT,BOTTOM);
         addLog(LEFT,AFTER+gap,FILL,FIT,chUnsec);
         lblog.ihtBackColors = new IntHashtable(30);
         timer = addTimer(200);
         updateButtonState(false,false);
      }
      catch (Exception ee)
      {
         MessageBox.showException(ee,true);
      }
   }
   
   public static void log(Object s)
   {
      BaseContainer.log(s);
      instance.loglistSelected();
   }

   public void onEvent(Event e)
   {
      try
      {
         switch (e.type)
         {
            case TimerEvent.TRIGGERED:
               if (timer != null && timer.triggered)
               {
                  switch (RadioDevice.getState(RadioDevice.BLUETOOTH))
                  {
                     case RadioDevice.RADIO_STATE_ENABLED: 
                        lstatus.setText("ON BUT NOT DISCOVERABLE");
                        updateButtonState(true,false);
                        break;
                     case RadioDevice.BLUETOOTH_STATE_DISCOVERABLE: 
                        lstatus.setText("ON AND DISCOVERABLE");
                        updateButtonState(true,true);
                        break;
                     default:
                        lstatus.setText("BLUETOOTH IS OFF");
                        updateButtonState(false,false);
                        break;
                  }
               }
               break;
            case ControlEvent.PRESSED:
               if (e.target == lblog)
                  loglistSelected();
               else
               if (e.target == btnRadioOff)
                  RadioDevice.setState(RadioDevice.BLUETOOTH, RadioDevice.RADIO_STATE_DISABLED);
               else
               if (e.target == btnRadioOn)
                  RadioDevice.setState(RadioDevice.BLUETOOTH, RadioDevice.RADIO_STATE_ENABLED);
               else
               if (e.target == btnDiscOn)
                  RadioDevice.setState(RadioDevice.BLUETOOTH, RadioDevice.BLUETOOTH_STATE_DISCOVERABLE);
               else
               if (e.target == btnListPaired)
               {
                  RemoteDevice[] l = LocalDevice.getLocalDevice().getDiscoveryAgent().retrieveDevices(DiscoveryAgent.PREKNOWN);
                  if (l == null || l.length == 0)
                  {
                     Vm.sleep(250);
                     l = LocalDevice.getLocalDevice().getDiscoveryAgent().retrieveDevices(DiscoveryAgent.PREKNOWN);
                  }
                  listDevices("Listing paired devices", l);
               }
               else
               if (e.target == btnListUnpaired)
                  listDevices("Listing unpaired devices", LocalDevice.getLocalDevice().getDiscoveryAgent().retrieveDevices(DiscoveryAgent.CACHED));
               else
               if (e.target == btnConnect)
               {
                  Object sel = lblog.getSelectedItem();
                  if (sel != null && sel instanceof RemoteDevice)
                  {
                     boolean printIt = true;//ask("This test currently only works with a CPCL / CPL compatible bluetooth printer (like Zebra MZ 320). Do you have such printer attached and want to print a test page?");
                     RemoteDevice rd = (RemoteDevice)sel;
                     log("Connecting to "+rd.getBluetoothAddress());
                     String addr = rd.getBluetoothAddress();
                     if (chUnsec.isChecked())
                        addr = "*"+addr;
                     Stream s = (Stream)Connector.open("btspp://"+addr+":0");
                     log("Connected!");
                     if (s != null && printIt)
                        printSample(s);
                     s.close();
                     log("Finished.");
                  }
               }
         }
      }
      catch (Exception ee)
      {
         log("Error - exception thrown "+ee);
         MessageBox.showException(ee,true);
      }
   }

   abstract protected void printSample(Stream s) throws Exception;

   private void loglistSelected()
   {
      Object sel = lblog.getSelectedItem();
      boolean ok = sel != null && sel instanceof RemoteDevice;
      btnConnect.setEnabled(ok);
      btnConnect.setBackColor(ok ? Color.GREEN : backColor);
   }

   private void updateButtonState(boolean on, boolean disc)
   {
      btnRadioOn.setEnabled(!on);
      btnRadioOff.setEnabled(on);
      btnDiscOn.setEnabled(!disc && on);
      btnListPaired.setEnabled(on);
      btnListUnpaired.setEnabled(on);
   }
}
