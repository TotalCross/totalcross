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
import totalcross.ui.Switch;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.PressListener;

public class SwitchSample extends BaseContainer implements PressListener {
  Switch s1, s2;

  @Override
  public void initUI() {
    super.initUI();
    s1 = new Switch(true);
    add(s1, CENTER, PARENTSIZE + 30, PARENTSIZE + 50, PREFERRED + fmH);
    s1 = new Switch(true);
    s1.setFont(font.adjustedBy(fmH / 2));
    add(s1, CENTER, AFTER + fmH, PARENTSIZE + 50, PREFERRED + fmH);
    s2 = new Switch(false);
    add(s2, CENTER, AFTER + fmH, PARENTSIZE + 60, PREFERRED + fmH);
    s1.addPressListener(this);
    s2.addPressListener(this);
    s1.textForeOn = "I";
    s1.textForeOff = "O";
    s2.textBackOn = "On";
    s2.textBackOff = "Off";
  }

  @Override
  public void controlPressed(ControlEvent e) {
    if (e.target == s1) {
      s2.setOn(s1.isOn());
    } else {
      s1.setOn(s2.isOn());
    }
  }

}