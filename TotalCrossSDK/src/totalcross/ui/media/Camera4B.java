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



package totalcross.ui.media;

import java.io.IOException;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import totalcross.Launcher4B;
import totalcross.io.File;
import totalcross.sys.Convert;
import totalcross.sys.Time;
import totalcross.ui.dialog.MessageBox;

public class Camera4B
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

   Player player;

   public Camera4B()
   {
      try
      {
         player = Manager.createPlayer("capture://video");
         player.realize();
         player.start();
      }
      catch (IOException e)
      {
         MessageBox.showException(e, true);
      }
      catch (MediaException e)
      {
         MessageBox.showException(e, true);
      }
   }

   /**
    * Takes a photo based on the members set.
    * 
    * @return The String with the file name where the image is located or null if the user canceled.
    * @throws totalcross.io.IOException
    *            if any other error occurs.
    */
   public String click() throws totalcross.io.IOException
   {
      String dir = initialDir != null ? initialDir : "device/";
      String fileName = defaultFileName != null ? defaultFileName : ("picture_" + new Time().getTimeLong() + ".jpg"); 
         
      byte[] imgBytes = Launcher4B.instance.initCamera(player);
      if (imgBytes != null)
      {
         File f = new File(Convert.appendPath(dir, fileName), File.CREATE_EMPTY); //flsobral@tc123_18: fixed Camera for BlackBerry not using the fields initialDir and defaultFileName - it would always save as device/foto.jpg
         f.writeBytes(imgBytes);
         String path = f.getPath();
         f.close();
         return path;
      }
      
      return null;
   }

   public static String[] getSupportedResolutions()
   {
      return Launcher4B.instance.getSupportedResolutions();
   }
}
