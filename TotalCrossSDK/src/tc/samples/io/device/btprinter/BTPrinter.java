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

package tc.samples.io.device.btprinter;

import totalcross.io.*;
import totalcross.io.device.*;
import totalcross.io.device.bluetooth.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.font.*;
import totalcross.ui.gfx.*;
import totalcross.util.*;

public class BTPrinter extends MainWindow
{
   Button btnRadioOn, btnDiscOn, btnRadioOff, btnListPaired, btnListUnpaired, btnConnect;
   Label lstatus;
   TimerEvent timer;
   ListBox loglist;
   
   public BTPrinter()
   {
      super("Bluetooth Printer",HORIZONTAL_GRADIENT);
      setUIStyle(Settings.Vista);
      if (Settings.screenWidth > 600)
         setDefaultFont(Font.getFont(false,20));
   }
   
   private void listDevices(String tit, RemoteDevice[] rd)
   {
      log(tit);
      if (rd == null)
         log("No device found");
      else
         for (int i = 0; i < rd.length; i++)
         {
            log(rd[i]);
            loglist.ihtBackColors.put(loglist.size()-1, Color.GREEN);
         }            
   }

   public void initUI()
   {
      try
      {
         Button btn;
         btn= new Button(" X ");
         btn.setBorder(Button.BORDER_NONE);
         btn.transparentBackground = true;
         add(btn,RIGHT,0);
         btn.addPressListener(new PressListener()
         {
            public void controlPressed(ControlEvent e)
            {
               exit(0);
            }
         });
         
         int gap = fmH/2;
         Button.commonGap = 5;
         add(btnRadioOn = new Button("set radio on"),LEFT,TOP+2);
         add(btnRadioOff = new Button("set radio off"),AFTER+gap,SAME);
         add(btnDiscOn = new Button("temporarily discoverable"), LEFT,AFTER+gap);
         add(btnListPaired = new Button("list paired"), LEFT,AFTER+gap);
         add(btnListUnpaired = new Button("list unpaired"), AFTER+gap,SAME);
         add(btnConnect = new Button("connect to selected device"), LEFT,AFTER+gap);
         btnConnect.setEnabled(false);
         Button.commonGap = 0;
         add(lstatus = new Label("",CENTER),LEFT,BOTTOM);
         loglist = new ListBox();
         loglist.setCursorColor(backColor);
         loglist.enableHorizontalScroll();
         loglist.ihtBackColors = new IntHashtable(30);
         //loglist.setCursorColor(loglist.getBackColor());
         add(loglist,LEFT,AFTER+gap,FILL,FIT,btnConnect);
         timer = addTimer(200);
         updateButtonState(false,false);
      }
      catch (Exception ee)
      {
         MessageBox.showException(ee,true);
      }
   }
   
   private void log(Object s)
   {
      loglist.add(s);
      loglist.selectLast();
      loglistSelected();
   }

   public void onEvent(Event e)
   {
      try
      {
         switch (e.type)
         {
            case TimerEvent.TRIGGERED:
               if (timer.triggered)
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
               if (e.target == loglist)
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
                  listDevices("Listing paired devices", LocalDevice.getLocalDevice().getDiscoveryAgent().retrieveDevices(DiscoveryAgent.PREKNOWN));
               else
               if (e.target == btnListUnpaired)
                  listDevices("Listing unpaired devices", LocalDevice.getLocalDevice().getDiscoveryAgent().retrieveDevices(DiscoveryAgent.CACHED));
               else
               if (e.target == btnConnect)
               {
                  Object sel = loglist.getSelectedItem();
                  if (sel != null && sel instanceof RemoteDevice)
                  {
                     RemoteDevice rd = (RemoteDevice)sel;
                     log("Connecting to "+rd.getBluetoothAddress());
                     Stream s = (Stream)Connector.open("btspp://"+rd.getBluetoothAddress()+":0");
                     log("Connected!");
                     if (s != null)
                     {
                        s.writeBytes("! 0 200 200 210 1\r\nTEXT 4 0 30 40 Gui S2 Nak\r\nFORM\r\nPRINT\r\n");
                        s.close();
                     }
                  }
               }
         }
      }
      catch (Exception ee)
      {
         MessageBox.showException(ee,true);
      }
   }

   private void loglistSelected()
   {
      Object sel = loglist.getSelectedItem();
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
