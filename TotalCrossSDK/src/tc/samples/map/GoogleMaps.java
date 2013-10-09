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

package tc.samples.map;

import totalcross.io.device.gps.*;
import totalcross.net.*;
import totalcross.phone.*;
import totalcross.res.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.font.*;
import totalcross.ui.gfx.*;

public class GoogleMaps extends MainWindow
{
   public static final int BKGCOLOR = 0x0A246A;
   public static final int SELCOLOR = 0x829CE2; // Color.brighter(BKGCOLOR,120);

   public GoogleMaps()
   {
      super("Google Maps Sample",NO_BORDER);
      setTitle("");
      setUIStyle(Settings.Android);
      setBackColor(UIColors.controlsBack = Color.WHITE);
      transitionEffect = TRANSITION_OPEN;
      UIColors.messageboxBack = Color.brighter(BKGCOLOR,64);
      UIColors.messageboxFore = Color.WHITE;
      Settings.uiAdjustmentsBasedOnFontHeight = true;
   }
   
   Edit edAddr, edLat, edLon, edTo;
   Button btnShow;
   RadioGroupController rg;
   Check chSat,chGPS;
   
   ScrollContainer sc;
   public void initUI()
   {
      try
      {
         addHeaderBar();
         if (!Settings.onJavaSE && !Settings.platform.equals(Settings.ANDROID) && !Settings.isIOS())
         {
            add(new Label("This program runs on\nthe Android or iOS platforms only",CENTER),CENTER,CENTER);
            return;
         }
         add(sc = new ScrollContainer(false, true),LEFT,AFTER,FILL,FILL);
         
         sc.add(new Label("Select the parameters"),LEFT+50,TOP);
         sc.add(new Ruler(),LEFT+50,AFTER+50,FILL-50,PREFERRED);
         rg = new RadioGroupController();
         Radio r;
         sc.add(r = new Radio(" Current location",rg),LEFT+50,AFTER+50,FILL,PREFERRED); r.leftJustify = true;
         sc.add(chGPS = new Check("Use GPS if activated."),LEFT+100,AFTER+50,FILL,PREFERRED);
         sc.add(r = new Radio(" Address",rg),LEFT+50,AFTER+100,FILL,PREFERRED); r.leftJustify = true;
         sc.add(edAddr = new Edit(),LEFT+100,AFTER+50,FILL-200,PREFERRED);
         sc.add(new Label("To (optional): "), LEFT+100,AFTER+50);
         sc.add(edTo = new Edit(),AFTER+100,SAME,FILL-200,PREFERRED);
         sc.add(r = new Radio(" Coordinates",rg),LEFT+50,AFTER+100,FILL,PREFERRED); r.leftJustify = true;
         sc.add(new Label("Lat: "),LEFT+50,AFTER+50);
         sc.add(edLat = new Edit("+99.999999"), AFTER+50,SAME);
         sc.add(new Label("Lon: "),LEFT+50,AFTER+50);
         sc.add(edLon = new Edit("+99.999999"), SAME,AFTER+50,edLat); // vertical align
         sc.add(new Ruler(),LEFT+50,AFTER+50,FILL-50,PREFERRED);
         sc.add(chSat = new Check("Show satellite images"),LEFT+50,AFTER+100,FILL,PREFERRED);
         sc.add(btnShow = new Button("Show map"),CENTER,AFTER+100,SCREENSIZE+80,PREFERRED+50);
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
   
   private void addHeaderBar() throws Exception
   {
      int c1 = 0x0A246A;
      Font f = font.adjustedBy(2,true);
      final Bar headerBar = new Bar("Google Maps Sample");
      headerBar.setFont(f);
      headerBar.setBackForeColors(c1,Color.WHITE);
      headerBar.addButton(Resources.exit);
      add(headerBar, LEFT,0,FILL,PREFERRED);
      headerBar.addPressListener(new PressListener()
      {
         public void controlPressed(ControlEvent e)  
         {
            e.consumed = true;
            switch (((Bar)e.target).getSelectedIndex())
            {
               case 1:
               {
                  exit(0);
                  break;
               }  
            }
         }
      });
      
      addKeyListener(new KeyListener() // listen for BACK keypress (mapped to ESCAPE)
      {
         public void keyPressed(KeyEvent e) {}
         public void actionkeyPressed(KeyEvent e) {}
         public void specialkeyPressed(KeyEvent e)
         {
            if (e.key == SpecialKeys.ESCAPE)
            {
               e.consumed = true;
               exit(0);
            }
         }
      });
   }
}
