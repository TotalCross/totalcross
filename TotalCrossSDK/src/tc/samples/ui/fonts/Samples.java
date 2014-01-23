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

import totalcross.ui.*;
import totalcross.ui.font.*;
import totalcross.ui.gfx.*;

public class Samples extends ScrollContainer
{
   private Control []controls;

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
      add(edname = new Edit(""),AFTER,SAME);
      add(new Label("Adress: "), LEFT,AFTER+5);
      add(edadress = new Edit(""),AFTER,SAME);
      add(new Label("Quarter: "), LEFT,AFTER+5);
      add(edquarter = new Edit(""),AFTER,SAME);
      add(new Label("Gender: "),LEFT,AFTER+5);
      add(new Radio("Male",rgSexo),AFTER,SAME,PREFERRED,SAME);
      add(new Radio("Female",rgSexo),AFTER+3,SAME,PREFERRED,SAME);
      add(ch = new Check("Married?"),LEFT,AFTER+5); ch.setChecked(true); if (uiAndroid) ch.checkColor = Color.CYAN;
      add(new FontBox(),CENTER,AFTER+3);
      rgSexo.getRadio(0).leftJustify = true;

      edname.setText("João da Silva");
      edadress.setText("Boston 2021");
      edquarter.setText("Copacabana");
      rgSexo.setSelectedIndex(0);

      

      controls = getBagChildren();
      repositionAllowed = false; // only reposition the controls
   }

   public void setFonts(Font f)
   {
      setFont(f);
      for (int i = controls.length; --i >= 0;)
         controls[i].setFont(f);
      reposition();
   }
 }
