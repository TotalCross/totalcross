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

import tc.samples.api.*;

import totalcross.io.*;
import totalcross.io.device.*;
import totalcross.io.device.bluetooth.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;

public class BTTransfer extends BaseContainer
{
   StreamConnectionNotifier server;
   Button btListP,btListNP,btClient, btSend, btHost;
   Edit edClient;
   
   public void onEvent(Event e)
   {
      try
      {
         if (e.type == ControlEvent.PRESSED)
         {
            if (e.target == btHost)
               startHost();
            else
            if (e.target == btListP || e.target == btListNP)
               listDevices(e.target == btListP);
            else
            if (e.target == btClient)
               startClient();
            else
            if (e.target == btSend && edClient.getLength() > 0)
               sendMessage();
         }
      }
      catch (Exception ee)
      {
         MessageBox.showException(ee,true);
      }
   }
   
   private void sendMessage() throws IOException
   {
      String s = edClient.getText();
      dssocket.writeString(s);
      edClient.clear();
   }

   private static final String UUID = "EFB1AB0FCF1CDC9927DB55E12D98A996";
   private void listDevices(String tit, RemoteDevice[] rd)
   {
      log(tit);
      if (rd == null)
         log("No device found");
      else
         for (int i = 0; i < rd.length; i++)
            log(rd[i]);
   }

   private void listDevices(boolean paired) throws IOException
   {
      if (!paired)
         RadioDevice.setState(RadioDevice.BLUETOOTH, RadioDevice.RADIO_STATE_ENABLED);
      btListNP.setEnabled(false);
      btListP.setEnabled(false);
  //    try
      {
         if (paired)
            listDevices("Listing paired devices", LocalDevice.getLocalDevice().getDiscoveryAgent().retrieveDevices(DiscoveryAgent.PREKNOWN));
         else
            listDevices("Listing unpaired devices", LocalDevice.getLocalDevice().getDiscoveryAgent().retrieveDevices(DiscoveryAgent.CACHED));
      }
//      finally
      {
         btListNP.setEnabled(true);
         btListP.setEnabled(true);
      }
   }
   
   Stream btsocket;
   DataStream dssocket;
   
   private void startClient() throws Exception
   {
      Object sel = lblog.getSelectedItem();
      if (sel != null && sel instanceof RemoteDevice)
      {
         try
         {
            RadioDevice.setState(RadioDevice.BLUETOOTH, RadioDevice.RADIO_STATE_ENABLED);
            btHost.setEnabled(false);
            btClient.setEnabled(false);
            RemoteDevice rd = (RemoteDevice)sel;
            log("Connecting to "+rd.getBluetoothAddress());
            btsocket = (Stream)Connector.open("btspp://"+rd.getBluetoothAddress()+":0;uuid="+UUID);
            dssocket = new DataStream(btsocket);
            log("Connected!");
            btSend.setEnabled(true);
         }
         catch (Exception e)
         {
            btClient.setEnabled(true);
            btSend.setEnabled(false);
            throw e;
         }
      }
   }

   private void startHost()
   {
      btHost.setEnabled(false);
      btClient.setEnabled(false);
      btListNP.setEnabled(false);
      btListP.setEnabled(false);
      new Thread() 
      { 
         public void run() 
         {
            try
            {
               log("Enabling bluetooth");
               RadioDevice.setState(RadioDevice.BLUETOOTH, RadioDevice.BLUETOOTH_STATE_DISCOVERABLE);
               log("Waiting connections...");
               server = (StreamConnectionNotifier) Connector.open("btspp://localhost:"+UUID);
               Stream connection = server.accept();
               log("Connection accepted! "+connection);
               DataStream ds = new DataStream(connection);
               String s;
               while ((s = ds.readString()) != null)
               {
                  log(s);
                  if (s.equals(QUIT))
                     break;
               }
               connection.close();
               server.close();
               
               //LocalDevice.getLocalDevice().getRecord(server);
            }
            catch (Exception e)
            {
               MessageBox.showException(e, true);
            }
         }
      }.start();
   }
   
   public void initUI()
   {
      try
      {
         super.initUI();
         if (!Settings.platform.equals(Settings.ANDROID) && !Settings.onJavaSE)
            add(new Label("This sample works\nonly in Android"),CENTER,CENTER);
         else
         {
            add(btHost = new Button("host"),RIGHT,TOP);
            add(btClient = new Button("client"),BEFORE-5,SAME);
            add(btListP = new Button("paired"),LEFT,SAME);
            add(btListNP = new Button("!paired"),AFTER+5,SAME);
            add(btSend = new Button(" Send "),AFTER+5,SAME);
            add(edClient = new Edit(),LEFT,AFTER+5);
            addLog(LEFT,AFTER+5,FILL,FILL,null);
            btSend.setEnabled(false);
            log("Instructions: You must have two devices, one HOST and one CLIENT. In the HOST device, press HOST button and wait. In the CLIENT device, press the paired and not paired buttons, then select the bluetooth device you want to connect to, and press CLIENT. Then type something and press SEND.");
         }
      }
      catch (Exception ee)
      {
         MessageBox.showException(ee,true);
      }
   }
   
   static String QUIT = "quit!";
   
   public void onRemove()
   {
      if (btsocket != null) // client code
         try
         {
            dssocket.writeString(QUIT);
            Vm.sleep(1000);
            btsocket.close();
         }
         catch (Exception ee) {ee.printStackTrace();}
      
      if (server != null) // host code
         try {server.close();} catch (Exception ee) {ee.printStackTrace();}
   }
}