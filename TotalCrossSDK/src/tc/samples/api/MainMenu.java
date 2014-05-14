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
import totalcross.ui.gfx.*;

public class MainMenu extends BaseContainer
{
   private ButtonMenu menu;
   
   static String DEFAULT_INFO = "Click Info for help. Hold button for tip";
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
      
      HtmlBrowser.class,
      
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
   
   String[] tips = 
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

   BaseContainer[] itemInstances = new BaseContainer[itemClasses.length];
   
   protected String getHelpMessage()
   {
      return "This is a TotalCross "+Settings.versionStr+" sample that shows most of the user interface controls available in the SDK. In this screen you can see the Bar control (at the header and footer), and also the new ButtonMenu (the menu at the middle). You may drag the menu up and down. Device information: screen: "+Settings.screenWidth+"x"+Settings.screenHeight+", device id: "+Settings.deviceId+", font size: "+Font.NORMAL_SIZE;
   }
   
   private void addToolTip(Control c, String text)
   {
      ToolTip t = new ToolTip(c,text);
      t.millisDelay = 500;
      t.millisDisplay = 5000;
      t.borderColor = Color.BLACK;
      t.setBackColor(0xF0F000);
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
      for (int i = 0; i < tips.length; i++)
         addToolTip(menu.getButton(i), ToolTip.split(tips[i],fm));
      if (!Settings.isOpenGL && !Settings.onJavaSE)
         menu.getButton(tips.length-2).setEnabled(false);

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
      BaseContainer c = itemInstances[idx] == null ? itemInstances[idx] = (BaseContainer)itemClasses[idx].newInstance() : itemInstances[idx];
      c.info = "Press Back for main menu";
      c.show();
      c.setInfo(c.info);
      if (c.isSingleCall)         
         itemInstances[idx] = null;
   }
   
   public void onAddAgain()
   {
      getParentWindow().setMenuBar(null);
   }
}