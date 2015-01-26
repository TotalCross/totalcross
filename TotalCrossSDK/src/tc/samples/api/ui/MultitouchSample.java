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

public class MultitouchSample extends BaseContainer
{
   ImageControl ic;
   public static Image screenShot;
   private static Image lata;

   public void initUI()
   {
      super.initUI();
      isSingleCall = true;
      
      if (!Settings.isOpenGL && !Settings.onJavaSE)
         add(new Label("This sample works only on iOS, Android and Windows Phone."),CENTER,CENTER);
      else
      try
      {
         super.initUI();
         if (lata == null)
            lata = new Image("ui/images/lata.jpg");
         ic = new ImageControl(screenShot != null ? screenShot : lata);
         screenShot = null;
         ic.allowBeyondLimits = false;
         ic.setEventsEnabled(true);
         updateStatus();
         add(ic,LEFT+gap,TOP+gap,FILL-gap,FILL-gap);
         ic.addMultiTouchListener(new MultiTouchListener()
         {
            public void scale(MultiTouchEvent e)
            {
               updateStatus();
            }
         });
      }
      catch (Exception e)
      {
         MessageBox.showException(e,false);
      }
   }
   
   private void updateStatus()
   {
      setInfo(ic.getImageWidth()+"x"+ic.getImageHeight());
   }
}