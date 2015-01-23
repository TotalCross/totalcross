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



package tc.samples.app.watch;

import totalcross.sys.Settings;
import totalcross.ui.Button;
import totalcross.ui.Container;
import totalcross.ui.MainWindow;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.PressListener;

/** A set of world watchs. */

public class WorldWatch extends MainWindow
{
   Container watches;
   Button btExit;

   static
   {
      Settings.isFullScreen = true;
   }

   public WorldWatch()
   {
      setUIStyle(Settings.Flat);
   }

   private void addWatch(int x, int y, int city)
   {
      Watch w = new Watch();
      watches.add(w);
      w.setRect(x, y, Settings.screenWidth / 2, Settings.screenHeight / 2);
      w.setCity(city);
   }

   public void initUI()
   {
      add(watches = new Container(), LEFT, TOP, FILL, FILL);
      add(btExit = new Button("Exit"), CENTER, BOTTOM, PREFERRED + 15, PREFERRED);
      btExit.addPressListener(new PressListener()
      {
         public void controlPressed(ControlEvent e)
         {
            MainWindow.exit(0);
         }
      });
     

      addWatch(LEFT, TOP, 49);
      addWatch(RIGHT, TOP, 45);
      addWatch(LEFT, BOTTOM, 47);
      addWatch(RIGHT, BOTTOM, 35);
   }
}
