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

package tc.samples.ui.image.multitouch;

import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.image.*;

public class MultiTouchTest extends MainWindow
{
   Image img;
   ImageControl ic;
   Label l;

   public MultiTouchTest()
   {
      super("MultiTouch Test", TAB_ONLY_BORDER);
   }

   public void initUI()
   {
      if (!Settings.isOpenGL && !Settings.onJavaSE)
         add(new Label("This sample works only on iOS and Android."),CENTER,CENTER);
      else
      try
      {
         add(l = new Label("",RIGHT),LEFT,0,FILL,PREFERRED);
         l.transparentBackground = true;
         img = new Image("tc/samples/ui/image/multitouch/lata.jpg");
         ic = new ImageControl(img);
         ic.allowBeyondLimits = false;
         ic.setEventsEnabled(true);
         updateStatus();
         add(ic,LEFT,TOP,FILL,FILL);
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
      l.setText(ic.getImageWidth()+"x"+ic.getImageHeight());
   }
}
