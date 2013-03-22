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

import totalcross.io.device.*;
import totalcross.io.device.gps.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;

public class GpsTest extends MainWindow
{
   ComboBox cbCom,cbBaud;
   Container gpsCont;
   GPSView gps;
   Button btn;
   
   public GpsTest()
   {
      super("GpsTest", TAB_ONLY_BORDER);
   }

   public void initUI()
   {
      add(btn = new Button("  Exit  "),RIGHT,TOP);
      if (Settings.platform.equals(Settings.ANDROID) || Settings.isWindowsDevice())
         add(new Label("In "+Settings.platform+" platform\nyou should select AUTODETECT\nas COM port"),CENTER,AFTER);
      add(new Label("1. Select baudRate: "),LEFT,AFTER);
      add(cbBaud = new ComboBox(),AFTER,SAME,FILL,PREFERRED);
      cbBaud.add("1200");
      cbBaud.add("2400");
      cbBaud.add("4800");
      cbBaud.add("9600");
      cbBaud.add("14400");
      cbBaud.add("19200");
      cbBaud.add("38400");
      cbBaud.add("57600");
      cbBaud.add("115200");
      cbBaud.setSelectedItem("9600");
      add(new Label("2. Select COM port: "),LEFT,AFTER);
      add(cbCom = new ComboBox(),AFTER,SAME,FILL,PREFERRED);
      cbCom.add("Autodetect");
      for (int i =0; i <= 10; i++)
         cbCom.add("COM"+i);
      add(gpsCont = new Container(),LEFT,AFTER,FILL,FILL);
   }
   
   public void onEvent(Event e)
   {
      if (e.type == ControlEvent.PRESSED)
         if (e.target == btn)
            exit(0);
         else
         if ((e.target == cbCom || e.target == cbBaud) && cbCom.getSelectedIndex() != -1)
         try
         {
            if (gps != null)
            {
               gps.stop();
               gpsCont.remove(gps);
               gps = null;
            }
            if (cbCom.getSelectedIndex() == 0)
               gps = new GPSView(500);
            else
            {
               int port = Convert.chars2int((String)cbCom.getSelectedItem());
               int baud = Convert.toInt((String)cbBaud.getSelectedItem());
               PortConnector pc = new PortConnector(port, baud);
               pc.setFlowControl(false);
               pc.readTimeout = 1000;
               gps = new GPSView(pc, 500);
            }
            gpsCont.add(gps, LEFT+2,TOP+2,FILL-2,FILL-2);
         }
         catch (Exception ee)
         {
            MessageBox.showException(ee, true);
         }
   }
}
