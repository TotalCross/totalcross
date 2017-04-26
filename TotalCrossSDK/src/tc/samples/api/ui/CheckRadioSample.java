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

import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;

public class CheckRadioSample extends BaseContainer
{
   Control c0,c1,c2,c3,c4,c5;
   public void initUI()
   {
      try
      {
         super.initUI();
         setTitle("Check and Radio");
         ScrollContainer sc = new ScrollContainer(false, true);
         sc.setInsets(gap,gap,gap,gap);
         add(sc,LEFT,TOP,FILL,FILL);

         Check c;
         
         sc.add(c0 = c = new Check("Check box / cyan check"),LEFT,AFTER,PREFERRED+gap,PREFERRED+gap); 
         c.checkColor = Color.CYAN;
         c.setChecked(true);

         sc.add(c1 = c = new Check("Check box / yellow background"),LEFT,AFTER+gap,PREFERRED+gap,PREFERRED+gap);
         c.setBackColor(Color.YELLOW);
         c.textColor = Color.BLUE;
         c.checkColor = Color.YELLOW;

         sc.add(c2 = c = new Check("Check box / green foreground"),LEFT,AFTER+gap,PREFERRED+gap,PREFERRED+gap); 
         c.setForeColor(Color.darker(Color.GREEN));
         c.checkColor = Color.GREEN;

         RadioGroupController rg = new RadioGroupController();
         
         Radio r;
         sc.add(c3 = r = new Radio("Radio / cyan check",rg),LEFT,AFTER+gap*2,PREFERRED+gap,PREFERRED+gap); 
         r.checkColor = Color.CYAN;
         r.setChecked(true);

         sc.add(c4 = r = new Radio("Radio / yellow background",rg),LEFT,AFTER+gap,PREFERRED+gap,PREFERRED+gap);
         r.setBackColor(Color.YELLOW);
         r.textColor = Color.BLUE;
         r.checkColor = Color.YELLOW;

         sc.add(c5 = r = new Radio("Radio / green foreground",rg),LEFT,AFTER+gap,PREFERRED+gap,PREFERRED+gap); 
         r.setForeColor(Color.darker(Color.GREEN));
         r.checkColor = Color.GREEN;
         
         c0.addPressListener(new PressListener()
         {
            public void controlPressed(ControlEvent e)
            {
               boolean b = ((Check)c0).isChecked();
               c1.setEnabled(b);
               c2.setEnabled(b);
               c3.setEnabled(b);
               c4.setEnabled(b);
               c5.setEnabled(b);
            }
         });
      }
      catch (Exception ee)
      {
         MessageBox.showException(ee,true);
      }
   }
}