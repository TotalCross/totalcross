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

import totalcross.ui.*;
import totalcross.ui.anim.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;

public class AnimationControlSample extends BaseContainer
{
   Button btnL,btnR,btnT,btnB,btnS;
   
   public void initUI()
   {
      super.initUI();
      Container c = new Container();
      c.setBackColor(0xEEEEEE);
      add(c,CENTER,CENTER,PARENTSIZE+90,PARENTSIZE+90);
      Button.commonGap = fmH;
      c.add(btnL = new Button("Left"),LEFT,CENTER);     btnL.appId = 1;
      c.add(btnR = new Button("Right"),RIGHT,CENTER);   btnR.appId = 2;
      c.add(btnT = new Button("Top"),CENTER,TOP);       btnT.appId = 3;
      c.add(btnB = new Button("Bottom"),CENTER,BOTTOM); btnB.appId = 4;
      c.add(btnS = new Button("SWAP"),CENTER,CENTER);   btnS.appId = 5;
      Button.commonGap = 0;
   }

   public void onEvent(Event e)
   {
      try
      {
         if (e.type == ControlEvent.PRESSED)
            switch (((Control)e.target).appId)
            {
               case 1: (PathAnimation.create(btnL,-LEFT).then(PathAnimation.create(btnL,LEFT))).start(); break;
               case 2: (PathAnimation.create(btnR,-RIGHT).then(PathAnimation.create(btnR,RIGHT))).start(); break;
               case 3: (PathAnimation.create(btnT,-TOP).then(PathAnimation.create(btnT,TOP))).start(); break;
               case 4: (PathAnimation.create(btnB,-BOTTOM).then(PathAnimation.create(btnB,BOTTOM))).start(); break;
               case 5: 
                  int xr = btnR.getX(), yr = btnR.getY(), xl = btnL.getX(), yl = btnL.getY();
                  (PathAnimation.create(btnR,xr,yr,xl,yl,null).then(PathAnimation.create(btnR,xl,yl,xr,yr,null))).start(); 
                  (PathAnimation.create(btnL,xl,yl,xr,yr,null).then(PathAnimation.create(btnL,xr,yr,xl,yl,null))).start(); 
                  break;
            }
      }
      catch (Exception ee)
      {
         MessageBox.showException(ee,true);
      }
   }
}
