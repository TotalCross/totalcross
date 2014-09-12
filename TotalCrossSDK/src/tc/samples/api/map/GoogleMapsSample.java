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

package tc.samples.api.map;

import tc.samples.api.*;

import totalcross.io.device.gps.*;
import totalcross.map.*;
import totalcross.net.*;
import totalcross.phone.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;

public class GoogleMapsSample extends BaseContainer
{
   public static final int BKGCOLOR = 0x0A246A;
   public static final int SELCOLOR = 0x829CE2; // Color.brighter(BKGCOLOR,120);

   Edit edAddr, edLat, edLon, edTo;
   Button btnShow;
   RadioGroupController rg;
   Check chSat,chGPS;
   
   ScrollContainer sc;
   public void initUI()
   {
      try
      {
         super.initUI();
         if (!Settings.onJavaSE && !Settings.platform.equals(Settings.ANDROID) && !Settings.isIOS())
         {
            add(new Label("This program runs on\nthe Android or iOS platforms only",CENTER),CENTER,CENTER);
            return;
         }
         int g = fmH/4;
         add(sc = new ScrollContainer(false, true),LEFT,TOP,FILL,FILL);
         
         sc.add(new Label("Select the parameters"),LEFT+g,TOP);
         sc.add(new Ruler(),LEFT+g,AFTER+g,FILL-g,PREFERRED);
         rg = new RadioGroupController();
         Radio r;
         sc.add(r = new Radio(" Current location",rg),LEFT+g,AFTER+g,FILL,PREFERRED); r.leftJustify = true;
         sc.add(chGPS = new Check("Use GPS if activated."),LEFT+2*g,AFTER+g,FILL,PREFERRED);
         sc.add(r = new Radio(" Address",rg),LEFT+g,AFTER+2*g,FILL,PREFERRED); r.leftJustify = true;
         sc.add(edAddr = new Edit(),LEFT+2*g,AFTER+g,FILL-g*4,PREFERRED);
         sc.add(new Label("To (optional): "), LEFT+2*g,AFTER+g);
         sc.add(edTo = new Edit(),AFTER+2*g,SAME,FILL-g*4,PREFERRED);
         sc.add(r = new Radio(" Coordinates",rg),LEFT+g,AFTER+2*g,FILL,PREFERRED); r.leftJustify = true;
         sc.add(new Label("Lat: "),LEFT+g,AFTER+g);
         sc.add(edLat = new Edit("+99.999999"), AFTER+g,SAME);
         sc.add(new Label("Lon: "),LEFT+g,AFTER+g);
         sc.add(edLon = new Edit("+99.999999"), SAME,AFTER+g,edLat); // vertical align
         sc.add(new Ruler(),LEFT+g,AFTER+g,FILL-g,PREFERRED);
         sc.add(chSat = new Check("Show satellite images"),LEFT+g,AFTER+2*g,FILL,PREFERRED);
         sc.add(btnShow = new Button("Show map"),CENTER,AFTER+2*g,SCREENSIZE+80,PREFERRED+g);
         edLat.setValidChars("+-0123456789.");
         edLon.setValidChars("+-0123456789.");
         rg.setSelectedIndex(0);
         enableControls();
      }
      catch (Exception e)
      {
         MessageBox.showException(e,true);
      }
   }
   
   private void enableControls()
   {
      int idx = rg.getSelectedIndex();
      chGPS.setEnabled(idx == 0);
      edAddr.setEnabled(idx == 1);
      edLat.setEnabled(idx == 2);
      edLon.setEnabled(idx == 2);
   }
   
   private ProgressBox mbgps;
   private boolean gpsNotCancelled;
   private GPS gps;
   private Exception gpsEx;

