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
import totalcross.ui.dialog.*;

/**
 * This class is used to enable the camera of the underlying device. The following platforms are supported:
 * Windows Mobile devices, Blackberry, Palm OS, Android and iOS.
 * 
 * For more information about the possible parameters on Windows, see: http://msdn2.microsoft.com/en-us/library/bb431794.aspx 
 * 
 * See the CameraTest sample. 
 * 
 * Note that you can easily rotate the image to put it in portrait mode, using the
 * <code>Image.getRotatedScaledInstance</code> method, after retrieving the image. 
 * 
 * To use this class with the Hand Held Dolphin barcode scanners, you must install
 * TotalCross3/dist/vm/wince/POCKETPC/ARM/Dolphin.dll. Only 640x480 photos are supported, and you may change the
 * following options:
 * <ul>
 * <li>initialDir
 * <li>defaultFileName (must end with jpg)
 * <li>resolutionWidth x resolutionHeight: possible values are 640x480 and 212x160 (different values defaults to
 * 640x480)
 * </ul>
 * All other options are ignored.
 * 
 * In Android you can set the defaultFileName, stillQuality, resolutionWidth and resolutionHeight. All other options are ignored.
 * 
 * You can call the getSupportedResolutions method to see the resolutions that are available at the device. 
 * 
 * In iOS there's no way to return the supported resolutions; it will take a photo using the default camera's resolution, and 
 * then will resize to the resolution defined in resolutionWidth/resolutionHeight, keeping the camera's aspect ratio. In iOS you can specify the defaultFileName with a path or just the name, or use a system-generated name. 
 * 
 * In Android, you can use the internal camera by setting the cameraType field.

 * 
 * @see #getSupportedResolutions()
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
    * @see #CAMERACAPTURE_STILLQUALITY_DEFAULT
    * @see #CAMERACAPTURE_STILLQUALITY_LOW
    * @see #CAMERACAPTURE_STILLQUALITY_NORMAL
    * @see #CAMERACAPTURE_STILLQUALITY_HIGH
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
   
   /**
    * This field is false by default so that the default camera orientation is still landscape. 
	 * If this is set to true, the camera orientation will follow the device orientation.
	 * Used on Android only.
	 */
   public boolean allowRotation;

   /** The camera type; defaults to CAMERA_CUSTOM. */
   public int cameraType;

   /** The original camera used in TotalCross */
   public static final int CAMERA_CUSTOM = 0;
   /** The native camera application; a copy of the image is returned. */      
   public static final int CAMERA_NATIVE = 1;
   /** The native camera application; the original image is deleted and a copy of it is returned. */
   public static final int CAMERA_NATIVE_NOCOPY = 2;
   
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

   /** The default title when used in desktop */
   public static String fcbTitle = "Select a photo";
   
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
      FileChooserBox fcb = new FileChooserBox(fcbTitle,FileChooserBox.defaultButtonCaptions,null);
      fcb.showPreview = true;
      fcb.popup();
      return fcb.getAnswer();
   }

   /** Returns the resolutions that are supported by the device. Works for Windows CE, Blackberry and Android.
    * 
    * When the information is not available, a default list is returned and the first element is set to "Default values:".
    * 
    * If you take a photo and an image with a different resolution is returned, its because the selected resolution is not supported by the device.
    * 
    * Values are always in LANDSCAPE.
    * 
    * @since TotalCross 1.3
    */
   public static String[] getSupportedResolutions()
   {
      return new String[]{"320x240","640x480","1024x768","2048x1536"};
   }
}
