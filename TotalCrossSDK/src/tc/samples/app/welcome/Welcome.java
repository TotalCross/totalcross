/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>                         *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
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

// $Id: Welcome.java,v 1.21 2011-01-04 13:19:29 guich Exp $

package tc.samples.app.welcome;

import totalcross.io.File;
import totalcross.sys.Convert;
import totalcross.sys.Settings;
import totalcross.sys.Vm;
import totalcross.ui.Button;
import totalcross.ui.Label;
import totalcross.ui.ListBox;
import totalcross.ui.MainWindow;
import totalcross.ui.MenuBar;
import totalcross.ui.MenuItem;
import totalcross.ui.Window;
import totalcross.ui.dialog.*;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.Event;
import totalcross.ui.font.Font;
import totalcross.ui.gfx.Color;

/**
 * Welcome is the welcome application.
 * <p>
 * This is the default program run when none is specified or when the VM needs
 * a program to run to show that the VM is functioning on a device.
 */

public class Welcome extends MainWindow
{
   private MenuBar mbar;
   private MenuItem miAudibleGC, miDumpMem, miCheckLeak, miPrintStack,miDebug2NVFS, miReadNVFS;

   public Welcome()
   {
      super("Welcome",TAB_ONLY_BORDER);
      totalcross.sys.Settings.applicationId = "SWAB";
   }

   public void initUI()
   {
      MenuItem fileMenu[] =
      {
        new MenuItem("File"),
        miReadNVFS = new MenuItem("Read NVFS memo"),
        new MenuItem(),
        new MenuItem("Exit"),
      };
      MenuItem optionsMenu[] =
      {
         new MenuItem("VM"),
         miAudibleGC = new MenuItem("Audible GC",false),
         miDumpMem   = new MenuItem("Dump Memory Stats",false),
         miCheckLeak = new MenuItem("Check memory leak",false), // guich@570_100: now memory leak detection can be turned on from here.
         miPrintStack= new MenuItem("Print stack trace",false),  // guich@580_35
         miDebug2NVFS= new MenuItem("Debug to NVFS",false) // guich@580_36
      };
      MenuItem helpMenu[] =
      {
         new MenuItem("?"),
         new MenuItem("About"),
      };
      miReadNVFS.isEnabled = miDebug2NVFS.isEnabled = Settings.platform.equals(Settings.PALMOS);
      setMenuBar(mbar = new MenuBar(new MenuItem[][]{fileMenu,optionsMenu,helpMenu}));
      if (Settings.isColor)
      {
         mbar.setBackForeColors(Color.BLUE, Color.WHITE);
         mbar.setCursorColor(Color.getRGB(100,100,255));
         mbar.setBorderStyle(NO_BORDER);
         mbar.setPopColors(Color.getRGB(0,120,255),Color.CYAN,-1); // use the default cursor color for the popup menu (last null param)
      }

      // now display the UI elements
      Label l = new Label("TotalCross Virtual Machine");
      l.setInvert(true);
      l.setFont(Font.getFont(this.font.name,true,Font.BIG_SIZE));
      add(l,CENTER,TOP+20);

      add(new Label("Version "+Settings.versionStr+" for " + Settings.platform),CENTER,AFTER+3);
      if (Settings.deviceId != null)
         add(new Label("Running on "+Settings.deviceId),CENTER,AFTER+3);
      add(new Label("Virtual Machine installed and ready"),CENTER,CENTER+15);
      add(new Label("SuperWaba Ltda"),CENTER,BOTTOM);
      // recover last appSettings status for the menu items
      if (Settings.appSettings != null)
      {
         if (Settings.appSettings.length() == 2) // guich@570_100: if old settings. add two extras
            Settings.appSettings += "000";
         if (Settings.appSettings.length() == 3) // guich@580_34: if old settings. add an extra one
            Settings.appSettings += "00";
         if (Settings.appSettings.length() == 5)
         {
            miAudibleGC.isChecked = Settings.appSettings.charAt(0) == '1';
            miDumpMem.isChecked   = Settings.appSettings.charAt(1) == '1';
            miCheckLeak.isChecked = Settings.appSettings.charAt(2) == '1';
            miPrintStack.isChecked= Settings.appSettings.charAt(3) == '1';
            miDebug2NVFS.isChecked= Settings.appSettings.charAt(4) == '1';
         }
      }
   }

