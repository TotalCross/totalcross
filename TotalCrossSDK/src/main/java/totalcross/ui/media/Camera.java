// Copyright (C) 2000-2013 SuperWaba Ltda. 
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.media;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import com.totalcross.annotations.ReplacedByNativeOnDeploy;

import totalcross.io.File;
import totalcross.io.IOException;
import totalcross.sys.Convert;
import totalcross.sys.Registry;
import totalcross.sys.Settings;
import totalcross.ui.dialog.FileChooserBox;

/**
 * This class is used to enable the camera of the underlying device. The following platforms are
 * supported: Windows Mobile devices, Blackberry, Palm OS, Android and iOS.
 *
 * <p>For more information about the possible parameters on Windows, see:
 * http://msdn2.microsoft.com/en-us/library/bb431794.aspx
 *
 * <p>See the CameraTest sample.
 *
 * <p>Note that you can easily rotate the image to put it in portrait mode, using the <code>
 * Image.getRotatedScaledInstance</code> method, after retrieving the image.
 *
 * <p>To use this class with the Hand Held Dolphin barcode scanners, you must install
 * TotalCross3/dist/vm/wince/POCKETPC/ARM/Dolphin.dll. Only 640x480 photos are supported, and you
 * may change the following options:
 *
 * <ul>
 *   <li>initialDir
 *   <li>defaultFileName (must end with jpg)
 *   <li>resolutionWidth x resolutionHeight: possible values are 640x480 and 212x160 (different
 *       values defaults to 640x480)
 * </ul>
 *
 * All other options are ignored.
 *
 * <p>In Android you can set the defaultFileName, stillQuality, resolutionWidth and
 * resolutionHeight. All other options are ignored.
 *
 * <p>You can call the getSupportedResolutions method to see the resolutions that are available at
 * the device.
 *
 * <p>In iOS there's no way to return the supported resolutions; it will take a photo using the
 * default camera's resolution, and then will resize to the resolution defined in
 * resolutionWidth/resolutionHeight, keeping the camera's aspect ratio. In iOS you can specify the
 * defaultFileName with a path or just the name, or use a system-generated name.
 *
 * <p>In Android, you can use the internal camera by setting the cameraType field.
 *
 * @see #getSupportedResolutions()
 */
public class Camera {
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
   * This field is false by default so that the default camera orientation is still landscape. If
   * this is set to true, the camera orientation will follow the device orientation. Used on Android
   * only.
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
  /** Take the picture from the gallery. */
  public static final int CAMERA_FROM_GALLERY = 3;

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

  public Camera() {}

  /**
   * Takes a photo based on the members set.
   *
   * @return The String with the file name where the image is located or null if the user canceled.
   * @throws IOException if any other error occurs.
   */
  public String click() throws IOException {
    if (Settings.onJavaSE
        || Settings.isWindowsCE()
        || Settings.platform.equals(Settings.WIN32)
        || Settings.isIOS()
        || Settings.platform.equals(Settings.ANDROID)) {
      if (initialDir != null) {
        try {
            new File(initialDir).createDir();
        } catch (IOException e) {
            e.printStackTrace();
        }
      }
      
      String output = this.nativeClick();
      if (Settings.platform.equals(Settings.ANDROID) && cameraType == CAMERA_FROM_GALLERY) {
	    try {
	    	File f = new File(output, File.READ_WRITE);
	    	if (f.getSize() < 500) { // If less than 500 bytes long it is likely a path, else a image
	    		byte[] data = f.readAndDelete();
		    	output = new String(data);
	    	}
		} catch (java.io.IOException e) {
			e.printStackTrace();
		}
      }
      
      return output;
    } else {
      new totalcross.ui.dialog.MessageBox(
              "Camera (Emulation)", "Say cheese!", new String[] {"Click"})
          .popup();
    }

    return defaultFileName;
  }

  @ReplacedByNativeOnDeploy
  private String nativeClick() {
    FileChooserBox fcb = new FileChooserBox(fcbTitle, FileChooserBox.defaultButtonCaptions, null);
    fcb.showPreview = fcb.newestFirst = true;
    fcb.popup();
    return fcb.getAnswer();
  }

  @Override
  protected void finalize() throws Throwable {
    nativeFinalize();
  }

  @ReplacedByNativeOnDeploy
  private void nativeFinalize() {}

  /**
   * Returns the resolutions that are supported by the device. Works for Windows CE, Blackberry and
   * Android.
   *
   * <p>When the information is not available, a default list is returned and the first element is
   * set to "Default values:".
   *
   * <p>If you take a photo and an image with a different resolution is returned, its because the
   * selected resolution is not supported by the device.
   *
   * <p>Values are always in LANDSCAPE.
   *
   * @since TotalCross 1.3
   */
  public static String[] getSupportedResolutions() {
    if (Settings.onJavaSE) {
      return new String[] {"320x240", "640x480", "1024x768", "2048x1536"};
    }

    String[] ret = null;
    if (Settings.platform.equals(Settings.ANDROID)) {
      String s = getNativeResolutions();
      if (s != null) {
        ret = sortResolutions(Convert.tokenizeString(s, ','));
      }
    } else if (Settings.isWindowsCE()) {
      List<String> v = new ArrayList<>(10);
      final String regPictureResolution =
          "Software\\Microsoft\\Pictures\\Camera\\OEM\\PictureResolution";
      String[] folders = Registry.list(Registry.HKEY_LOCAL_MACHINE, regPictureResolution);
      for (String folder : folders) {
        final String fullKey = regPictureResolution + "\\" + folder;
        try {
          final int w = Registry.getInt(Registry.HKEY_LOCAL_MACHINE, fullKey, "Width");
          final int h = Registry.getInt(Registry.HKEY_LOCAL_MACHINE, fullKey, "Height");
          v.add(w + "x" + h);
        } catch (Exception e) {
          // key not found
        }
      }
      if (v.size() > 0) {
        ret = sortResolutions(v.toArray(new String[v.size()]));
      }
    }
    if (ret == null) {
      ret = new String[] {"default resolution", "320x240", "640x480", "1024x768", "2048x1536"};
    }
    return ret;
  }

  private static String[] sortResolutions(String[] res) {
    for (int i = res.length; --i >= 0; ) {
      String[] sp = Convert.tokenizeString(res[i], 'x');
      res[i] = Convert.zeroPad(sp[0], 5) + "x" + Convert.zeroPad(sp[1], 5);
    }
    Convert.qsort(res, 0, res.length - 1);
    for (int i = res.length; --i >= 0; ) {
      String[] sp = Convert.tokenizeString(res[i], 'x');
      res[i] = Convert.zeroUnpad(sp[0]) + "x" + Convert.zeroUnpad(sp[1]);
    }
    return res;
  }

  @ReplacedByNativeOnDeploy
  private static String getNativeResolutions() {
    return null;
  }
}
