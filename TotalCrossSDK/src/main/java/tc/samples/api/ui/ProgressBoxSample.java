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

import tc.samples.api.BaseContainer;
import totalcross.sys.Vm;
import totalcross.ui.ButtonMenu;
import totalcross.ui.Spinner;
import totalcross.ui.dialog.MessageBox;
import totalcross.ui.dialog.ProgressBox;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.Event;
import totalcross.ui.gfx.Color;

public class ProgressBoxSample extends BaseContainer
{
  ButtonMenu menu;

  @Override
  public void initUI()
  {
    try
    {
      super.initUI();

      String[] items =
        {
            "Small ProgressBox (style 1)",
            "Big ProgressBox (style 1)",
            "Small ProgressBox (style 2)",
            "Big ProgressBox (style 2)"
        };

      menu = new ButtonMenu(items, ButtonMenu.SINGLE_COLUMN);
      menu.pressedColor = Color.GREEN;
      add(menu,LEFT,TOP,FILL,FILL);

      setInfo("Each test takes 5 seconds");
    }
    catch (Exception ee)
    {
      MessageBox.showException(ee,true);
    }
  }

  @Override
  public void onEvent(Event e)
  {
    if (e.type == ControlEvent.PRESSED && e.target == menu)
    {
      int sel = menu.getSelectedIndex();
      Spinner.spinnerType = sel >= 2 ? Spinner.ANDROID : Spinner.IPHONE;
      String msg = sel == 0 || sel == 2 ? "Loading, please wait..." : "This device will explode\nin 5 seconds...\nthrow it away!";
      ProgressBox pb = new ProgressBox("Message",msg,null);
      pb.popupNonBlocking();
      // we can't just block using Vm.sleep because it would also 
      // block a screen rotation from correctly paint the screen
      Vm.safeSleep(5000);
      pb.unpop();
      setInfo(sel == 1 || sel == 3 ? "BUM!!!!" : "Loaded");
    }
  }
}