   public void onExit()
   {
      if (mbar == null) return; // only in debugs
      if (!miAudibleGC.isChecked && !miDumpMem.isChecked && !miCheckLeak.isChecked && !miPrintStack.isChecked && !miDebug2NVFS.isChecked)
         Settings.appSettings = null; // no options, delete appSettings
      else
         Settings.appSettings = (miAudibleGC.isChecked ?"1":"0") +
                                (miDumpMem.isChecked   ?"1":"0") +
                                (miCheckLeak.isChecked ?"1":"0") +
                                (miPrintStack.isChecked?"1":"0") +
                                (miDebug2NVFS.isChecked?"1":"0");
   }

   class View extends Window
   {
      private Button btnBack,btnDelete;
      public View(File f) throws totalcross.io.IOException
      {
         super("Debug console",Window.RECT_BORDER);
         byte[] buf = new byte[f.getSize()];
         f.readBytes(buf,0,buf.length);
         f.close();
         String[] lines = Convert.tokenizeString(new String(buf),'\n');
         setBackColor(Color.CYAN);
         //dontSaveBehind(true);
         makeUnmovable();
         setRect(CENTER,CENTER,Settings.screenWidth-6,Settings.screenHeight-6);
         add(btnBack = new Button("Back"), RIGHT,BOTTOM);
         btnBack.setBackColor(Color.GREEN);
         add(btnDelete = new Button("DELETE"), BEFORE-10,BOTTOM);
         btnDelete.setBackColor(Color.RED);
         ListBox lb;
         add(lb = new ListBox());
         lb.enableHorizontalScroll();
         lb.setRect(LEFT,TOP,FILL,FIT,btnBack);
         lb.add(lines);
      }
      public void onEvent(Event e)
      {
         switch (e.type)
         {
            case ControlEvent.PRESSED:
               if (e.target == btnBack)
                  unpop();
               else
               if (e.target == btnDelete)
               {
                  Vm.debug(Vm.ERASE_DEBUG);
                  unpop();
               }
         }
      }
   }

   public void onEvent(Event e)
   {
      switch (e.type)
      {
         case ControlEvent.PRESSED:
            if (e.target == mbar)
               switch (mbar.getSelectedIndex())
               {
                  case 1:
                  {
                     File f;
                     try
                     {
                        try
                        {
                           f = new File("/DebugConsole",File.READ_WRITE, 1);
                        }
                        catch (totalcross.io.FileNotFoundException fnfe)
                        {
                           new MessageBox("Attention","File DebugConsole on the\nNVFS volume was not found.").popupNonBlocking();
                           break;
                        }

                        MessageBox mb=null;
                        try
                        {
                           mb = new MessageBox("Wait","Loading and parsing file...",null);
                           mb.popupNonBlocking();
                           // the file is surely less than 32k, so we can read it at once
                           mb.unpop();
                           mb = null;
                           new View(f).popupNonBlocking();
                        }
                        catch (OutOfMemoryError oome)
                        {
                           new MessageBox("Error","Out of memory.").popupNonBlocking();
                        }
                        if (mb != null)
                           mb.unpop();
                     }
                     catch (totalcross.io.IOException ioe)
                     {
                        new MessageBox("Attention","An exception ocurred while trying\nto open the DebugConsole.\n" + ioe).popupNonBlocking();
                     }
                     break;
                  }
                  case 3: // Exit
                     exit(0);
                     break;
                  case 104:
                     MessageBox mb;
                     mb = new MessageBox("Caution","Are you sure? This is not\na secure feature and at some\ntimes can reset the device\nwhile printing the trace!",new String[]{"Yes","No"});
                     mb.popup();
                     miPrintStack.isChecked = mb.getPressedButtonIndex() == 0;
                     break;
                  case 201: // About
                     new MessageBox("About","TotalCross Virtual Machine\nwww.totalcross.com\nCopyright (c) 2000-2011\nSuperWaba Ltda\nPhone: +55 21 2239-6695\nRio de Janeiro - Brazil").popupNonBlocking();
                     break;
               }
            break;
      }
   }
}
