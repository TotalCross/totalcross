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

import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.image.*;

public class ImageControlSample extends BaseContainer
{
   Image img;
   TimerEvent timer;
   boolean grow=true;
   ImageControl ic;
   Label lab;
   boolean stopped;
   double minScale, maxScale;
   
   public void initUI()
   {
      try
      {
         super.initUI();
         isSingleCall = true;
         
         setTitle("Scale with ImageControl");
         add(lab = new Label("To Mirian",CENTER),LEFT,TOP,FILL,PREFERRED);
         img = new Image("ui/images/heart.png");
         int nw = parent.getWidth()/2, nh = parent.getHeight()/2;
         if (img.getWidth() > nw || img.getHeight() > nh)
            img = img.smoothScaledFixedAspectRatio(nw < nh ? nw : nh, nh < nw);
         ic = new ImageControl(img);
         ic.centerImage = true;
         add(ic,LEFT,AFTER,FILL,FILL);
         timer = addTimer(25);
         maxScale = Settings.isWindowsDevice() ? 2 : 5;
         minScale = Settings.isWindowsDevice() ? 1 : 0.5;
      }
      catch (Exception ee)
      {
         MessageBox.showException(ee,true);
      }
   }
   public void onEvent(Event e)
   {
      try
      {
         if (e.type == TimerEvent.TRIGGERED && timer != null && timer.triggered && !stopped)
         {
            if (img.hwScaleH > maxScale)
               grow = false;
            img.hwScaleH = img.hwScaleW = img.hwScaleH * (grow ? 1.05 : 0.95);
            if (!grow && img.hwScaleH <= minScale)
               grow = true;
            ic.setImage(img); // this actually just computes the center position
         }
         else
         if (e.type == PenEvent.PEN_UP && e.target == ic)
         {
            stopped = true;
            InputBox ib = new InputBox("Change name","(don't type \"To\")","");
            ib.popup();
            String s = ib.getValue();
            if (s != null)
               lab.setText("To "+s);
            stopped = false;
         }
      }
      catch (Throwable ee)
      {
         ee.printStackTrace();
         Toast.show("Exception: "+ee.getClass().getName(),2000);
      }
   }
   public void onRemove()
   {
      removeTimer(timer);
      timer = null;
   }
}