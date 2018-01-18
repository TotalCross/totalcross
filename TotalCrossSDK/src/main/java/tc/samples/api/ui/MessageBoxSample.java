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
import totalcross.res.Resources;
import totalcross.ui.Button;
import totalcross.ui.ScrollContainer;
import totalcross.ui.UIColors;
import totalcross.ui.dialog.MessageBox;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.PressListener;
import totalcross.ui.gfx.Color;
import totalcross.ui.image.Image;

public class MessageBoxSample extends BaseContainer {
  @Override
  public void initUI() {
    try {
      super.initUI();
      ScrollContainer sc = new ScrollContainer(false, true);
      sc.setInsets(gap, gap, gap, gap);
      add(sc, LEFT, TOP, FILL, FILL);

      Button btn;

      sc.add(btn = new Button("Title only"), CENTER, TOP + fmH, PREFERRED + gap, PREFERRED + gap);
      btn.addPressListener(new PressListener() {
        @Override
        public void controlPressed(ControlEvent e) {
          MessageBox mb = new MessageBox("Message", "This is a MessageBox with title.", new String[] { "Close" });
          mb.popup();
        }
      });
      sc.add(btn = new Button("Yes/No Title only"), CENTER, AFTER + fmH, PREFERRED + gap, PREFERRED + gap);
      btn.addPressListener(new PressListener() {
        @Override
        public void controlPressed(ControlEvent e) {
          MessageBox mb = new MessageBox("Message", "Do you prefer this one?", new String[] { "Yes", "No" });
          mb.popup();
        }
      });
      sc.add(btn = new Button("No title"), CENTER, AFTER + fmH, PREFERRED + gap, PREFERRED + gap);
      btn.addPressListener(new PressListener() {
        @Override
        public void controlPressed(ControlEvent e) {
          MessageBox mb = new MessageBox("", "This is a MessageBox without title.", new String[] { "Close" });
          mb.popup();
        }
      });
      sc.add(btn = new Button("Title and Icon\nTop separator"), CENTER, AFTER + fmH, PREFERRED + gap, PREFERRED + gap);
      btn.addPressListener(new PressListener() {
        @Override
        public void controlPressed(ControlEvent e) {
          MessageBox mb = new MessageBox("Message", "This is a MessageBox with title and icon with top separator.",
              new String[] { "Close" });
          mb.headerColor = UIColors.messageboxBack;
          mb.footerColor = 0xAAAAAA;
          try {
            mb.setIcon(Resources.warning);
          } catch (Exception ee) {
            ee.printStackTrace();
          }
          mb.popup();
        }
      });
      sc.add(btn = new Button("Title and Icon\nTop/bottom separators"), CENTER, AFTER + fmH, PREFERRED + gap,
          PREFERRED + gap);
      btn.addPressListener(new PressListener() {
        @Override
        public void controlPressed(ControlEvent e) {
          MessageBox mb = new MessageBox("Message",
              "This is a MessageBox with title and icon with top and bottom separators.", new String[] { "Close" });
          mb.footerColor = mb.headerColor = UIColors.messageboxBack;
          try {
            // paint a copy of the image with the yellow color
            Image img = Resources.warning.getFrameInstance(0);
            img.applyColor2(Color.YELLOW);
            mb.setIcon(img);
          } catch (Exception ee) {
            ee.printStackTrace();
          }
          mb.popup();
        }
      });
    } catch (Exception ee) {
      MessageBox.showException(ee, true);
    }
  }
}