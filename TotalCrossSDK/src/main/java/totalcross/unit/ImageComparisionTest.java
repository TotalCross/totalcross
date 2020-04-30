// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.unit;

import totalcross.sys.Settings;
import totalcross.sys.Vm;
import totalcross.ui.MainWindow;
import totalcross.ui.gfx.Graphics;

/** This is a special TestCase, used to compare two images. One image is
 * created in a recording mode at desktop and the other image is the first
 * one running at device.
 */

public class ImageComparisionTest extends TestCase {
  // set to true to record the compressed strings
  protected boolean recording;
  // set to true to debug, sending all output to the screen.
  protected boolean debuggingOnScreen;

  protected Graphics maing;
  protected ImageTester it;

  @Override
  public void testRun() {
    maing = MainWindow.getMainWindow().getGraphics();
  }

  protected void assertOK(String in256, String in65536, String title) {
    if (debuggingOnScreen) {
      ;
    } else if (recording) {
      maing.drawImage(it, 0, 0);
      if (!Settings.onJavaSE) {
        Vm.sleep(10000);
      }
      Vm.debug("private String " + title + "_" + Settings.screenBPP + " = \"" + it.toString() + "\";");
    } else {
      it.title = title;
      assertEquals(it, Settings.screenBPP == 8 ? in256 : in65536);
    }
  }
}
