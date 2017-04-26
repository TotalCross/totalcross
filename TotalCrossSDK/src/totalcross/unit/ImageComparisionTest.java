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



package totalcross.unit;

import totalcross.ui.gfx.*;
import totalcross.sys.*;
import totalcross.ui.*;

/** This is a special TestCase, used to compare two images. One image is
 * created in a recording mode at desktop and the other image is the first
 * one running at device.
 */

public class ImageComparisionTest extends TestCase
{
   // set to true to record the compressed strings
   protected boolean recording;
   // set to true to debug, sending all output to the screen.
   protected boolean debuggingOnScreen;

   protected Graphics maing;
   protected ImageTester it;

   public void testRun()
   {
      maing = MainWindow.getMainWindow().getGraphics();
   }

   protected void assertOK(String in256, String in65536, String title)
   {
      if (debuggingOnScreen)
         ;
      else
      if (recording)
      {
         maing.drawImage(it,0,0);
         if (!Settings.onJavaSE)
            Vm.sleep(10000);
         Vm.debug("private String "+title+"_"+Settings.screenBPP+" = \""+it.toString()+"\";");
      }
      else
      {
         it.title = title;
         assertEquals(it, Settings.screenBPP == 8 ? in256 : in65536);
      }
   }
}
