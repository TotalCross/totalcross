/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>                         *
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



package tc.samples.app.activation;

import ras.*;

import totalcross.io.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;

public class ManualActivation extends MainWindow
{
   public ManualActivation()
   {
      super("Manual Activation", VERTICAL_GRADIENT);
   }
   
   Button btn;
   
   ListBox lb;
   void log(String s)
   {
      lb.addWrapping(s);
      lb.selectLast();
   }
   
   public void initUI()
   {
      String cmd = getCommandLine();
      add(btn = new Button("Select file"),LEFT,TOP,FILL,PREFERRED+4);
      add(lb = new ListBox(),LEFT,AFTER,FILL,FILL);
      lb.setCursorColor(Color.WHITE);
      gradientTitleStartColor = Color.BLUE;
      gradientTitleEndColor = Color.BLACK;
      if (cmd != null && cmd.length() > 0)
      {
         try
         {
            activate(cmd);
            exit(0);
         }
         catch (Exception e)
         {
            MessageBox.showException(e,true);
            exit(1);
         }
         return;
      }
      log("You're using the user interface mode. You can also call this program in commandline mode, passing the full path of the tcreq.pdb as argument.");
      log("===============================");
   }
   
   public void onEvent(Event e)
   {
      if (e.type == ControlEvent.PRESSED && e.target == btn)
         try
         {
            String sel = selectTCReq();
            if (sel != null)
               activate(sel);
         }
         catch (Exception ee)
         {
            MessageBox.showException(ee,true);
            exit(1);
         }
   }

   private void activate(String fileReq) throws Exception
   {
      log("Reading "+fileReq);
      
      File f = new File(fileReq, File.READ_WRITE);
      byte[] request = new byte[f.getSize()];
      f.readBytes(request, 0, request.length);
      f.close();
      
      log("Sending request...");
      byte[] response = ActivationClient.activate(request);
      
      String fileSuc = Convert.replace(fileReq,"tcreq.pdb","tcsuc.pdb");
      log("Saving "+fileSuc);
      f = new File(fileSuc, File.CREATE_EMPTY);
      f.writeBytes(response);
      f.close();
      
      log("Done.");
   }

   public static String selectTCReq() throws IOException
   {
      FileChooserBox fc = new FileChooserBox("Please select the tcreq.pdb file to activate:",new String[]{"Cancel","Select"}, new FileChooserBox.Filter()
      {
         public boolean accept(File f) throws IOException
         {
            return f.isDir() || f.getPath().endsWith("tcreq.pdb");
         }
      });
      String drive = Settings.appPath;
      if (!Settings.platform.equals(Settings.WIN32))
         drive = "/";
      else
      {
         if (drive.length() < 2 || drive.charAt(1) != ':')
            drive = "c:\\";
         else
            drive = drive.substring(0,3);
      }
      fc.mountTree(drive);
      fc.defaultButton = 1;
      //fc.initialPath = ;
      fc.popup();
      String s;
      if (fc.getPressedButtonIndex() == 1 && (s=fc.getAnswer()) != null && s.toLowerCase().endsWith("tcreq.pdb"))
         return s;
      return null;
   }
}
