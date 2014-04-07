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

package tc.samples.ui;

import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;

public class TabbedContainerSample extends BaseContainer
{
   public void initUI()
   {
      try
      {
         super.initUI();
         setTitle("TabbedContainer");
         
         String[] caps = {"juju","dedé","babi"};
         Image [] icons = {new Image("images/ic_dialog_alert.png"),new Image("images/ic_dialog_usb.png"),new Image("images/ic_dialog_info.png")};
         final TabbedContainer tc = new TabbedContainer(caps);
         tc.setBackColor(Color.DARK);
         tc.getContainer(0).setBackColor(0x080C84);
         tc.getContainer(1).setBackColor(0x840C08);
         tc.getContainer(2).setBackColor(0x088608);
         tc.setIcons(icons);
         tc.pressedColor = Color.ORANGE;
         tc.activeTabBackColor = 0xDDDDDD;
         tc.allSameWidth = true;
         tc.extraTabHeight = fmH*2;
         add(tc,LEFT,TOP+fmH,FILL,SCREENSIZE+30);

         final TabbedContainer tc2 = new TabbedContainer(caps);
         tc2.setType(TabbedContainer.TABS_BOTTOM);
         tc2.setBackColor(Color.DARK);
         tc2.getContainer(0).setBackColor(0x080C84);
         tc2.getContainer(1).setBackColor(0x840C08);
         tc2.getContainer(2).setBackColor(0x088608);
         tc2.useOnTabTheContainerColor = true;
         tc2.pressedColor = Color.ORANGE;
         tc2.allSameWidth = true;
         tc2.extraTabHeight = fmH/2;
         add(tc2,LEFT,BOTTOM-fmH,FILL,SCREENSIZE+30);
         
         final Check ch = new Check("Disable last tab");
         add(ch, LEFT+2,CENTER);
         ch.addPressListener(new PressListener()
         {
            public void controlPressed(ControlEvent e)
            {
               tc.setEnabled(2,!ch.isChecked());
               tc2.setEnabled(2,!ch.isChecked());
            }
         });
      }
      catch (Exception ee)
      {
         MessageBox.showException(ee,true);
      }
   }
}