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
import totalcross.ui.gfx.*;

public class EditSample extends BaseContainer
{
   public void initUI()
   {
      try
      {
         Settings.is24Hour = true;
         
         super.initUI();
         ScrollContainer sc = new ScrollContainer(false, true);
         sc.setInsets(gap,gap,gap,gap);
         add(sc,LEFT,TOP,FILL,FILL);
         Edit e;
         
         int focusColor = Color.WHITE;
         
         sc.add(new Label("Normal"),LEFT,AFTER);
         e = new Edit();
         e.focusColor = focusColor;
         e.setBackColor(Color.brighter(Color.YELLOW));
         sc.add(e,LEFT,AFTER);
         
         sc.add(new Label("Currency mode with Calculator (masked)"),LEFT,AFTER+gap);
         e = new Edit();
         e.focusColor = focusColor;
         e.setBackColor(Color.brighter(Color.MAGENTA));
         e.setMode(Edit.CURRENCY,true); 
         sc.add(e,LEFT,AFTER);
         
         sc.add(new Label("Currency mode with Calculator (unmasked)"),LEFT,AFTER+gap);
         e = new Edit();
         e.focusColor = focusColor;
         e.setBackColor(Color.brighter(Color.BLUE));
         e.setKeyboard(Edit.KBD_CALCULATOR); 
         sc.add(e,LEFT,AFTER);
         
         sc.add(new Label("Currency mode with NumericBox"),LEFT,AFTER+gap);
         e = new Edit();
         e.focusColor = focusColor;
         e.setMode(Edit.CURRENCY); 
         e.setBackColor(Color.brighter(Color.CYAN));
         sc.add(e,LEFT,AFTER);
         e.setKeyboard(Edit.KBD_NUMERIC);

         sc.add(new Label("Date mode with Calendar"),LEFT,AFTER+gap);
         e = new Edit("99/99/99");
         e.focusColor = focusColor;
         e.setMode(Edit.DATE,true); 
         sc.add(e,LEFT,AFTER);

         sc.add(new Label("Hour with TimeBox (24-hour format)"),LEFT,AFTER+gap);
         TimeBox.hideIfInvalid = false;
         e = new Edit("99"+Settings.timeSeparator+"99"+Settings.timeSeparator+"99");
         e.setValidChars("0123456789AMP");
         e.setBackColor(Color.brighter(Color.RED));
         e.setMode(Edit.NORMAL,true);
         sc.add(e,LEFT,AFTER);
         e.setKeyboard(Edit.KBD_TIME);

         sc.add(new Label("Password (last character is shown)"),LEFT,AFTER+gap);
         e = new Edit("");
         e.setMode(Edit.PASSWORD); 
         e.setBackColor(Color.brighter(Color.GREEN));
         sc.add(e,LEFT,AFTER);

         sc.add(new Label("Password (all characters are hidden)"),LEFT,AFTER+gap);
         e = new Edit("");
         e.setMode(Edit.PASSWORD_ALL); 
         e.setBackColor(Color.brighter(Color.BLUE));
         sc.add(e,LEFT,AFTER);

         sc.add(new Label("Brazilian's 8-digit postal code"),LEFT,AFTER+gap);
         e = new Edit("99.999-999");
         e.setMode(Edit.NORMAL,true);
         sc.add(e,LEFT,AFTER);
      }
      catch (Exception ee)
      {
         MessageBox.showException(ee,true);
      }
   }
}