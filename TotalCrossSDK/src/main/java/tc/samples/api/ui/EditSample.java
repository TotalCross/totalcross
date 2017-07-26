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
         
         e = new Edit();
         e.caption = "Normal";
         e.captionIcon = getAwesomeImage('\uf12d', fmH, Color.BLACK);
         e.focusColor = focusColor;
         e.setBackColor(Color.darker(Color.YELLOW));
         sc.add(e,LEFT,AFTER,FILL,PREFERRED);
         final Edit e1 = e;
         e.captionPress = new CaptionPress()
         {
            public void onIconPress()
            {
               Vm.debug("on icon press");
               e1.clear();
            }
            
            public void onCaptionPress()
            {
               Vm.debug("on caption press");
               e1.clear();
            }
         };
         
         e = new Edit();
         e.caption = "Currency mode with Calculator (masked)";
         e.focusColor = focusColor;
         e.setBackColor(Color.darker(Color.MAGENTA));
         e.setMode(Edit.CURRENCY,true); 
         sc.add(e,LEFT,AFTER,FILL,PREFERRED);
         
         e = new Edit();
         e.caption = "Currency mode with Calculator (unmasked)";
         e.focusColor = focusColor;
         e.setBackColor(Color.darker(Color.BLUE));
         e.setKeyboard(Edit.KBD_CALCULATOR); 
         sc.add(e,LEFT,AFTER,FILL,PREFERRED);
         
         e = new Edit();
         e.caption = "Currency mode with NumericBox";
         e.focusColor = focusColor;
         e.setMode(Edit.CURRENCY); 
         e.setBackColor(Color.darker(Color.CYAN));
         sc.add(e,LEFT,AFTER,FILL,PREFERRED);
         e.setKeyboard(Edit.KBD_NUMERIC);

         e = new Edit("99/99/99");
         e.caption = "Date mode with Calendar";
         e.focusColor = focusColor;
         e.setMode(Edit.DATE,true); 
         sc.add(e,LEFT,AFTER,FILL,PREFERRED);

         e = new Edit("99"+Settings.timeSeparator+"99"+Settings.timeSeparator+"99");
         e.caption = "Hour with TimeBox (24-hour format)";
         TimeBox.hideIfInvalid = false;
         e.setValidChars("0123456789AMP");
         e.setBackColor(Color.darker(Color.RED));
         e.setMode(Edit.NORMAL,true);
         sc.add(e,LEFT,AFTER,FILL,PREFERRED);
         e.setKeyboard(Edit.KBD_TIME);

         e = new Edit("");
         e.caption = "Password (last character is shown)";
         e.setMode(Edit.PASSWORD); 
         e.setBackColor(Color.darker(Color.GREEN));
         sc.add(e,LEFT,AFTER,FILL,PREFERRED);

         e = new Edit("");
         e.caption = "Password (all characters are hidden)";
         e.setMode(Edit.PASSWORD_ALL); 
         e.setBackColor(Color.darker(Color.BLUE));
         sc.add(e,LEFT,AFTER,FILL,PREFERRED);

         e = new Edit("99.999-999");
         e.setValidChars(Edit.numbersSet);
         e.caption = "Brazilian's 8-digit postal code";
         e.setMode(Edit.NORMAL,true);
         sc.add(e,LEFT,AFTER,FILL,PREFERRED);
      }
      catch (Exception ee)
      {
         MessageBox.showException(ee,true);
      }
   }
}