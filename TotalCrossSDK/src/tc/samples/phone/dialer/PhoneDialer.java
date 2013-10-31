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



package tc.samples.phone.dialer;

import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.event.*;
import totalcross.ui.font.*;
import totalcross.ui.gfx.*;
import totalcross.io.*;
import totalcross.phone.*;

public class PhoneDialer extends MainWindow implements Dial.Listener
{
   PushButtonGroup pbg;
   Edit ed;
   ListBox lb;
   KeyEvent backspace,ke;
   Button dial,hang;

   public PhoneDialer()
   {
      super("Phone dialer", TAB_ONLY_BORDER);
      setUIStyle(Settings.Vista);
      backspace = new KeyEvent(); backspace.type = KeyEvent.SPECIAL_KEY_PRESS; backspace.target = ed; backspace.key = SpecialKeys.BACKSPACE;
      ke = new KeyEvent(); ke.type = KeyEvent.KEY_PRESS; ke.target = ed;
      Dial.listener = this;
   }

   public void initUI()
   {
      Font big = Font.getFont(true, Font.NORMAL_SIZE+2);
      ed = new Edit("");
      ed.setFont(big);
      ed.setEnabled(false);
      add(ed,LEFT,TOP);

      pbg = new PushButtonGroup(new String[]{"1","2","3","4","5","6","7","8","9","P","0"," << "}, 5, 4);
      pbg.setFont(big);
      add(pbg,CENTER,AFTER+5,PREFERRED,PREFERRED+16);
      pbg.setFocusLess(true);

      dial = new Button("Dial");
      dial.setFont(big);
      add(dial, SAME, AFTER+5, SAME-18, PREFERRED);
      dial.setBackColor(Color.GREEN);
      dial.setEnabled(false);

      hang = new Button("Hang");
      hang.setFont(big);
      add(hang, SAME, AFTER+5, SAME, PREFERRED);
      hang.setBackColor(Color.RED);
      hang.setEnabled(false);

      lb = new ListBox();
      lb.enableHorizontalScroll();
      add(lb,LEFT,AFTER+5,FILL,FILL);
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
                  case 11: ed.onEvent(backspace); break;
                  case -1: break;
                  default: ke.key = pbg.getSelectedItem().charAt(0); ed.onEvent(ke); break;
               }
               dial.setEnabled(ed.getLength() > 0);
            }
            else
            if (e.target == dial)
            {
               try
               {
                  Dial.number(ed.getText());
                  hang.setEnabled(true);
               }
               catch (IOException ex)
               {
                  dialStatusChange(ex.getClass().toString());
                  if (ex.getMessage() != null) dialStatusChange(ex.getMessage());
               }
            }
            else
            if (e.target == hang)
            {
               hang.setEnabled(false);
               dial.setEnabled(true);
               Dial.hangup();
            }
      }
   }

   public void dialStatusChange(String s)
   {
      if (s.indexOf("idle") >= 0)
      {
         hang.setEnabled(false);
         dial.setEnabled(true);
         repaintNow();
      }
      lb.add(s);
      lb.selectLast();
   }
}
