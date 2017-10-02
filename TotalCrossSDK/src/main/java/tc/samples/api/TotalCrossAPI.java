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

import totalcross.sys.Settings;
import totalcross.sys.Vm;
import totalcross.ui.MainWindow;
import totalcross.ui.Toast;
import totalcross.ui.UIColors;
import totalcross.ui.event.PushNotificationEvent;
import totalcross.ui.event.PushNotificationListener;
import totalcross.ui.gfx.Color;

public class TotalCrossAPI extends MainWindow {
  static {
    Settings.resizableWindow = true;
    Settings.appVersion = "1.06";
    Settings.windowSize = Settings.WINDOWSIZE_480X640;
    Settings.companyContact = "registro@totalcross.com";

    // comentar quando for enviar pra loja Google Play, pois la usa o appid padrao
    Settings.applicationId = "tapi";

    // upload your xap and the page will give you the correct values to put in the properties below
    // These values are placed by tc.Deploy in the file AppxManifest.xml inside the XAP (which is a zip file)
    Settings.companyInfo = "TotalCross";
    Settings.appPackagePublisher = "53F995CF-1FB5-4EC3-84DD-A694BE4CFD1A";
    Settings.appPackageIdentifier = "1748TotalCross.TotalCrossAPI";
    Settings.iosCFBundleIdentifier = "com.totalcross.tcapi";
  }

  public TotalCrossAPI() {
    super("TotalCross API", NO_BORDER);
    setTitle("");
    setUIStyle(Settings.Material);
    setBackColor(UIColors.controlsBack = Color.WHITE);
    Settings.fadeOtherWindows = true;
    UIColors.messageboxBack = Color.brighter(BaseContainer.BKGCOLOR, 64);
    UIColors.messageboxFore = Color.WHITE;
    Settings.scrollDistanceOnMouseWheelMove = fmH * 10;

    // MUST REGISTER AT CONSTRUCTOR, SINCE THE TOKEN MAY BE SENT VERY EARLY
    // You may also use onEvent.
    this.addPushNotificationListener(new PushNotificationListener() {
      @Override
      public void tokenReceived(PushNotificationEvent e) {
        Vm.debug("Token: " + e.message);
      }

      @Override
      public void messageReceived(PushNotificationEvent e) {
        Toast.show("Message received: " + e.message, 3000);
      }
    });
  }

  @Override
  public void initUI() {
    new MainMenu().show();
  }
}
