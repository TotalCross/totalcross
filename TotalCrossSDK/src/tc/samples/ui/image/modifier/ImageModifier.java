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

package tc.samples.ui.image.modifier;

import totalcross.io.File;
import totalcross.io.IOException;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.MessageBox;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.Event;
import totalcross.ui.gfx.Rect;
import totalcross.ui.image.Image;
import totalcross.ui.image.ImageException;

public class ImageModifier extends MainWindow
{
   String mainPath;
   MenuBar mbar;
   MenuItem miLiveChanges;
   ScrollBar sbRotate;
   ScrollBar sbScale;
   ScrollBar sbContrast;
   ScrollBar sbBrightness;
   Label lbRotate;
   Label lbScale;
   Label lbContrast;
   Label lbBrightness;
   ComboBox cbFiles;
   ImageControl imgFrm;
   Image img;
   Image imgRotated;
   Image imgContrasted;
   Rect rectImg;
   int rotateLevel; // -180 .. +180
   int scaleLevel; // 1 .. infinity
   byte contrastLevel; // -128 .. 127
   byte brightnessLevel; // -128 .. 127

   public ImageModifier()
   {
      super("Image Modifier", TAB_ONLY_BORDER);
   }

   public void initUI()
   {
      if (Settings.platform.equals(Settings.WIN32))
         mainPath = ".\\";
      else
      if (Settings.platform.equals(Settings.LINUX) || Settings.isIOS())
         mainPath = Settings.appPath + "/";
      else
         mainPath = "\\TotalCross\\";
      MenuItem[][] menus =
      {
         {
            new MenuItem("File"), // 0
            new MenuItem("Open..."), // 1
            new MenuItem("Connect..."), // 2
            new MenuItem("Save"), // 3
            new MenuItem("Save As..."), // 4
            new MenuItem(),
            new MenuItem("Exit") // 6
         },
         {
            new MenuItem("Options"), // 100
            miLiveChanges = new MenuItem("Live Changes",true), // 101
         },
         {
            new MenuItem("Help"), // 200
            new MenuItem("About"), // 201
            new MenuItem("Usage") // 202
         }
      };
      setMenuBar(mbar = new MenuBar(menus));

      cbFiles = new ComboBox();
      try
      {
         cbFiles.add("tc/samples/ui/image/modifier/cat.jpeg");

         listAllImages(mainPath, cbFiles);
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
      add(cbFiles);
      cbFiles.setRect(LEFT, AFTER + 2, FILL, PREFERRED);

      add(new Label("Rotate "), LEFT, AFTER + 1);
      add(lbRotate = new Label("0000"), AFTER, SAME);
      add(new Label("Scale "), LEFT, AFTER);
      add(lbScale = new Label("0000"), AFTER, SAME);
      add(new Label("Contrast "), LEFT, AFTER);
      add(lbContrast = new Label("0000"), AFTER, SAME);
      add(new Label("Brightness "), LEFT, AFTER);
      add(lbBrightness = new Label("0000"), AFTER, SAME);

      int x = Settings.screenWidth / 2;
      int dy = lbBrightness.getPreferredHeight()-4;
      add(sbRotate = new ScrollBar(ScrollBar.HORIZONTAL));
      sbRotate.setValues(180, 1, 0, 361);
      sbRotate.setUnitIncrement(5);
      sbRotate.setBlockIncrement(30);
      sbRotate.setRect(x, SAME + 2, FILL, dy, lbRotate);

      add(sbScale = new ScrollBar(ScrollBar.HORIZONTAL));
      sbScale.setValues(100, 1, 0, 400); // palm os has very limited memory
      sbScale.setUnitIncrement(5);
      sbScale.setBlockIncrement(20);
      sbScale.setRect(x, SAME + 2, FILL, dy, lbScale);

      add(sbContrast = new ScrollBar(ScrollBar.HORIZONTAL));
      sbContrast.setValues(128, 0, 0, 256);
      sbContrast.setUnitIncrement(8);
      sbContrast.setBlockIncrement(32);
      sbContrast.setRect(x, SAME + 2, FILL, dy, lbContrast);

      add(sbBrightness = new ScrollBar(ScrollBar.HORIZONTAL));
      sbBrightness.setValues(128, 0, 0, 256);
      sbBrightness.setUnitIncrement(8);
      sbBrightness.setBlockIncrement(32);
      sbBrightness.setRect(x, SAME + 2, FILL, dy, lbBrightness);

      sbRotate.setLiveScrolling(miLiveChanges.isChecked);
      sbScale.setLiveScrolling(miLiveChanges.isChecked);
      sbContrast.setLiveScrolling(miLiveChanges.isChecked);
      sbBrightness.setLiveScrolling(miLiveChanges.isChecked);

      lbRotate.setText("0");
      lbContrast.setText("0");
      lbBrightness.setText("0");
      lbScale.setText("100");

      add(imgFrm = new ImageControl());
      imgFrm.setRect(LEFT + 1, AFTER + 4, FILL - 2, FILL - 2,lbBrightness);
      rectImg = imgFrm.getAbsoluteRect();
      imgFrm.allowBeyondLimits = false;
      imgFrm.centerImage = true;
      imgFrm.setEventsEnabled(true);
   }

   private void listAllImages(String path, ComboBox lb) throws totalcross.io.IOException
   {
      File file = new File(path);
      String names[] = file.listFiles(), name;
      if (names != null)
      {
         int n = names.length;
         for (int i = 0; i < n; ++i)
         {
            if ((name=names[i]) != null)
            {
               if (name.endsWith("/"))
                  listAllImages(path + name, lb);
               else
               if (Image.isSupported(names[i]))
                  lb.add(path + names[i]);
            }
         }
      }
   }

   private void loadImage(String dsc)
   {
      img = null;
      imgContrasted = null;
      imgRotated = null;

      try
      {
         img = new Image(dsc);

         int scaleW = (100 * rectImg.width) / img.getWidth();
         int scaleH = (100 * rectImg.height) / img.getHeight();
         if ((scaleH < 100) || (scaleW < 100))
            sbScale.setValue((scaleH < scaleW) ? scaleH : scaleW);
         else
            sbScale.setValue(100);
         onEvent(getPressedEvent(sbScale));
      }
      catch (ImageException ie)
      {
         tellUser("Error", "Cannot decode " + dsc);
      }
      catch (totalcross.io.IOException ioe)
      {
         tellUser("Error", "Error ocurred while processing the file " + dsc + "/n" + ioe.getMessage());
      }
   }

   private void setImage(Image img)
   {
      imgFrm.setImage(img);
   }

   public void onEvent(Event event)
   {
      switch (event.type)
      {
         case ControlEvent.PRESSED:
            if (event.target instanceof ScrollBar)
            {
               try
               {
                  if ((event.target == sbContrast) || (event.target == sbBrightness))
                  {
                     byte newBrightnessLevel = (byte) (sbBrightness.getValue() - 128);
                     byte newContrastLevel = (byte) (sbContrast.getValue() - 128);
                     if (newBrightnessLevel == brightnessLevel && newContrastLevel == contrastLevel)
                        return;
                     brightnessLevel = newBrightnessLevel;
                     contrastLevel = newContrastLevel;
                     lbBrightness.setText(Convert.toString(brightnessLevel));
                     lbContrast.setText(Convert.toString(contrastLevel));
                     if (img != null)
                     {
                        try
                        {
                           if (imgRotated == null)
                           {
                              imgContrasted = null;
                              Vm.gc();
                              imgRotated = img.getRotatedScaledInstance(scaleLevel, rotateLevel, getBackColor());
                           }
                           setImage(imgRotated.getTouchedUpInstance(brightnessLevel,contrastLevel));
                        }
                        catch (ImageException e)
                        {
                           // TODO Auto-generated catch block
                           totalcross.ui.dialog.MessageBox.showException(e, true);
                        }
                     }
                  }
                  else if ((event.target == sbRotate) || (event.target == sbScale))
                  {
                     int newRotateLevel = sbRotate.getValue() - 180;
                     int newScaleLevel = sbScale.getValue();
                     if (newRotateLevel == rotateLevel && newScaleLevel == scaleLevel)
                        return;
                     rotateLevel = newRotateLevel;
                     scaleLevel = newScaleLevel;
                     lbRotate.setText(Convert.toString(rotateLevel));
                     lbScale.setText(Convert.toString(scaleLevel));
                     if (img != null)
                     {
                        if (imgContrasted == null)
                        {
                           imgRotated = null;
                           Vm.gc();
                           imgContrasted = img.getTouchedUpInstance(brightnessLevel,contrastLevel);
                        }
                        setImage(imgContrasted.getRotatedScaledInstance(scaleLevel, rotateLevel, getBackColor()));
                     }
                  }
               }
               catch (ImageException e)
               {
                  Vm.alert(e.getMessage());
               }
            }
            else
            if (event.target == cbFiles && cbFiles.getSelectedIndex() >= 0)
            {
               loadImage((String) cbFiles.getSelectedItem());
            }
            else
            if (event.target == mbar)
            {
               switch (mbar.getSelectedIndex())
               {
                  case -1:
                     break;
                  case 6:
                     exit(0);
                     break;
                  case 101: // live changes
                     sbRotate.setLiveScrolling(miLiveChanges.isChecked);
                     sbScale.setLiveScrolling(miLiveChanges.isChecked);
                     sbContrast.setLiveScrolling(miLiveChanges.isChecked);
                     sbBrightness.setLiveScrolling(miLiveChanges.isChecked);
                     break;
                  case 201: // about
                     tellUser("About", "ImageModifier\nWritten by: Pierre G. Richard\nand Guilherme Campos Hazan");
                     break;
               }
               onEvent(getPressedEvent(sbScale));
            }
            repaint();
            break;
      }
   }

   private void tellUser(String topic, String what)
   {
      MessageBox mb = new MessageBox(topic, what);
      mb.setTextAlignment(LEFT);
      mb.setBackForeColors(0x806600, 0xE6FF99);
      mb.popupNonBlocking();
   }
}
