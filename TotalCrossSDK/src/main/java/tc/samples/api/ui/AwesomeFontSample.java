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
import totalcross.sys.Convert;
import totalcross.sys.Settings;
import totalcross.ui.Label;
import totalcross.ui.ScrollContainer;
import totalcross.ui.ToolTip;

/**
 * Shows all AwesomeFont chars.
 * 
 * Also at: https://fortawesome.github.io/Font-Awesome/cheatsheet/
 * 
 * Some other samples here it, like AccordionSample and BaseMenu.
 */
public class AwesomeFontSample extends BaseContainer {
  @Override
  public void initUI() {
    super.initUI();
    ScrollContainer sc = new ScrollContainer(false, true);
    add(sc, LEFT, TOP + fmH / 4, FILL, FILL - fmH / 4);
    setAwesome(sc, fmH * 2);
    int cols = Math.min(Settings.screenWidth, Settings.screenHeight) / (fmH * 3);
    info = "Hold char to see its unicode value";
    for (int i = 0xF000, j = 0; i <= 0xF27F; i++, j++) {
      Label l = new Label(String.valueOf((char) i), CENTER);
      new ToolTip(l, "\\u" + Convert.unsigned2hex(i, 4));
      sc.add(l, (j % cols) == 0 ? LEFT : AFTER, (j % cols) == 0 ? AFTER : SAME, PARENTSIZE - cols, fmH * 3);
    }
  }
}
