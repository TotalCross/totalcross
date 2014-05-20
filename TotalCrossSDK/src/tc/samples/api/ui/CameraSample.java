/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2014 SuperWaba Ltda.                                      *
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

package tc.samples.api.ui;

import tc.samples.api.*;

import totalcross.io.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;
import totalcross.ui.media.*;

public class CameraSample extends BaseContainer
{
   Button btnFilm, btnPhoto, btnRotate;
   Label l;
   ComboBox cbRes;
   ImageControl ic;
   Camera camera;
   
   public void initUI()
   {
      super.initUI();
      setTitle("Camera");
      Settings.showMemoryMessagesAtExit = false;
      
      add(l = new Label(""), LEFT, BOTTOM);
      add(btnFilm = new Button("Film"), LEFT+gap, BEFORE,PREFERRED+fmH,PREFERRED+fmH);
      add(btnPhoto = new Button("Photo"), AFTER + 5, SAME, SAME, SAME);
      add(btnRotate = new Button("Rotate"), AFTER + 5, SAME, SAME, SAME);
      add(cbRes = new ComboBox(Camera.getSupportedResolutions()),AFTER+gap,SAME,FILL-gap,SAME,btnRotate); // guich@tc126_24
      cbRes.setSelectedIndex(0);
      btnRotate.setEnabled(false);
      add(ic = new ImageControl(), LEFT+gap, TOP+gap, FILL-gap, FIT-gap);
      ic.setEventsEnabled(true);
      camera = new Camera();
      if (Settings.isIOS() || Settings.platform.equals(Settings.WINDOWSPHONE))
         btnFilm.setVisible(false);
   }

   public void onEvent(Event event)
   {
      try
      {
         if (event.type == ControlEvent.PRESSED)
         {
            if (event.target == btnPhoto)
            {
               btnPhoto.setEnabled(false);
               camera.captureMode = Camera.CAMERACAPTURE_MODE_STILL;
               camera.stillQuality = Camera.CAMERACAPTURE_STILLQUALITY_HIGH;
               // get resolution
               String res = (String)cbRes.getSelectedItem();
               if (res == null) res = "640x480";
               String[] p = Convert.tokenizeString(res,'x');
               try
               {
                  camera.resolutionWidth  = Convert.toInt(p[0]);
                  camera.resolutionHeight = Convert.toInt(p[1]);
               }
               catch (InvalidNumberException ine)
               {
                  // keep original resolution
               }
               //
               l.setText("Starting camera...");
               l.repaintNow();
               camera.defaultFileName = "picture.jpg";
               String ret = camera.click();
               if (ret != null)
               {
                  File f = new File(ret, File.READ_ONLY);
                  int s = f.getSize();
                  try
                  {
                     Image img = new Image(f);
                     //img.transparentColor = Image.NO_TRANSPARENT_COLOR; // doesn't make sense on photos to have a transparent background
                     ic.setImage(img);
                     btnRotate.setEnabled(true);
                     if (Settings.platform.equals(Settings.ANDROID))
                        ret = copyToSD(f);
                     l.setMarqueeText(img.getWidth() + "x" + img.getHeight() + " (" + s +" bytes) " + ret, 100, 3, -5);
                  }
                  catch (OutOfMemoryError oome) // guich@tc126_24
                  {
                     btnRotate.setEnabled(false);
                     if (ret != null)
                        l.setText(ret);
                     ic.setImage(null);
                     new MessageBox("Error","Out of memory trying to load the image. The picture was taken but it cannot be shown here. If you selected a resolution that is not too big (under 1MP), then this error means that this resolution is not really supported on the camera, alghough it is listed, and a possible big default resolution was selected instead. Select another resolution.").popup();
                  }
                  f.close();
                  repaint();
               }
               else
               {
                  btnRotate.setEnabled(false);
                  l.setText("User canceled");
               }
               btnPhoto.setEnabled(true);
            }
            else if (event.target == btnFilm)
            {
               l.setText("Starting camcorder...");
               l.repaintNow();
               // guich@tc126_24: set the parameters again
               camera.stillQuality = Camera.CAMERACAPTURE_STILLQUALITY_HIGH;
               camera.resolutionWidth  = 240;
               camera.resolutionHeight = 320;
               camera.captureMode = Camera.CAMERACAPTURE_MODE_VIDEOONLY;
               camera.defaultFileName = "movie.mpg";
               String ret = camera.click();
               if (ret == null)
                  l.setText("User canceled");
               else
               {
                  File f = new File(ret,File.READ_ONLY);
                  int len = f.getSize();
                  if (Settings.platform.equals(Settings.ANDROID))
                     ret = copyToSD(f);
                  f.close();
                  l.setMarqueeText(ret + " - "+len+" bytes - videos cannot be displayed in TotalCross yet. Resolution set to 240x320", 100, 3, -5);
               }
               ic.setImage(null);
               btnRotate.setEnabled(false);
            }
            else if (event.target == btnRotate)
            {
               ic.setImage(ic.getImage().getRotatedScaledInstance(100, 90, Color.BLACK));
               repaint();
            }
         }
      }
      catch (Exception e)
      {
         MessageBox.showException(e, false);
      }
   }

   private String copyToSD(File f)
   {
      String ret = f.getPath();
      try
      {
         String dir = ret.endsWith(".3gp") ? "/sdcard/video/" : "/sdcard/photo/";
         try {new File(dir).createDir();} catch (Exception e) {}
         String ret2 = dir+(ret.substring(ret.lastIndexOf('/')+1));
         File f2 = new File(ret2,File.CREATE_EMPTY);
         f.copyTo(f2);
         f2.close();
         ret += " (and also at "+ret2+")";
      } catch (Exception e) {e.printStackTrace();}
      return ret;
   }
}
