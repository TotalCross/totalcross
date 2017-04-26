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

public class ControlAnimationSample extends BaseContainer
{
   Button btnL,btnR,btnT,btnB,btnSwp,btnRot,btnSH;
   Container c;
   
   public void initUI()
   {
      super.initUI();
      c = new Container();
      Button.commonGap = fmH/2;
      add(btnSwp = new Button("SWAP"),LEFT,TOP+fmH/4);        btnSwp.appId = 5;
      add(btnSH  = new Button("SHOW/HIDE"),CENTER,TOP+fmH/4); btnSH .appId = 6;
      add(btnRot = new Button("ROTATE"),RIGHT,TOP+fmH/4);     btnRot.appId = 7;
      c.setBackColor(0xEEEEEE);
      add(c,LEFT+fmH,AFTER+fmH,FILL-fmH,FILL-fmH);
      c.add(btnB = new Button("Bottom"),CENTER,BOTTOM);         btnB.appId = 4;
      c.add(btnR = new Button("Right"),RIGHT,CENTER,SAME,SAME); btnR.appId = 2;
      c.add(btnL = new Button("Left"),LEFT,CENTER,SAME,SAME);   btnL.appId = 1;
      c.add(btnT = new Button("Top"),CENTER,TOP,SAME,SAME);     btnT.appId = 3;
      Button.commonGap = 0;
   }

   public void onEvent(Event e)
   {
      try
      {
         if (e.type == ControlEvent.PRESSED)
            switch (((Control)e.target).appId)
            {
               case 1: PathAnimation.create(btnL,-LEFT,null,-1).then(PathAnimation.create(btnL,LEFT,null,-1)).start(); break;
               case 2: PathAnimation.create(btnR,-RIGHT,null,-1).then(PathAnimation.create(btnR,RIGHT,null,-1)).start(); break;
               case 3: PathAnimation.create(btnT,-TOP,null,-1).then(PathAnimation.create(btnT,TOP,null,-1)).start(); break;
               case 4: PathAnimation.create(btnB,-BOTTOM,null,-1).then(PathAnimation.create(btnB,BOTTOM,null,-1)).start(); break;
               case 5:
               {
                  int xr = btnR.getX(), yr = btnR.getY(), xl = btnL.getX(), yl = btnL.getY();
                  PathAnimation.create(btnR,xr,yr,xl,yl,null,-1).then(PathAnimation.create(btnR,xl,yl,xr,yr,null,-1)).start(); 
                  PathAnimation.create(btnL,xl,yl,xr,yr,null,-1).then(PathAnimation.create(btnL,xr,yr,xl,yl,null,-1)).start(); 
                  break;
               }
               case 6: (FadeAnimation.create(c,false,null,-1).then(FadeAnimation.create(c,true,null,-1))).start(); break;
               case 7:
               {
                  int xr = btnR.getX(), yr = btnR.getY(), xl = btnL.getX(), yl = btnL.getY();
                  int xt = btnT.getX(), yt = btnT.getY(), xb = btnB.getX(), yb = btnB.getY();
                  PathAnimation p1 = PathAnimation.create(btnL,xt,yt,null,500);
                  PathAnimation p2 = PathAnimation.create(btnT,xr,yr,null,500);
                  PathAnimation p3 = PathAnimation.create(btnR,xb,yb,null,500);
                  PathAnimation p4 = PathAnimation.create(btnB,xl,yl,null,500);
                  p1.with(p2); p2.with(p3); p3.with(p4);
                  p1.start(); 
                  
                  break;
               }
            }
      }
      catch (Exception ee)
      {
         MessageBox.showException(ee,true);
      }
   }
}
