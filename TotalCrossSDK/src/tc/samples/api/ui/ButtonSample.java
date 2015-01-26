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

import totalcross.res.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.font.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;

public class ButtonSample extends BaseContainer
{
   ScrollContainer sc;
   private int ccount=10;
   
   public void initUI()
   {
      try
      {
         super.initUI();
         sc = new ScrollContainer(false, true);
         sc.setInsets(gap,gap,gap,gap);
         add(sc,LEFT,TOP,FILL,FILL);
         Button c;
   
         sc.add(c=new Button("Simple button"), LEFT, AFTER, PREFERRED+gap, PREFERRED );
         c.addPressListener(new PressListener()
         {
            public void controlPressed(ControlEvent e)
            {
               if (--ccount == 0) MainWindow.exit(0);               
               else setInfo(ccount == 1 ? "1 more click will exit" : ccount+" more clicks to exit");
            }
         });
   
         sc.add(c = new Button("This is\na multi-line\nButton"), LEFT, AFTER + gap, PREFERRED+gap, PREFERRED+gap);
         c.setPressedColor(Color.ORANGE);
         
         Image img = Resources.warning.getSmoothScaledInstance(fmH, fmH);
         img.applyColor2(BKGCOLOR);
         sc.add(c = new Button("This is an image Button", img, LEFT, gap), LEFT, AFTER + gap, PREFERRED+gap, PREFERRED+gap);
         c.setBackColor(SELCOLOR);
   
         
         img = new Image("ui/images/find.png").hwScaledFixedAspectRatio(fmH*2,true);
         Font f = Font.getFont(true, Font.NORMAL_SIZE+2);
   
         sc.add(new Label("Text with\nimage: "),LEFT,AFTER+gap);
         Button btn;
         
         btn = new Button("Search", img, TOP, 8);
         btn.setFont(f);
         sc.add(btn,AFTER+5,SAME);
   
         btn = new Button("Search", img, BOTTOM, 8);
         btn.setFont(f);
         sc.add(btn,AFTER+5,SAME);
   
         btn = new Button("Search", img, LEFT, 8);
         btn.setFont(f);
         sc.add(btn,LEFT+5,AFTER+gap);
   
         btn = new Button("Search", img, RIGHT, 8);
         btn.setFont(f);
         sc.add(btn,AFTER+gap,SAME);
   
         btn = new Button(" Horizontal ");
         btn.setForeColor(0xEEEEEE);
         btn.setTextShadowColor(Color.BLACK);
         btn.setBorder(Button.BORDER_3D_HORIZONTAL_GRADIENT);
         sc.add(btn, RIGHT-5,AFTER+gap,PREFERRED,PREFERRED+10);
         
         btn = new Button(" Vertical ");
         btn.setForeColor(0xEEEEEE);
         btn.setTextShadowColor(Color.BLACK);
         btn.setBorder(Button.BORDER_3D_VERTICAL_GRADIENT);
         sc.add(btn, BEFORE-5,SAME,SAME,SAME);
   
         sc.add(new Label("Gradient: "),BEFORE-5,SAME,PREFERRED,SAME);
         
         addbtn(0xFF0000,RIGHT-5,AFTER+gap);
         addbtn(0x00FF00,BEFORE-4,SAME);
         addbtn(0xFFFF00,BEFORE-4,SAME);
         if (Settings.screenWidth > 240) addbtn(0x00FFFF,BEFORE-4,SAME);
         sc.add(new Label("Colorized: "),BEFORE-5,SAME,PREFERRED,SAME);

         sc.add(new Label("Image only:"), LEFT,AFTER+gap);
         addImageOnly();

         sc.add(new Ruler(),LEFT,AFTER+gap,FILL,PREFERRED);
         
         final Check cc = new Check("Enabled");
         sc.add(cc, LEFT,AFTER+gap);
         cc.setChecked(true);
         cc.addPressListener(new PressListener()
         {
            public void controlPressed(ControlEvent e)
            {
               for (Control c : sc.getBagChildren())
                  if (c instanceof Button)
                     c.setEnabled(cc.isChecked());
            }
         });
      }
      catch (Exception e)
      {
         MessageBox.showException(e,true);
         back();
      }
   }

   private void addImageOnly() throws Exception
   {
      Image[] images = {new Image("ui/images/cancel.png"), new Image("ui/images/ok.png")}; // images are 300x300
      int imgRes = 2048;
      int targetRes[] = {480,320,240};
      int k=0;
      final Button []btns = new Button[images.length * targetRes.length];
      
      for (int j = 0; j < targetRes.length; j++)
         for (int i = 0; i < 2; i++)
         {
            Image original = images[i];
            double factor = (double) targetRes[j] / (double) imgRes;
            Image img2 = original.smoothScaledBy(factor, factor);
            Button btn = btns[k++] = new Button(img2);
            if (j == 0) // just a demo for the user
               btn.pressedImage = img2.getTouchedUpInstance((byte)64,(byte)0);
            btn.setBorder(Button.BORDER_NONE);
            sc.add(btn, i == 0 ? LEFT : CENTER, i == 0 ? AFTER+gap : SAME, PARENTSIZE+30,PREFERRED);
         }
   }

   void addbtn(int color, int xpos, int ypos) throws Exception
   {
      Button btn = new Button("Rect", new Image("ui/images/buttontemplate.png"), CENTER, 6);
      btn.setBackColor(backColor);
      btn.borderColor3DG = color;
      btn.setTextShadowColor(Color.BLACK);
      btn.setForeColor(color);
      btn.setFont(font.asBold());
      btn.setBorder(Button.BORDER_GRAY_IMAGE);
      sc.add(btn,xpos,ypos);
   }
}