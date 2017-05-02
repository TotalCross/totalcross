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

import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;

public class ImageModifiersSample extends BaseContainer
{
   ScrollBar sbRotate;
   ScrollBar sbScale;
   ScrollBar sbContrast;
   ScrollBar sbBrightness;
   Label lbRotate;
   Label lbScale;
   Label lbContrast;
   Label lbBrightness;
   ImageControl imgFrm;
   Image img;
   Image imgRotated;
   Image imgContrasted;
   Rect rectImg;
   int rotateLevel; // -180 .. +180
   int scaleLevel; // 1 .. infinity
   byte contrastLevel; // -128 .. 127
   byte brightnessLevel; // -128 .. 127

   public void initUI()
   {
      super.initUI();
      setTitle("Image Modifiers");

      Label l;
      
      add(l = new Label("Rotate "), LEFT, TOP + gap);
      add(sbRotate = new ScrollBar(ScrollBar.HORIZONTAL));
      sbRotate.setValues(180, 1, 0, 361);
      sbRotate.setUnitIncrement(5);
      sbRotate.setBlockIncrement(30);
      sbRotate.setLiveScrolling(true);
      sbRotate.setRect(RIGHT, SAME + 2, PARENTSIZE+70, PREFERRED+fmH/4, lbRotate);
      add(lbRotate = new Label("0000"), AFTER, SAME,FIT,SAME,l);
      
      add(l = new Label("Scale "), LEFT, AFTER+gap);
      add(sbScale = new ScrollBar(ScrollBar.HORIZONTAL));
      sbScale.setValues(100, 1, 0, 400); // palm os has very limited memory
      sbScale.setUnitIncrement(5);
      sbScale.setBlockIncrement(20);
      sbScale.setLiveScrolling(true);
      sbScale.setRect(RIGHT, SAME + 2, PARENTSIZE+70, PREFERRED+fmH/4, lbScale);
      add(lbScale = new Label("0000"), AFTER, SAME,FIT,SAME,l);
      
      add(l = new Label("Contrast "), LEFT, AFTER+gap);
      add(sbContrast = new ScrollBar(ScrollBar.HORIZONTAL));
      sbContrast.setValues(128, 0, 0, 256);
      sbContrast.setUnitIncrement(8);
      sbContrast.setBlockIncrement(32);
      sbContrast.setLiveScrolling(true);
      sbContrast.setRect(RIGHT, SAME + 2, PARENTSIZE+70, PREFERRED+fmH/4, lbContrast);
      add(lbContrast = new Label("0000"), AFTER, SAME,FIT,SAME,l);
      
      add(l = new Label("Brightness "), LEFT, AFTER+gap);
      add(sbBrightness = new ScrollBar(ScrollBar.HORIZONTAL));
      sbBrightness.setValues(128, 0, 0, 256);
      sbBrightness.setUnitIncrement(8);
      sbBrightness.setBlockIncrement(32);
      sbBrightness.setLiveScrolling(true);
      sbBrightness.setRect(RIGHT, SAME + 2, PARENTSIZE+70, PREFERRED+fmH/4, lbBrightness);
      add(lbBrightness = new Label("0000"), AFTER, SAME,FIT,SAME,l);

      lbRotate.setText("0");
      lbContrast.setText("0");
      lbBrightness.setText("0");
      lbScale.setText("100");

      add(imgFrm = new ImageControl());
      imgFrm.setRect(LEFT + 1, AFTER + gap, FILL - 2, FILL - 2,lbBrightness);
      rectImg = imgFrm.getAbsoluteRect();
      imgFrm.allowBeyondLimits = false;
      imgFrm.centerImage = true;
      imgFrm.setEventsEnabled(true);
      
      loadImage("ui/images/cat.jpeg");
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
                              imgRotated = img.getRotatedScaledInstance(scaleLevel, rotateLevel, 0);
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
