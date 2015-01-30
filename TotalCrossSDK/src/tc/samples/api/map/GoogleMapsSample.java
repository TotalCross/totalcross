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

import totalcross.io.*;
import totalcross.io.device.gps.*;
import totalcross.map.*;
import totalcross.net.*;
import totalcross.phone.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;
import totalcross.util.*;

public class GoogleMapsSample extends BaseContainer
{
   public static final int BKGCOLOR = 0x0A246A;
   public static final int SELCOLOR = 0x829CE2; // Color.brighter(BKGCOLOR,120);

   Edit edAddr, edLat, edLon, edTo;
   Button btnShow;
   RadioGroupController rg;
   Check chSat,chGPS,chWaze;
   
   ScrollContainer sc;
   private boolean isAndroid = Settings.platform.equals(Settings.ANDROID);
   
   public void initUI()
   {
      try
      {
         super.initUI();
         if (!Settings.onJavaSE && !isAndroid && !Settings.isIOS() && !Settings.platform.equals(Settings.WINDOWSPHONE))
         {
            add(new Label("This program runs on\nthe Android, Windows Phone\nor iOS platforms only",CENTER),CENTER,CENTER);
            return;
         }
         int g = fmH/4;
         add(sc = new ScrollContainer(false, true),LEFT,TOP,FILL,FILL);
         
         rg = new RadioGroupController();
         Radio r;
         sc.add(new Label("Select the parameters"),LEFT+g,TOP);
         sc.add(r = new Radio(" TotalCross MGP locatoin",rg),LEFT+g,AFTER+g,FILL,PREFERRED); r.leftJustify = true;
         sc.add(new Ruler(),LEFT+g,AFTER+g,FILL-g,PREFERRED);
         sc.add(r = new Radio(" Current location",rg),LEFT+g,AFTER+g,FILL,PREFERRED); r.leftJustify = true;
         sc.add(chGPS = new Check("Use GPS if activated."),LEFT+2*g,AFTER+g,FILL,PREFERRED);
         sc.add(r = new Radio(" Address",rg),LEFT+g,AFTER+2*g,FILL,PREFERRED); r.leftJustify = true;
         sc.add(edAddr = new Edit(),LEFT+2*g,AFTER+g,FILL-g*4,PREFERRED);
         sc.add(chWaze = new Check("Use WAZE if installed"),LEFT+g,AFTER+g);
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
         edAddr.setText("Av. Sen. Carlos Jereissati 3000, Fortaleza, CE, brazil");
         edTo.setText("Av Norte 2920, Fortaleza, CE, Brazil");
         rg.setSelectedIndex(0);
         enableControls();
         chWaze.addPressListener(new PressListener()
         {
            public void controlPressed(ControlEvent e)
            {
               if (chWaze.isChecked())
                  Toast.show("The address you specify will be used as the destination address. Dont use \"To (optional)\".",3000);
            }
         });
      }
      catch (Exception e)
      {
         MessageBox.showException(e,true);
      }
   }
   
   private void enableControls()
   {
      int idx = rg.getSelectedIndex();
      chGPS.setEnabled(idx == 1);
      edAddr.setEnabled(idx == 2);
      edLat.setEnabled(idx == 3);
      edLon.setEnabled(idx == 3);
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
                  int sel = rg.getSelectedIndex();
                  String addr = null;
                  switch (sel)
                  {
                     case 0:
                        if (!isAndroid)
                           GoogleMaps.showAddress(toLatLon("Av Norte 2920, Luciano Cavalcante, Fortaleza, CE, Brasil"), chSat.isChecked());
                        else
                        {
                           // text
                           GoogleMaps.Place p1 = new GoogleMaps.Place();
                           p1.lat = -3.778284;
                           p1.lon = -38.482617;
                           p1.backColor = Color.WHITE;
                           p1.capColor = Color.BLACK;
                           p1.detColor = 0x444444;
                           p1.pinColor = Color.RED;
                           p1.caption = "TotalCross MGP";
                           p1.fontPerc = 150;
                           p1.detail = "Av Norte 2920\nLuciano Cavalcante\nCeará - Brazil";
                           // now draw the brazilian's flag
                           double ww = -0.0003, hh = -0.0002;
                           double left = p1.lat + ww/4, right = left + ww;
                           double top = p1.lon - hh, bottom = top + hh+hh;
                           // green rectangle
                           GoogleMaps.Shape s1 = new GoogleMaps.Shape();
                           s1.color = Color.GREEN;
                           s1.filled = true;
                           s1.lats = new double[]{left,right,right,left};
                           s1.lons = new double[]{top,top,bottom,bottom};
                           // yellow losangle
                           GoogleMaps.Shape s2 = new GoogleMaps.Shape();
                           s2.color = Color.YELLOW;
                           s2.filled = true;
                           s2.lats = new double[]{left+(right-left)/2,right,left+(right-left)/2,left};
                           s2.lons = new double[]{top,top+(bottom-top)/2,bottom,top+(bottom-top)/2};
                           // blue circle
                           GoogleMaps.Circle c = new GoogleMaps.Circle();
                           c.lat = (left+right)/2;
                           c.lon = (top+bottom)/2;
                           c.color = Color.BLUE;
                           c.filled = true;
                           c.rad = hh/2;
                           
                           GoogleMaps.showMap(new GoogleMaps.MapItem[]{p1,s1,s2,c},chSat.isChecked());
                        }
                        return;
                     case 1:
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
                     case 2:
                        addr = edAddr.getText();
                        if (!isAndroid)
                        {
                           String tempAddr = toLatLon(addr);
                           if (tempAddr == null)
                              Vm.debug("Unabled to find coordinates for address (addr): "+addr);
                           else
                              addr = tempAddr;
                        }
                        break;
                     case 3:
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
                        if (!isAndroid && to.length() > 0)
                        {
                           String tempTo = toLatLon(to);
                           if (tempTo == null)
                              Vm.debug("Unable to find coordinates for address (to): "+to);
                           else
                              to = tempTo;
                        }
                        Vm.debug("addr: "+addr+", to: "+to);
                        if (chWaze.isChecked() || (to != null && to.length() > 0))
                           ok = totalcross.map.GoogleMaps.showRoute(addr,to,null,(chSat.isChecked() ? GoogleMaps.SHOW_SATELLITE_PHOTOS : 0) | (chWaze.isChecked() ? GoogleMaps.USE_WAZE : 0));
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
                     if (mb != null) mb.unpop();
                     mb = new MessageBox("","Failed to show the map. Location not found.",null);
                     mb.popupNonBlocking();
                     Vm.safeSleep(2000);
                     mb.unpop();
                  }
               }
               break;
         }
      }
      catch (NotInstalledException nie)
      {
         Toast.show("Waze is not installed. Uncheck it and try again",2000);
      }
      catch (Exception ee)
      {
         MessageBox.showException(ee,true);
      }
   }

   private String toLatLon(String addr) throws IOException, InvalidNumberException
   {
      double[] ll = GoogleMaps.getLocation(addr);
      return ll == null ? null : "@"+ll[0]+","+ll[1];
   }
}