   public void onEvent(Event e)
   {
      try
      {
         switch (e.type)
         {
            case ControlEvent.PRESSED:
               if (e.target instanceof Radio)
                  enableControls();
               else
               if (e.target == mbgps && mbgps.getPressedButtonIndex() == 0)
                  gpsNotCancelled = false;
               else
               if (e.target == btnShow)
               {
                  if (!ConnectionManager.isInternetAccessible())
                  {
                     new MessageBox("Attention","Internet is not available!").popup();
                     return;
                  }
                  GoogleMaps.Circle c = new GoogleMaps.Circle();
                  c.lat = -3.73243;
                  c.lon = -38.483414;
                  c.color = Color.BLUE;
                  c.filled = false;
                  c.rad = 70;
                  GoogleMaps.Circle c2 = new GoogleMaps.Circle();
                  c2.lat = -3.73243;
                  c2.lon = -38.483424;
                  c2.color = Color.RED;
                  c2.filled = true;
                  c2.rad = 30;
                  GoogleMaps.Circle c3 = new GoogleMaps.Circle();
                  c3.lat = -3.73243;
                  c3.lon = -38.483464;
                  c3.color = Color.ORANGE | 0xAA000000;
                  c3.filled = true;
                  c3.rad = 25;
                  GoogleMaps.Shape s1 = new GoogleMaps.Shape();
                  s1.color = Color.YELLOW | 0X88000000;
                  s1.filled = true;
                  s1.lats = new double[]{-3.73143,-3.73243,-3.73193};
                  s1.lons = new double[]{-38.483424, -38.483524, -38.483124};
                  GoogleMaps.Place p1 = new GoogleMaps.Place();
                  p1.lat = s1.lats[2];
                  p1.lon = s1.lons[2];
                  p1.backColor = Color.WHITE;
                  p1.capColor = Color.BLACK;
                  p1.detColor = 0x444444;
                  p1.caption = "Home location";
                  p1.detail = "Rua Tavares Coutinho 2050\nAp 102 - Varjota\nCeará - Brazil";
                  p1.pinFilename = "";
                  
                  GoogleMaps.showMap(new GoogleMaps.MapItem[]{c,c2,c3,s1,p1},false);
                  if (true) return;
                  
                  
                  String addr = null;
                  int sel = rg.getSelectedIndex();
                  switch (sel)
                  {
                     case 0:
                        gpsNotCancelled = true;
                        if (chGPS.isChecked())
                        {
                           final int seconds = 60;
                           String msg1 = "Starting GPS...\n(wait ";
                           String msg2 = " seconds)";
                           mbgps = new ProgressBox("",msg1+seconds+msg2,new String[]{"  Cancel  "});
                           mbgps.popupNonBlocking();
                           gps = null;
                           try
                           {
                              gpsEx = null;
                              gps = new GPS();
                              // wait until a timeout, or the user cancelled, or something is captured 
                              int ini = Vm.getTimeStamp();
                              for (int i = 0; i < 60 && gpsNotCancelled && gps.location[0] == GPS.INVALID; i++)
                              {
                                 Vm.safeSleep(1000);
                                 try
                                 {
                                    gps.retrieveGPSData();
                                    int sec = seconds-(Vm.getTimeStamp()-ini)/1000;
                                    if (sec >= 0)
                                       mbgps.setText(msg1+sec+msg2);
                                    else 
                                       break;
                                 }
                                 catch (Exception eee)
                                 {
                                    gpsEx = eee;
                                    break;
                                 }
                              }
                              if (mbgps != null)
                                 mbgps.unpop();
                              if (gpsEx != null)
                                 throw gpsEx;
                              if (gps.location[0] != GPS.INVALID)
                                 addr = "@"+gps.location[0]+","+gps.location[1];
                           }
                           catch (Exception ioe) 
                           {
                              mbgps.unpop();
                              mbgps = null;
                              MessageBox mb = new MessageBox("Error","An error occured while activating GPS: "+ioe.getMessage()+".",null);
                              mb.popupNonBlocking();
                              Vm.safeSleep(2000);
                              mb.unpop();
                           }
                           finally
                           {
                              if (gps != null)
                                 gps.stop();
                           }
                        }
                        if (addr == null && CellInfo.isSupported())
                        {
                           final int seconds = 5;
                           String msg1 = (chGPS.isChecked()?"GPS failed.\n":"")+"Getting location from GSM...\n(wait ";
                           String msg2 = " seconds)";
                           ProgressBox mb = new ProgressBox("",msg1+seconds+msg2,null);
                           try
                           {
                              mb.popupNonBlocking();
                              CellInfo.update();
                              int ini = Vm.getTimeStamp();
                              for (int i = 0; i < 5*2; i++)
                                 if (CellInfo.cellId != null)
                                    break;
                                 else
                                 {
                                    Vm.safeSleep(500);
                                    CellInfo.update();
                                    int sec = seconds - (Vm.getTimeStamp()-ini)/1000;
                                    if (sec >= 0)
                                       mb.setText(msg1+sec+msg2);
                                 }
                              double[] ret = CellInfo.toCoordinates();
                              if (ret != null)
                                 addr = "@"+ret[0]+","+ret[1];
                           }
                           catch (Exception ee)
                           {
                           }
                           mb.unpop();
                        }
                        break;
                     case 1: 
                        addr = edAddr.getText();
                        break;
                     case 2:
                        addr = "@"+edLat.getText()+","+edLon.getText();
                        break;
                  }
                  boolean ok = false;
                  MessageBox mb = sel != 1 ? null : new MessageBox("Wait","Searching location...",null);
                  if (addr != null)
                  {
                     if (mb != null)
                        mb.popupNonBlocking();
                     try
                     {
                        String to = edTo.getText();
                        if (to.length() > 0)
                           ok = totalcross.map.GoogleMaps.showRoute(addr,to,null,chSat.isChecked());
                        else
                           ok = totalcross.map.GoogleMaps.showAddress(addr,chSat.isChecked());                        
                     }
                     finally
                     {
                        if (mb != null)
                           mb.unpop();
                     }
                  }
                  if (!ok)
                  {
                     mb = new MessageBox("","Failed to show the map. Location not found.",null);
                     mb.popupNonBlocking();
                     Vm.safeSleep(2000);
                     mb.unpop();
                  }
               }
               break;
         }
      }
      catch (Exception ee)
      {
         MessageBox.showException(ee,true);
      }
   }
}
