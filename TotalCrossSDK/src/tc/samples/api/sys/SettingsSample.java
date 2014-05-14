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
import totalcross.ui.*;
import totalcross.ui.font.*;

public class SettingsSample extends BaseContainer
{
   public void initUI()
   {
      ListBox lb = new ListBox(new String[]
      {
         "Version is " + Settings.versionStr,
         "Build number is "+Settings.buildNumber,
         "Platform is " + Settings.platform,
         "User is " + Settings.userName,
         "Pen is " + (Settings.keyboardFocusTraversable ? "missing" : "available"),
         "Virtual keyboard is " + Settings.virtualKeyboard,
         "Screen is " + Settings.screenWidth + "x" + Settings.screenHeight,
         "Screen dpi is " + Settings.screenWidthInDPI + "x" + Settings.screenHeightInDPI,
         "Default font size is " + Font.NORMAL_SIZE,
         "Device font height is " + Settings.deviceFontHeight,
         "Screen bpp is " + Settings.screenBPP,
         "timeZoneMinutes is " + Settings.timeZoneMinutes,
         "daylightSavingsMinutes is " + Settings.daylightSavingsMinutes,
         "dateFormat is " + Settings.dateFormat,
         "dateSeparator is " + Settings.dateSeparator,
         "decimalSeparator is " + Settings.decimalSeparator,
         "thousandsSeparator is " + Settings.thousandsSeparator + "  ",
         "timeSeparator is " + Settings.timeSeparator,
         "is24Hour is " + Settings.is24Hour,
         "weekStart is " + Settings.weekStart,
         "Battery is at " + Vm.getRemainingBattery() + "%",
         "Free memory is at " + Vm.getFreeMemory(),
         "Line number is " + Settings.lineNumber,
         "Rom serial number is " + Settings.romSerialNumber,
         "IMEI is "+ Settings.imei,
         "ICCID is "+ Settings.iccid,
         "Rom version is " + Settings.romVersion,
         "Device id is " + Settings.deviceId,
         "App path is " + Settings.appPath,
      });
      lb.enableHorizontalScroll();
      add(lb, LEFT, TOP, FILL, FILL);
      lb.requestFocus();
   }
}
