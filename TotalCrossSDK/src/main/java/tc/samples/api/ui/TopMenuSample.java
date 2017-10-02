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
import totalcross.ui.ComboBox;
import totalcross.ui.Container;
import totalcross.ui.Control;
import totalcross.ui.Edit;
import totalcross.ui.Label;
import totalcross.ui.ScrollContainer;
import totalcross.ui.TopMenu;
import totalcross.ui.dialog.MessageBox;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.PressListener;
import totalcross.ui.gfx.Color;
import totalcross.ui.image.Image;

public class TopMenuSample extends BaseContainer {
  ScrollContainer sc;

  private class FilterContainer extends Container implements PressListener {
    @Override
    public void initUI() {
      super.initUI();
      Label l = new Label("FILTERS", CENTER, Color.WHITE, true);
      l.transparentBackground = true;
      add(l, LEFT, TOP, FILL, PREFERRED);
      add(new ComboBox(new String[] { "Name", "Name 1", "Name 2", "Name 3" }), LEFT, AFTER + fmH / 4, FILL, PREFERRED);
      add(new Edit(), LEFT, AFTER + fmH / 4);
      Button b;
      add(b = new Button("Search"), LEFT, AFTER + fmH / 4, FILL, PREFERRED);
      b.setBackColor(Color.CYAN);
      add(b = new Button("Close"), LEFT, AFTER + fmH, FILL, PREFERRED);
      b.setBackColor(Color.RED);
      b.addPressListener(this);
    }

    @Override
    public void controlPressed(ControlEvent e) {
      TopMenu top = (TopMenu) getParentWindow();
      top.unpop(new TopMenu.AnimationListener() {
        @Override
        public void onAnimationFinished() {
          new MessageBox("Message", "The search was done.").popup();
        }
      });
    }
  }

  @Override
  public void initUI() {
    try {
      super.initUI();

      Control[] items = { new TopMenu.Item("Videocalls", Resources.warning),
          new TopMenu.Item("Insert emoticon", Resources.exit), new ComboBox(new String[] { "Smile", "Sad", "Laugh" }),
          new TopMenu.Item("Add text", Resources.back), new TopMenu.Item("See contact", Resources.menu),
          new TopMenu.Item("Add slide", Resources.warning), new TopMenu.Item("Add subject", Resources.exit),
          new TopMenu.Item("Add persons", Resources.back), new TopMenu.Item("Programmed messages", Resources.menu),
          new TopMenu.Item("Add to the phone book", Resources.warning), };
      show(new TopMenu(items, CENTER), "CENTER");
      show(new TopMenu(items, BOTTOM), "BOTTOM");
      show(new TopMenu(items, TOP), "TOP");
      show(new TopMenu(items, LEFT), "LEFT");
      show(new TopMenu(items, RIGHT), "RIGHT");

      final TopMenu t = new TopMenu(new Control[] { new FilterContainer() }, RIGHT);
      t.totalTime = 500;
      t.autoClose = false;
      t.backImage = new Image("ui/images/back1.jpg");
      t.backImageAlpha = 96;
      t.popup();
      back();
    } catch (Exception e) {
      MessageBox.showException(e, true);
      back();
    }
  }

  private void show(final TopMenu t, String dir) {
    setInfo("Showing at " + dir + ". Click outside or drag");
    t.addPressListener(new PressListener() {
      @Override
      public void controlPressed(ControlEvent e) {
        setInfo("Selected index: " + t.getSelectedIndex());
      }
    });
    t.popup();
  }
}