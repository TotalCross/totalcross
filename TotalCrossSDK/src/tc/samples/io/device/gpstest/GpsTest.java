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



package tc.samples.io.device.gpstest;

import totalcross.io.device.gps.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;

public class GpsTest extends MainWindow
{
   Container gpsCont;
   GPSView gps;
   Button btnExit,btnGo;
   
   public GpsTest()
   {
      super("GpsTest", TAB_ONLY_BORDER);
   }

   public void initUI()
   {
      add(btnExit = new Button("  Exit  "),RIGHT,TOP,PARENTSIZE+10,PREFERRED);
      add(btnGo = new Button("Connect"),LEFT,TOP,PARENTSIZE+80,PREFERRED);
      add(gpsCont = new Container(),LEFT,AFTER,FILL,FILL);
   }
   
   public void onEvent(Event e)
   {
      if (e.type == ControlEvent.PRESSED)
         if (e.target == btnExit)
            exit(0);
         else
         if (e.target == btnGo)
         try
         {
            if (gps != null)
            {
               gps.stop();
               gpsCont.remove(gps);
               gps = null;
            }
            gps = new GPSView(500);
            gpsCont.add(gps, LEFT+2,TOP+2,FILL-2,FILL-2);
         }
         catch (Exception ee)
         {
            MessageBox.showException(ee, true);
         }
   }
}
