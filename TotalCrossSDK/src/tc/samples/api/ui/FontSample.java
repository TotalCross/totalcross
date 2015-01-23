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
import totalcross.ui.event.*;
import totalcross.ui.font.*;
import totalcross.ui.gfx.*;

public class FontSample extends BaseContainer
{
   class FontBox extends Control
   {
      public void onPaint(Graphics g)
      {
         g.backColor = 0;
         g.fillRect(0,0,width,height);
      }
      
      public int getPreferredWidth()
      {
         return fmH;
      }
      
      public int getPreferredHeight()
      {
         return fmH;
      }
   }

   class Samples extends ScrollContainer
   {
      private Control []controls;

      public Samples()
      {
         super(true,true);
      }
      
      public void initUI()
      {
         setBackColor(Color.darker(getBackColor(),10)); // darker background
         Edit edname,edadress,edquarter;
         Check ch;
         RadioGroupController rgSexo = new RadioGroupController();

         add(new Label("Name: "), LEFT,TOP+5);
         add(edname = new Edit(""),AFTER,SAME,SCREENSIZE+200,PREFERRED);
         add(new Label("Adress: "), LEFT,AFTER+5);
         add(edadress = new Edit(""),AFTER,SAME,SCREENSIZE+200,PREFERRED);
         add(new Label("Quarter: "), LEFT,AFTER+5);
         add(edquarter = new Edit(""),AFTER,SAME,SCREENSIZE+200,PREFERRED);
         add(new Label("Gender: "),LEFT,AFTER+5);
         add(new Radio("Male",rgSexo),AFTER,SAME,PREFERRED,SAME);
         add(new Radio("Female",rgSexo),AFTER+3,SAME,PREFERRED,SAME);
         add(ch = new Check("Married?"),LEFT,AFTER+5); ch.setChecked(true); if (uiAndroid) ch.checkColor = Color.CYAN;
         add(new FontBox(),AFTER+fmH*3,CENTER_OF);
         rgSexo.getRadio(0).leftJustify = true;

         edname.setText("João da Silva");
         edadress.setText("Boston 2021");
         edquarter.setText("Copacabana");
         rgSexo.setSelectedIndex(0);

         controls = getBagChildren();
      }

      public void setFonts(Font f)
      {
         setFont(f);
         for (int i = controls.length; --i >= 0;)
            controls[i].setFont(f);
         repositionAllowed = false; // only reposition the controls, otherwise
         reposition();
         repositionAllowed = true; // only reposition the controls, otherwise
      }
   }

   class Selector extends Container
   {
      Check ckBold;
      Slider slSize;
      Label lSize;
      Font selFont;
      String [] fonts = {Font.DEFAULT,"monospace"};
      RadioGroupController rg = new RadioGroupController();

      public void initUI()
      {
         Label l;
         int max = Font.MAX_FONT_SIZE*(Settings.isWindowsDevice() ? 2 : 3);
         add(new Label("Typeface: "),LEFT,TOP);
         add(new Radio("Normal",rg),AFTER+fmH,SAME);
         add(new Radio("Monospace",rg), AFTER+fmH,SAME);
         rg.setSelectedIndex(0);
         add(l = new Label("Size:  "+Font.MIN_FONT_SIZE), LEFT, AFTER);
         add(new Label(""+max), RIGHT, SAME);
         add(slSize = new Slider(), AFTER+2, SAME, FIT-2, SAME+fmH/2,l);
         slSize.setLiveScrolling(!Settings.isWindowsDevice());
         slSize.setMinimum(Font.MIN_FONT_SIZE);
         slSize.setMaximum(max+1); // +1: visible items
         slSize.drawFilledArea = slSize.drawTicks = false;
         slSize.setValue(Font.NORMAL_SIZE);
         add(ckBold = new Check("Bold"), LEFT, AFTER);
         add(lSize = new Label(" 999 "),CENTER_OF,AFTER,slSize);
         selFont = font;
         updateSize();
      }

      public void updateSize()
      {
         int size = slSize.getValue();
         lSize.setText(String.valueOf(size));
      }

      public Font getSelectedFont()
      {
         int fontIdx = rg.getSelectedIndex();
         ckBold.setEnabled(fontIdx == 0);
         selFont = Font.getFont(fonts[fontIdx],fontIdx == 0 && ckBold.isChecked(), slSize.getValue());
         updateSize();
         return selFont;
      }

      public int getPreferredHeight()
      {
         return fmH * 5 + insets.top+insets.bottom;
      }
   }

   Selector selector;
   Samples samples;

   public void initUI()
   {
      super.initUI();
      setTitle("Font sizes");
      add(selector = new Selector(), LEFT,TOP+2,FILL,PREFERRED);
      add(samples = new Samples(), LEFT,AFTER,PARENTSIZE+100,FILL);
      samples.setBackColor(Color.darker(getBackColor(),10)); // darker background
   }

   public void onEvent(Event e)
   {
     if (e.type == ControlEvent.PRESSED && (e.target == selector.ckBold || e.target == selector.slSize || e.target instanceof Radio))
        samples.setFonts(selector.getSelectedFont());
   }
}
