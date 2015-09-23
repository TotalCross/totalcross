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
      Settings.companyContact = "registro@totalcross.com";
      Settings.applicationId = "tapi";
      // upload your xap and the page will give you the correct values to put in the properties below
      // These values are placed by tc.Deploy in the file AppxManifest.xml inside the XAP (which is a zip file)
      Settings.companyInfo = "TotalCross";
      Settings.appPackagePublisher = "53F995CF-1FB5-4EC3-84DD-A694BE4CFD1A";
      Settings.appPackageIdentifier = "1748TotalCross.TotalCrossAPI";
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
