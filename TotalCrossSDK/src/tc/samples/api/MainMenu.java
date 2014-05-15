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

import tc.samples.api.crypto.*;
import tc.samples.api.html.*;
import tc.samples.api.io.*;
import tc.samples.api.io.device.*;
import tc.samples.api.lang.reflection.*;
import tc.samples.api.lang.thread.*;
import tc.samples.api.map.*;
import tc.samples.api.net.*;
import tc.samples.api.net.mail.*;
import tc.samples.api.phone.*;
import tc.samples.api.sys.*;
import tc.samples.api.util.*;
import tc.samples.api.xml.*;

import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.font.*;

public class MainMenu extends BaseContainer
{
   private ButtonMenu menu;
   
   static String DEFAULT_INFO = "Click Info for help";
   String[] items =
   {
      "crypto - Cipher",
      "crypto - Digest",
      "crypto - Signature",
      "html - HtmlContainer",
      "io - File",
      "io - PDBFile",
      "io.device - Bluetooth Print (Citizen)",
      "io.device - Bluetooth Print (Zebra)",
      "io.device - Bluetooth Transfer",
      "io.device - GPS",
      "io.device - Scanner Internal",
      "io.device - Scanner Camera",
      "io.device - PortConnector",
      "lang - Reflection",
      "lang - Thread",
      "map - GoogleMaps",
      "net - Mail",
      "net - FTP",
      "net - Server socket",
      "net - Socket Http",
      "net - Socket Https",
      "phone - Dialer",
      "phone - SMS",
      "sys - Settings",
      "sys - External Viewers",
      "util - Zip",
      "util - Zlib",
      "xml - Soap",
   };
   
   Class[] itemClasses =
   {
      CipherSample.class,
      DigestSample.class,
      SignatureSample.class,
      
      HtmlContainerSample.class,
      
      FileSample.class,
      PDBFileSample.class,
      
      PrinterCitizen.class,
      PrinterZebra.class,
      BTTransfer.class,
      GpsSample.class,
      ScannerInternal.class,
      ScannerZXing.class,
      PortConnectorSample.class,
      
      ReflectionSample.class,
      ThreadSample.class,
      
      GoogleMapsSample.class,
      
      MailSample.class,
      FTPSample.class,
      ServerSocketSample.class,
      SocketSample.class,
      SecureSocketSample.class,
      
      PhoneDialerSample.class,
      PhoneSmsSample.class,
      
      SettingsSample.class,
      ExternalViewersSample.class,
      
      ZipSample.class,
      ZLibSample.class,
      
      SoapSample.class,
      
   };

   protected String getHelpMessage()
   {
      return "This is a TotalCross "+Settings.versionStr+"."+Settings.buildNumber+" sample that shows most of the Application Programming Interfaces available in the SDK. You may drag the menu up and down. Device information: screen: "+Settings.screenWidth+"x"+Settings.screenHeight+", device id: "+Settings.deviceId+", font size: "+Font.NORMAL_SIZE;
   }
   
   public void initUI()
   {
      super.initUI(); // important!
      
      ToolTip.distY = fmH*3;
      menu = new ButtonMenu(items, ButtonMenu.MULTIPLE_VERTICAL);
      
      menu.pressedColor = BKGCOLOR;
      if (Math.max(Settings.screenWidth,Settings.screenHeight)/Font.NORMAL_SIZE > 30)
      {
         menu.borderGap = 100;
         menu.buttonHorizGap = menu.buttonVertGap = 200;
      }
      else menu.buttonHorizGap = 50;
      
      add(menu,LEFT,TOP,FILL,FILL);

      setInfo(DEFAULT_INFO);

      String cmd = MainWindow.getCommandLine();
      if (cmd != null && cmd.startsWith("/t"))
         try 
         {
            showSample(Convert.toInt(cmd.substring(2)));
            return;
         }
         catch (Exception e) {}
   }
   
   public void onEvent(Event e)
   {
      if (e.type == ControlEvent.PRESSED && e.target == menu)
         try
         {
            int idx = menu.getSelectedIndex();
            if (0 <= idx && idx < itemClasses.length)
               showSample(idx);
         }
         catch (Exception ee)
         {
            MessageBox.showException(ee,true);
         }
   }

   private void showSample(int idx) throws Exception
   {
      BaseContainer c = (BaseContainer)itemClasses[idx].newInstance();
      c.info = "Press Back for main menu";
      c.show();
      c.setInfo(c.info);
   }
   
   public void onAddAgain()
   {
      getParentWindow().setMenuBar(null);
   }
}