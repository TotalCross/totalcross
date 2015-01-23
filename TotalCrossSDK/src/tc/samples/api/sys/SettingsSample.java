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



package tc.samples.api.sys;

import tc.samples.api.*;

import totalcross.sys.*;
import totalcross.ui.font.*;

public class SettingsSample extends BaseContainer
{
   public void initUI()
   {
      super.initUI();
      addLog(LEFT, TOP, FILL, FILL,null);
      log("Version is " + Settings.versionStr,false);
      log("Build number is "+Settings.buildNumber,false);
      log("Platform is " + Settings.platform,false);
      log("User is " + Settings.userName,false);
      log("Pen is " + (Settings.keyboardFocusTraversable ? "missing" : "available"),false);
      log("Virtual keyboard is " + Settings.virtualKeyboard,false);
      log("Screen is " + Settings.screenWidth + "x" + Settings.screenHeight,false);
      log("Screen dpi is " + Settings.screenWidthInDPI + "x" + Settings.screenHeightInDPI,false);
      log("Default font size is " + Font.NORMAL_SIZE,false);
      log("Device font height is " + Settings.deviceFontHeight,false);
      log("Screen bpp is " + Settings.screenBPP,false);
      log("timeZoneMinutes is " + Settings.timeZoneMinutes,false);
      log("daylightSavingsMinutes is " + Settings.daylightSavingsMinutes,false);
      log("dateFormat is " + Settings.dateFormat,false);
      log("dateSeparator is " + Settings.dateSeparator,false);
      log("decimalSeparator is " + Settings.decimalSeparator,false);
      log("thousandsSeparator is " + Settings.thousandsSeparator + "  ",false);
      log("timeSeparator is " + Settings.timeSeparator,false);
      log("is24Hour is " + Settings.is24Hour,false);
      log("weekStart is " + Settings.weekStart,false);
      log("Battery is at " + Vm.getRemainingBattery() + "%",false);
      log("Free memory is at " + Vm.getFreeMemory(),false);
      log("Line number is " + Settings.lineNumber,false);
      log("Rom serial number is " + Settings.romSerialNumber,false);
      log("IMEI is "+ Settings.imei,false);
      log("ICCID is "+ Settings.iccid,false);
      log("Rom version is " + Settings.romVersion,false);
      log("Device id is " + Settings.deviceId,false);
      log("App path is " + Settings.appPath,false);
   }
}
