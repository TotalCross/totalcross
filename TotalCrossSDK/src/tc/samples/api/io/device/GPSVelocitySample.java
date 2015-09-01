/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2014 SuperWaba Ltda.                                      *
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

import totalcross.io.device.gps.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.chart.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;

import tc.samples.api.*;

public class GPSVelocitySample extends BaseContainer
{
   Velocimeter vel;
   TimerEvent tt;
   GPS gps;
   Label l;
   double lastVel;
   
   public void initUI()
   {
      try
      {
         super.initUI();
         vel = new Velocimeter();
         vel.value = 0;
         vel.max = 120;
         vel.pointerColor = Color.WHITE;
         add(vel,CENTER,CENTER,PARENTSIZE+50,PARENTSIZE+50);
         add(l = new Label("",CENTER),LEFT,AFTER+50);
         gps = new GPS();
         tt = addTimer(50);
      }
      catch (GPSDisabledException gde)
      {
         Toast.show("GPS is disabled, please enable it!",2000);
         super.back();
      }
      catch (Exception e)
      {
         MessageBox.showException(e,true);
         back();
      }
   }
   
   public void back()
   {
      super.back();
      if (gps != null) gps.stop();
   }
   
   public void onEvent(Event e)
   {
      if (e.type == TimerEvent.TRIGGERED && tt.triggered)
         try
         {
            if (gps.retrieveGPSData())
            {
               double v = gps.velocity;
               if (v < 0) v = 0;
               if (v != lastVel)
               {
                  lastVel = v;
                  vel.value = (int)(v * 3.6); // m/s -> km/h
                  l.setText(vel.value+" km/h");
               }
            }
         }
         catch (Exception ee)
         {
            Toast.show("Error: "+ee.getMessage(),2000);
         }
   }
}