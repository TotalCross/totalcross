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

package tc.samples.api.ui;

import tc.samples.api.*;

import totalcross.ui.chart.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;

public class VelocimeterSample extends BaseContainer
{
   Velocimeter vel;
   TimerEvent tt;
   
   public void initUI()
   {
      try
      {
         super.initUI();
         tt = addTimer(50);
         vel = new Velocimeter();
         vel.value = -20;
         vel.max = 40;
         vel.pointerColor = Color.GREEN;
         add(vel,CENTER,CENTER,PARENTSIZE+50,PARENTSIZE+50);
      }
      catch (Exception e)
      {
         MessageBox.showException(e,true);
         back();
      }
   }
   
   public void onEvent(Event e)
   {
      if (e.type == TimerEvent.TRIGGERED && tt.triggered)
      {
         vel.value++;
         if (vel.value > vel.max+20)
            vel.value = vel.min-20;
         repaint();
      }
   }
}