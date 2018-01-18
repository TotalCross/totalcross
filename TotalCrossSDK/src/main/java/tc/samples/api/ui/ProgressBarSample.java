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
import totalcross.sys.Vm;
import totalcross.ui.Container;
import totalcross.ui.Control;
import totalcross.ui.ProgressBar;
import totalcross.ui.dialog.MessageBox;
import totalcross.ui.gfx.Color;

public class ProgressBarSample extends BaseContainer {
  ProgressBar pbe, pbzv, pbv, pbzh, pbh;

  @Override
  public void initUI() {
    try {
      super.initUI();
      isSingleCall = true;

      Container sc = new Container();
      sc.setInsets(gap, gap, gap, gap);
      add(sc, LEFT, TOP, FILL, FILL);

      pbh = new ProgressBar();
      pbh.max = 50;
      pbh.highlight = true;
      pbh.suffix = " of " + pbh.max;
      pbh.textColor = 0xAAAA;
      sc.add(pbh, LEFT, TOP, FILL, PREFERRED);

      // endless ProgressBar
      pbe = new ProgressBar();
      pbe.max = width / 4; // max-min = width of the bar
      pbe.setEndless();
      pbe.setBackColor(Color.YELLOW);
      pbe.setForeColor(Color.ORANGE);
      pbe.prefix = "Loading, please wait...";
      sc.add(pbe, LEFT, AFTER + gap, FILL, PREFERRED);
      pbzh = new ProgressBar();
      pbzh.max = 50;
      pbzh.drawText = false;
      pbzh.setBackForeColors(Color.DARK, Color.RED);
      sc.add(pbzh, LEFT, AFTER + gap, FILL, FONTSIZE + 50);

      final int max = Settings.onJavaSE ? 2000 : 200;
      // vertical ones
      pbv = new ProgressBar();
      pbv.vertical = true;
      pbv.max = max;
      pbv.suffix = "";
      pbv.textColor = Color.BLUE;
      pbv.setBackColor(Color.CYAN);
      pbv.setForeColor(Color.GREEN);
      sc.add(pbv, RIGHT, AFTER + gap, PREFERRED, FILL);

      pbzv = new ProgressBar();
      pbzv.vertical = true;
      pbzv.max = 50;
      pbzv.drawText = false;
      pbzv.setBackForeColors(Color.RED, Color.DARK);

      sc.add(pbzv, BEFORE - gap, SAME, FONTSIZE + 50, SAME);
    } catch (Exception ee) {
      MessageBox.showException(ee, true);
    }
  }

  @Override
  public void onSwapFinished() {
    final int ini = Vm.getTimeStamp();
    repaintNow();
    // runs the bench test
    int max = pbv.max;
    for (int i = max; --i >= 0;) {
      int v = pbh.getValue();
      v = (v + 1) % (pbh.max + 1);
      Control.enableUpdateScreen = false; // since each setValue below updates the screen, we disable it to let it paint all at once at the end
      pbh.setValue(v);
      pbv.setValue(i);
      pbe.setValue(5); // increment value
      pbzh.setValue(v);
      Control.enableUpdateScreen = true;
      pbzv.setValue(v);
      if (Settings.onJavaSE) {
        Vm.sleep(20);
      }
    }
    int ela = Vm.getTimeStamp() - ini;
    info = "Elapsed: " + ela + "ms (" + Convert.toString((double) ela / pbv.max, 1) + " ms per frame)";
    Vm.debug(info);
  }
}