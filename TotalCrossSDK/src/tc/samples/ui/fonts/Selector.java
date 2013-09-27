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



package tc.samples.ui.fonts;

import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.font.*;

public class Selector extends Container
{
   static ComboBox cbNames;
   Check ckBold;
   Slider slSize;
   Label lSize;
   Font selFont;

   public void initUI()
   {
      Label l1,l2;
      add(l1 = new Label("Name: "), LEFT, TOP + 3);
      add(cbNames = new ComboBox(new String[] { Font.DEFAULT, "Arial"}), AFTER, SAME);
      cbNames.setSelectedIndex(0);
      add(l2 = new Label("Size:  "+Font.MIN_FONT_SIZE), LEFT, AFTER + 3, l1);
      int max = Settings.isOpenGL || Settings.onJavaSE ? Font.MAX_FONT_SIZE*2 : Font.MAX_FONT_SIZE;
      add(l1 = new Label(""+max), RIGHT, SAME);
      add(slSize = new Slider(), AFTER+2, SAME, FIT-2, SAME,l2);
      slSize.setLiveScrolling(true);
      slSize.setMinimum(Font.MIN_FONT_SIZE);
      slSize.setMaximum(max+1); // +1: visible items
      slSize.drawFilledArea = false;
      slSize.drawTicks = true;
      slSize.setValue(Font.NORMAL_SIZE);
      add(lSize = new Label(" 999 (h=999) "),CENTER_OF,AFTER+3);
      add(ckBold = new Check("Bold"), LEFT, AFTER + 3, l2);
      selFont = font;
      updateSize();
   }

   public void updateSize()
   {
      int size = slSize.getValue();
      lSize.setText(size+" (h="+selFont.fm.height+")");
   }

   public Font getSelectedFont()
   {
      selFont = Font.getFont((String)cbNames.getSelectedItem(), ckBold.isChecked(), slSize.getValue());
      updateSize();
      return selFont;
   }

   public int getPreferredHeight()
   {
      return new Label().getPreferredHeight() * 4 + insets.top+insets.bottom;
   }
}
