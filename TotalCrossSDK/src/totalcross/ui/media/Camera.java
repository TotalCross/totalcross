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

// $Id: Camera.java,v 1.9 2011-01-04 13:18:58 guich Exp $

package totalcross.ui.media;

import totalcross.io.IOException;

/**
 * This class is used to enable the camera of the underlying device. The following platforms are supported:
 * Windows Mobile devices, Blackberry, Palm OS and Android.<br>
 * <br>
 * For more information about the possible parameters on Windows, see: http://msdn2.microsoft.com/en-us/library/bb431794.aspx <br>
 * <br>
 * See the CameraTest sample. <br>
 * <br>
 * Note that you can easily rotate the image to put it in portrait mode, using the
 * <code>Image.getRotatedScaledInstance</code> method, after retrieving the image. <br>
 * <br>
 * To use this class with the Hand Held Dolphin barcode scanners, you must install
 * TotalCrossSDK/dist/vm/wince/POCKETPC/ARM/Dolphin.dll. Only 640x480 photos are supported, and you may change the
 * following options:
 * <ul>
 * <li>initialDir
 * <li>defaultFileName (must end with jpg)
 * <li>resolutionWidth x resolutionHeight: possible values are 640x480 and 212x160 (different values defaults to
 * 640x480)
 * </ul>
 * All other options are ignored.
 * <br><br>
 * In Android, it will open the Gallery program. Press menu, then "Capture picture", take the photo, press Back, select the photo, and that's it.
 * Android has an api that goes directly to the photo program, however, its api is buggy and only a thumbnail image is returned. Using the implemented
 * way, a full-sized image is returned. The only parameter that works in Android is defaultFileName; all the others are ignored.
 */

public class Camera
{
   /** The initial directory. */
   public String initialDir;
   /** The default file name. */
   public String defaultFileName;
   /** The title to display in the window. */
   public String title;

   /**
    * The still quality.
    * 
    * @see #CAMERACAPTURE_MODE_STILL
    * @see #CAMERACAPTURE_MODE_VIDEOONLY
    * @see #CAMERACAPTURE_MODE_VIDEOWITHAUDIO
    */
   public int stillQuality;
   /**
    * Video type; defaults to ALL.
    * 
    * @see #CAMERACAPTURE_VIDEOTYPE_ALL
    * @see #CAMERACAPTURE_VIDEOTYPE_STANDARD
    * @see #CAMERACAPTURE_VIDEOTYPE_MESSAGING
    */
   public int videoType = CAMERACAPTURE_VIDEOTYPE_ALL;
   /** The width for the resolution */
   public int resolutionWidth;
   /** The height for the resolution */
   public int resolutionHeight;
   /** Maximum time limit for recording a video. */
   public int videoTimeLimit;
   /**
    * Capture mode; defaults to STILL.
    * 
    * @see #CAMERACAPTURE_MODE_STILL
    * @see #CAMERACAPTURE_MODE_VIDEOONLY
    * @see #CAMERACAPTURE_MODE_VIDEOWITHAUDIO
    */
   public int captureMode = CAMERACAPTURE_MODE_STILL;

   /** Used in the cameraMode member. */
   public static final int CAMERACAPTURE_MODE_STILL = 0;
   /** Used in the cameraMode member. */
   public static final int CAMERACAPTURE_MODE_VIDEOONLY = 1;
   /** Used in the cameraMode member. */
   public static final int CAMERACAPTURE_MODE_VIDEOWITHAUDIO = 2;

   /** Used in the videoType member. */
   public static final int CAMERACAPTURE_STILLQUALITY_DEFAULT = 0;
   /** Used in the videoType member. */
   public static final int CAMERACAPTURE_STILLQUALITY_LOW = 1;
   /** Used in the videoType member. */
   public static final int CAMERACAPTURE_STILLQUALITY_NORMAL = 2;
   /** Used in the videoType member. */
   public static final int CAMERACAPTURE_STILLQUALITY_HIGH = 3;

   /** Used in the captureMode member. */
   public static final int CAMERACAPTURE_VIDEOTYPE_ALL = 0xFFFF;
   /** Used in the captureMode member. */
   public static final int CAMERACAPTURE_VIDEOTYPE_STANDARD = 1;
   /** Used in the captureMode member. */
   public static final int CAMERACAPTURE_VIDEOTYPE_MESSAGING = 2;

   public Camera()
   {
   }

   /**
    * Takes a photo based on the members set.
    * 
    * @return The String with the file name where the image is located or null if the user canceled.
    * @throws IOException
    *            if any other error occurs.
    */
   public String click() throws IOException
   {
      new totalcross.ui.dialog.MessageBox("Camera", "Say cheese!", new String[] { "Click" }).popup();
      return defaultFileName;
   }
}
