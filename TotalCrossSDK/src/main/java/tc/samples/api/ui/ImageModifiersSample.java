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

import tc.samples.api.BaseContainer;
import totalcross.sys.Convert;
import totalcross.sys.Vm;
import totalcross.ui.ImageControl;
import totalcross.ui.Label;
import totalcross.ui.ScrollBar;
import totalcross.ui.Slider;
import totalcross.ui.dialog.MessageBox;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.Event;
import totalcross.ui.gfx.Rect;
import totalcross.ui.image.Image;
import totalcross.ui.image.ImageException;

public class ImageModifiersSample extends BaseContainer {
  Slider slRotate;
  Slider slScale;
  Slider slContrast;
  Slider slBrightness;
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

  @Override
  public void initUI() {
    super.initUI();
    setTitle("Image Modifiers");

    Label l;

    add(l = new Label("Rotate "), LEFT, TOP + gap);
    add(slRotate = new Slider(ScrollBar.HORIZONTAL));
    slRotate.setValues(180, 1, 0, 361);
    slRotate.setUnitIncrement(5);
    slRotate.setBlockIncrement(30);
    slRotate.setLiveScrolling(true);
    slRotate.setRect(RIGHT, SAME + 2, PARENTSIZE + 60, PREFERRED + fmH / 4, lbRotate);
    add(lbRotate = new Label("0000"), AFTER, SAME, FIT, SAME, l);

    add(l = new Label("Scale "), LEFT, AFTER + gap);
    add(slScale = new Slider(ScrollBar.HORIZONTAL));
    slScale.setValues(100, 1, 0, 400); // palm os has very limited memory
    slScale.setUnitIncrement(5);
    slScale.setBlockIncrement(20);
    slScale.setLiveScrolling(true);
    slScale.setRect(RIGHT, SAME + 2, PARENTSIZE + 60, PREFERRED + fmH / 4, lbScale);
    add(lbScale = new Label("0000"), AFTER, SAME, FIT, SAME, l);

    add(l = new Label("Contrast "), LEFT, AFTER + gap);
    add(slContrast = new Slider(ScrollBar.HORIZONTAL));
    slContrast.setValues(128, 0, 0, 256);
    slContrast.setUnitIncrement(8);
    slContrast.setBlockIncrement(32);
    slContrast.setLiveScrolling(true);
    slContrast.setRect(RIGHT, SAME + 2, PARENTSIZE + 60, PREFERRED + fmH / 4, lbContrast);
    add(lbContrast = new Label("0000"), AFTER, SAME, FIT, SAME, l);

    add(l = new Label("Brightness "), LEFT, AFTER + gap);
    add(slBrightness = new Slider(ScrollBar.HORIZONTAL));
    slBrightness.setValues(128, 0, 0, 256);
    slBrightness.setUnitIncrement(8);
    slBrightness.setBlockIncrement(32);
    slBrightness.setLiveScrolling(true);
    slBrightness.setRect(RIGHT, SAME + 2, PARENTSIZE + 60, PREFERRED + fmH / 4, lbBrightness);
    add(lbBrightness = new Label("0000"), AFTER, SAME, FIT, SAME, l);

    lbRotate.setText("0");
    lbContrast.setText("0");
    lbBrightness.setText("0");
    lbScale.setText("100");

    slRotate.sliderColor = slScale.sliderColor = slContrast.sliderColor = slBrightness.sliderColor = 0xFF5252;

    add(imgFrm = new ImageControl());
    imgFrm.setRect(LEFT + 1, AFTER + gap, FILL - 2, FILL - 2, lbBrightness);
    rectImg = imgFrm.getAbsoluteRect();
    imgFrm.allowBeyondLimits = false;
    imgFrm.centerImage = true;
    imgFrm.setEventsEnabled(true);

    loadImage("ui/images/cat.jpeg");
  }

  private void loadImage(String dsc) {
    img = null;
    imgContrasted = null;
    imgRotated = null;

    try {
      img = new Image(dsc);

      int scaleW = (100 * rectImg.width) / img.getWidth();
      int scaleH = (100 * rectImg.height) / img.getHeight();
      if ((scaleH < 100) || (scaleW < 100)) {
        slScale.setValue((scaleH < scaleW) ? scaleH : scaleW);
      } else {
        slScale.setValue(100);
      }
      onEvent(getPressedEvent(slScale));
    } catch (ImageException ie) {
      tellUser("Error", "Cannot decode " + dsc);
    } catch (totalcross.io.IOException ioe) {
      tellUser("Error", "Error ocurred while processing the file " + dsc + "/n" + ioe.getMessage());
    }
  }

  private void setImage(Image img) {
    imgFrm.setImage(img);
  }

  @Override
  public void onEvent(Event event) {
    switch (event.type) {
    case ControlEvent.PRESSED:
      if (event.target instanceof ScrollBar) {
        try {
          if ((event.target == slContrast) || (event.target == slBrightness)) {
            byte newBrightnessLevel = (byte) (slBrightness.getValue() - 128);
            byte newContrastLevel = (byte) (slContrast.getValue() - 128);
            if (newBrightnessLevel == brightnessLevel && newContrastLevel == contrastLevel) {
              return;
            }
            brightnessLevel = newBrightnessLevel;
            contrastLevel = newContrastLevel;
            lbBrightness.setText(Convert.toString(brightnessLevel));
            lbContrast.setText(Convert.toString(contrastLevel));
            if (img != null) {
              try {
                if (imgRotated == null) {
                  imgContrasted = null;
                  Vm.gc();
                  imgRotated = img.getRotatedScaledInstance(scaleLevel, rotateLevel, 0);
                }
                setImage(imgRotated.getTouchedUpInstance(brightnessLevel, contrastLevel));
              } catch (ImageException e) {
                // TODO Auto-generated catch block
                totalcross.ui.dialog.MessageBox.showException(e, true);
              }
            }
          } else if ((event.target == slRotate) || (event.target == slScale)) {
            int newRotateLevel = slRotate.getValue() - 180;
            int newScaleLevel = slScale.getValue();
            if (newRotateLevel == rotateLevel && newScaleLevel == scaleLevel) {
              return;
            }
            rotateLevel = newRotateLevel;
            scaleLevel = newScaleLevel;
            lbRotate.setText(Convert.toString(rotateLevel));
            lbScale.setText(Convert.toString(scaleLevel));
            if (img != null) {
              if (imgContrasted == null) {
                imgRotated = null;
                Vm.gc();
                imgContrasted = img.getTouchedUpInstance(brightnessLevel, contrastLevel);
              }
              setImage(imgContrasted.getRotatedScaledInstance(scaleLevel, rotateLevel, getBackColor()));
            }
          }
        } catch (ImageException e) {
          Vm.alert(e.getMessage());
        }
      }
      repaint();
      break;
    }
  }

  private void tellUser(String topic, String what) {
    MessageBox mb = new MessageBox(topic, what);
    mb.setTextAlignment(LEFT);
    mb.setBackForeColors(0x806600, 0xE6FF99);
    mb.popupNonBlocking();
  }
}
