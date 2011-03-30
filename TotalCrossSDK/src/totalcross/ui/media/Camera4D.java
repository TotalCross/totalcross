/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
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

// $Id: Camera4D.java,v 1.5 2011-01-04 13:18:58 guich Exp $

package totalcross.ui.media;

import totalcross.io.IOException;
import totalcross.sys.Settings;
import totalcross.ui.Button;
import totalcross.ui.Control;
import totalcross.ui.Window;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.PressListener;

public class Camera4D
{
   public String initialDir;
   public String defaultFileName;
   public String title;

   private Window cameraScreen;

   public int stillQuality;

   public int videoType = CAMERACAPTURE_VIDEOTYPE_ALL;
   public int resolutionWidth;
   public int resolutionHeight;
   public int videoTimeLimit;

   public int captureMode = CAMERACAPTURE_MODE_STILL;

   public static final int CAMERACAPTURE_MODE_STILL = 0;
   public static final int CAMERACAPTURE_MODE_VIDEOONLY = 1;
   public static final int CAMERACAPTURE_MODE_VIDEOWITHAUDIO = 2;
   public static final int CAMERACAPTURE_STILLQUALITY_DEFAULT = 0;
   public static final int CAMERACAPTURE_STILLQUALITY_LOW = 1;
   public static final int CAMERACAPTURE_STILLQUALITY_NORMAL = 2;
   public static final int CAMERACAPTURE_STILLQUALITY_HIGH = 3;

   public static final int CAMERACAPTURE_VIDEOTYPE_ALL = 0xFFFF;
   public static final int CAMERACAPTURE_VIDEOTYPE_STANDARD = 1;
   public static final int CAMERACAPTURE_VIDEOTYPE_MESSAGING = 2;

   public Camera4D()
   {
   }

   public String click() throws IOException
   {
      if (Settings.platform.equals(Settings.PALMOS))
      {
         cameraScreen = new Window();
         Button bOk = new Button("Ok");
         bOk.appObj = this;
         cameraScreen.add(bOk, Control.LEFT, Control.BOTTOM);
         initCamera();
         bOk.addPressListener(new PressListener()
         {
            public void controlPressed(ControlEvent e)
            {
               Button bOk = (Button) e.target;
               Camera4D camera = (Camera4D) bOk.appObj;
               camera.defaultFileName = camera.nativeClick();
               bOk.getParentWindow().unpop();
            }
         });
         cameraScreen.popup();
      }
      else if (Settings.isWindowsDevice() || Settings.platform.equals(Settings.WIN32) || Settings.platform.equals(Settings.ANDROID))
         return this.nativeClick();
      else
         new totalcross.ui.dialog.MessageBox("Camera (Emulation)", "Say cheese!", new String[] { "Click" }).popup();

      return defaultFileName;
   }
   
   protected void finalize() throws Throwable
   {
      nativeFinalize();
   }
   
   native private String nativeClick();

   native private void initCamera();
   
   native private void nativeFinalize();
}
