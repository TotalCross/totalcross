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

package tc.samples.map;

import totalcross.io.device.gps.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.font.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;

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
   
   Edit edAddr, edLat, edLon;
   Button btnShow;
   RadioGroupController rg;
   Check chSat;
   
   public void initUI()
   {
      try
      {
         addHeaderBar();
         ScrollContainer sc;
         add(sc = new ScrollContainer(false, true),LEFT,AFTER,FILL,FILL);
         
         sc.add(new Label("Select the parameters"),LEFT+50,TOP);
         rg = new RadioGroupController();
         Radio r;
         sc.add(r = new Radio(" Current location",rg),LEFT+50,AFTER+50,FILL,PREFERRED); r.leftJustify = true;
         sc.add(r = new Radio(" Address",rg),LEFT+50,AFTER+100,FILL,PREFERRED); r.leftJustify = true;
         sc.add(edAddr = new Edit(),LEFT+100,AFTER+50,FILL-200,PREFERRED);
         sc.add(r = new Radio(" Coordinates",rg),LEFT+50,AFTER+100,FILL,PREFERRED); r.leftJustify = true;
         sc.add(new Label("Lat: "),LEFT+50,AFTER+50);
         sc.add(edLat = new Edit("+99.999999"), AFTER+50,SAME);
         sc.add(new Label("Lon: "),LEFT+50,AFTER+50);
         sc.add(edLon = new Edit("+99.999999"), SAME,AFTER+50,edLat); // vertical align
         sc.add(chSat = new Check("Show satellite images"),LEFT+50,AFTER+100,FILL,PREFERRED); chSat.leftJustify = true;
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
      edAddr.setEnabled(idx == 1);
      edLat.setEnabled(idx == 2);
      edLon.setEnabled(idx == 2);
   }
   
   private MessageBox mbgps;
   private boolean gpsloop;

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
               {
                  Vm.alert("oi");
                  gpsloop = false;
               }
               else
               if (e.target == btnShow)
               {
                  String addr = "";
                  int sel = rg.getSelectedIndex();
                  switch (sel)
                  {
                     case 0:
                        mbgps = new MessageBox("","Starting GPS. Wait\nfor max 10 seconds.",new String[]{"Cancel"});
                        mbgps.popupNonBlocking();
                        GPS gps = null;
                        gpsloop = true;
                        try
                        {
                           add(gps = new GPS(),10000,100); // place outside screen
                           for (int i = 0; i < 40 && gpsloop && gps.location[0] == 0; i++)
                              Vm.safeSleep(250);
                        }
                        catch (Exception ioe) 
                        {
                           MessageBox mb = new MessageBox("Error","An error occured while activating GPS: "+ioe.getMessage()+". Will use the last knwon location (if any).",null);
                           mb.popupNonBlocking();
                           Vm.safeSleep(4000);
                           mb.unpop();
                        }
                        finally
                        {
                           mbgps.unpop();
                           if (gps != null)
                              gps.stop();
                        }
                        addr = "";
                        if (!gpsloop) // user cancelled?
                           return;
                        break;
                     case 1: 
                        addr = edAddr.getText();
                        break;
                     case 2:
                        addr = "@"+edLat.getText()+","+edLon.getText();
                        break;
                  }
                  MessageBox mb = sel != 1 ? null : new MessageBox("Wait","Searching location...",null);
                  if (mb != null)
                     mb.popupNonBlocking();
                  boolean ok = false;
                  try
                  {
                     ok = totalcross.map.GoogleMaps.showAddress(addr,chSat.isChecked());
                  }
                  finally
                  {
                     if (mb != null)
                        mb.unpop();
                  }
                  if (!ok)
                  {
                     mb = new MessageBox("","Failed to show the map",null);
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
      Image exitImg = new Image("exit.png");
      int c1 = 0x0A246A;
      Font f = font.adjustedBy(2,true);
      final Bar headerBar = new Bar("Google Maps Sample");
      headerBar.setFont(f);
      headerBar.setBackForeColors(c1,Color.WHITE);
      headerBar.addButton(exitImg);
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
