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

package totalcross.ui.media;

import totalcross.io.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.util.*;

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
   
   public boolean allowRotation;

   public int cameraType;
   public static String fcbTitle = "Select a photo";

   public static final int CAMERA_CUSTOM = 0;
   public static final int CAMERA_NATIVE = 1;
   public static final int CAMERA_NATIVE_NOCOPY = 2;

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
      if (Settings.isWindowsDevice() || Settings.platform.equals(Settings.WIN32) || Settings.isIOS() || Settings.platform.equals(Settings.ANDROID) || Settings.platform.equals(Settings.WINDOWSPHONE))
      {
         if (initialDir != null) try {new File(initialDir).createDir();} catch (Exception e) {}
         return this.nativeClick();
      }
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

   public static String[] getSupportedResolutions()
   {
      String[] ret = null;
      if (Settings.platform.equals(Settings.ANDROID))
      {
         String s = getNativeResolutions();
         if (s != null)
            ret = sortResolutions(Convert.tokenizeString(s,','));
      }
      else
      if (Settings.isWindowsDevice())
      {
         Vector v = new Vector(10);
         String dir = "Software\\Microsoft\\Pictures\\Camera\\OEM\\PictureResolution";
         String[] folders = Registry.list(Registry.HKEY_LOCAL_MACHINE,dir);
         for (int i =0; i < folders.length; i++)
         {
            String f = folders[i];
            String fullKey = dir+"\\"+f;
            try
            {
               int w = Registry.getInt(Registry.HKEY_LOCAL_MACHINE, fullKey, "Width");
               int h = Registry.getInt(Registry.HKEY_LOCAL_MACHINE, fullKey, "Height");
               v.addElement(w+"x"+h);
            }
            catch (Exception e) {} // key not found
         }
         if (v.size() > 0)
            ret = sortResolutions((String[])v.toObjectArray());
      }
      if (ret == null)
         ret = new String[]{"default resolution","320x240","640x480","1024x768","2048x1536"};
      return ret;
   }
   private static String[] sortResolutions(String[] res)
   {
      for (int i = res.length; --i >= 0;) {String[] sp = Convert.tokenizeString(res[i],'x'); res[i] = Convert.zeroPad(sp[0],5)+"x"+Convert.zeroPad(sp[1],5);}
      Convert.qsort(res,0,res.length-1);
      for (int i = res.length; --i >= 0;) {String[] sp = Convert.tokenizeString(res[i],'x'); res[i] = Convert.zeroUnpad(sp[0])+"x"+Convert.zeroUnpad(sp[1]);}
      return res;
   }

   static native private String getNativeResolutions();
}
