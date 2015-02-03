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



package tc.samples.api.phone;

import tc.samples.api.*;

import totalcross.io.*;
import totalcross.phone.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.font.*;
import totalcross.ui.gfx.*;

public class PhoneDialerSample extends BaseContainer
{
   PushButtonGroup pbg;
   Edit ed;
   Button dial;

   public void initUI()
   {
      super.initUI();
      Font big = Font.getFont(true, Font.NORMAL_SIZE+2);
      ed = new Edit("");
      ed.setFont(Font.getFont(true, Font.NORMAL_SIZE*2));
      ed.setEnabled(false);
      add(ed,LEFT,TOP);
      
      pbg = new PushButtonGroup(new String[]{"1","2","3","4","5","6","7","8","9","P","0"," << "}, 5, 4);
      pbg.setFont(big);
      add(pbg,CENTER,CENTER,PARENTSIZEMIN+50,PARENTSIZEMIN+50);
      pbg.setFocusLess(true);

      dial = new Button("Dial");
      dial.setFont(big);
      add(dial, SAME, AFTER+5, SAME, PREFERRED);
      dial.setBackColor(Color.GREEN);
      dial.setEnabled(false);
   }

   public void onEvent(Event e)
   {
      switch (e.type)
      {
         case ControlEvent.PRESSED:
            if (e.target == pbg)
            {
               switch (pbg.getSelectedIndex())
               {
                  case 11: if (ed.getLength() > 0) ed.setText(ed.getText().substring(0,ed.getText().length()-1)); break;
                  case -1: break;
                  default: String s = pbg.getSelectedItem(); ed.setText(ed.getText()+s); 
                  break;
               }
               dial.setEnabled(ed.getLength() > 0);
            }
            else
            if (e.target == dial)
            {
               try
               {
                  Dial.number(ed.getText());
               }
               catch (IOException ex)
               {
                  MessageBox.showException(ex,true);
               }
            }
      }
   }
}
