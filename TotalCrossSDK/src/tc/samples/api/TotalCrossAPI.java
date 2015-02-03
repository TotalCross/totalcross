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

package tc.samples.api;

import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.gfx.*;

public class TotalCrossAPI extends MainWindow
{
   static
   {
      Settings.resizableWindow = true;
      Settings.appVersion = "1.03";
      Settings.windowSize = Settings.WINDOWSIZE_480X640;
      Settings.windowFont = 16;
   }
   
   public TotalCrossAPI()
   {
      super("TotalCross API",NO_BORDER);
      setTitle("");
      setUIStyle(Settings.Android);
      setBackColor(UIColors.controlsBack = Color.WHITE);
      Settings.fadeOtherWindows = true;
      UIColors.messageboxBack = Color.brighter(BaseContainer.BKGCOLOR,64);
      UIColors.messageboxFore = Color.WHITE;
      Vm.debug(Vm.ALTERNATIVE_DEBUG);
   }
   
   public void initUI()
   {
      new MainMenu().show();
   }
}
