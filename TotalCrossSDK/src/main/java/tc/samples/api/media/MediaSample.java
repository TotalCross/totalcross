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

package tc.samples.api.media;

import tc.samples.api.BaseContainer;
import totalcross.io.File;
import totalcross.sys.Settings;
import totalcross.sys.Vm;
import totalcross.ui.Button;
import totalcross.ui.dialog.MessageBox;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.PressListener;
import totalcross.ui.media.Sound;

public class MediaSample extends BaseContainer {
  @Override
  public void initUI() {
    try {
      super.initUI();
      // you may write the file only once; here we write always because it is pretty small, just 29k
      final boolean isWAV = Settings.isWindowsCE() || Settings.platform.equals(Settings.WIN32);
      final String ext = isWAV ? "wav" : "mp3";
      // note: we're using ext concatenation here to prevent the wav/mp3 files being added twice to the TCZ
      new File("device/sample." + ext, File.CREATE_EMPTY)
          .writeAndClose(Vm.getFile(isWAV ? "tc/samples/api/sample.wav" : "tc/samples/api/sample.mp3"));

      final Button b = new Button(isWAV ? "Play WAV sample" : "Play MP3 sample");
      add(b, CENTER, CENTER);
      b.addPressListener(new PressListener() {
        @Override
        public void controlPressed(ControlEvent e) {
          try {
            Sound.play("device/sample." + ext);
          } catch (Exception ee) {
            MessageBox.showException(ee, true);
          }
        }
      });
    } catch (Exception e) {
      MessageBox.showException(e, true);
    }
  }
}
