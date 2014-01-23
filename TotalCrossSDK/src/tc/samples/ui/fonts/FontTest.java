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

import totalcross.sys.Settings;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;

public class FontTest extends MainWindow
{
   Selector selector;
   Samples samples;
   Button btnExit;

   public FontTest()
   {
      super("Font test",TAB_ONLY_BORDER);
      setUIStyle(Settings.Android);
      //setFont(totalcross.ui.font.Font.getFont("Arial",false,Font.NORMAL_SIZE));
   }

   public void initUI()
   {
      add(selector = new Selector(), LEFT,TOP+2,FILL,PREFERRED);
      add(samples = new Samples(), LEFT,AFTER,PARENTSIZE+100,FILL);
      samples.setBackColor(Color.darker(getBackColor(),10)); // darker background
      btnExit = new Button("  X  ");
      btnExit.setBorder(Button.BORDER_NONE);
      add(btnExit,RIGHT,0);
   }

   public void onEvent(Event e)
   {
      if (e.type == ControlEvent.PRESSED && e.target == btnExit)
         exit(0);
      else
     if (e.type == ControlEvent.PRESSED && (e.target == Selector.cbNames || e.target == selector.ckBold || e.target == selector.slSize))
     {
        String fontname=(String)Selector.cbNames.getSelectedItem();
        totalcross.ui.font.Font f = selector.getSelectedFont();
        if(!f.name.equalsIgnoreCase(fontname))
           new MessageBox("TotalCross","Font not found. Please install \nthe file "+fontname+".tcz").popupNonBlocking();
        else
           samples.setFonts(f);
      }
   }
}

