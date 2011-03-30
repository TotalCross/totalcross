/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
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



package tc.samples.app.consoleviewer;

import totalcross.io.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;

public class ConsoleViewer extends MainWindow
{
   private Button btnExit,btnDelete,btnConvert;
   private String[] lines;
   private boolean isPalm, isIPhone;
   private File ff;

   public ConsoleViewer()
   {
      super("Console Viewer",Window.RECT_BORDER);
   }
   
   private void error(String s)
   {
      add(new Label(s),CENTER,CENTER);
      repaintNow();
      Vm.sleep(2000);
      exit(0);
   }

   public void initUI()
   {
      isPalm = Settings.platform.equals(Settings.PALMOS);
      isIPhone = Settings.platform.equals(Settings.IPHONE);
      if (!isPalm && !isIPhone)
         error("This program only works\non Palm OS and iPhone.");
      else
      try
      {
         if (isPalm)
            loadFile(new File("/DebugConsole",File.READ_WRITE, 1));
         else
         {
            FileChooserBox fcb = new FileChooserBox(new FileChooserBox.Filter()
            {
               public boolean accept(File f) throws IOException
               {
                  if (f.isDir()) // if its a folder, check if there's a debugconsole on it.
                  {
                     String[] list = f.listFiles();
                     if (list != null)
                        for (int i = 0; i < list.length; i++)
                           if (list[i].indexOf("DebugConsole.txt") >= 0)
                              return true;
                     return false;
                  }
                  String path = f.getPath();
                  return f.isDir() || path.indexOf("DebugConsole.txt") >= 0;
               }
            });
            fcb.mountTree("/Applications",0);
            if (fcb.getTree() == null || fcb.getTree().size() <= 1)
            {
               new MessageBox("Attention", "No DebugConsole.txt found on tree hierarchy!").popup();
               exit(0);
            }
            fcb.popup();
            String a = fcb.getAnswer();
            if (a != null && a.indexOf("DebugConsole.txt") >= 0)
               loadFile(new File(a, File.READ_WRITE));
            else
               exit(0);
         }
      }
      catch (FileNotFoundException fnfe)
      {
         error("No debug console file found");
      }
      catch (Exception e)
      {
         MessageBox.showException(e,false);
         exit(0);
      }
   }

   private void loadFile(File f) throws Exception
   {
      ff = f;
      // read the file
      Vm.gc();
      byte[] buf = new byte[f.getSize()];
      f.readBytes(buf,0,buf.length);
      //f.close(); - don't close! the user may delete the file later
      lines = Convert.tokenizeString(new String(buf),'\n');
      if (lines == null || lines.length == 0)
         error("The file is empty.");
      else
      {           
         setBackColor(Color.CYAN);
         add(btnExit = new Button("Exit"), RIGHT,BOTTOM);
         add(btnDelete = new Button("DELETE and Exit"), BEFORE-10,BOTTOM);
         if (isPalm) add(btnConvert = new Button("Convert to PDB"), BEFORE-10,BOTTOM);
         btnExit.setBackColor(Color.GREEN);
         btnDelete.setBackColor(Color.RED);
         if (isPalm) btnConvert.setBackColor(Color.BLUE);
         ListBox lb;
         add(lb = new ListBox());
         lb.enableHorizontalScroll();
         if (isIPhone)
            lb.extraHorizScrollButtonHeight = 10;
         lb.setRect(LEFT,TOP,FILL,FIT,btnExit);
         lb.add(lines);
      }
   }
   
   public void onExit()
   {
      if (ff != null) 
         try {ff.close();} catch (Exception e) {}
   }
   
   public void onEvent(Event e)
   {
      switch (e.type)
      {
         case ControlEvent.PRESSED:
            if (e.target == btnConvert)
               try
               {
                  byte[] enter = {13,10};
                  PDBFile pdb = new PDBFile("DebugConsole.TCVM.TEXT", PDBFile.CREATE_EMPTY);
                  pdb.setAttributes(PDBFile.DB_ATTR_BACKUP);
                  ResizeRecord rr = new ResizeRecord(pdb, 32000);
                  rr.startRecord();
                  rr.writeBytes(enter,0,enter.length);
                  rr.writeBytes(enter,0,enter.length);
                  for (int i = 0; i < lines.length; i++)
                  {
                     int l = lines[i].length();
                     if (pdb.getRecordSize()+l >= 65000)
                     {
                        rr.endRecord();
                        rr.startRecord();
                     }
                     rr.writeBytes(lines[i].getBytes(), 0, l);
                     rr.writeBytes(enter,0,enter.length);
                  }
                  rr.endRecord();
                  pdb.close();
                  new MessageBox("Attention","DebugConsole.pdb created with success. Don't forget to enable backup in HotSync custom menu to let the file be copied in next hotsync. Exiting...").popup();
                  exit(0);
               }
               catch (Exception ex)
               {
                  MessageBox.showException(ex,false);
                  exit(0);
               }
            else
            if (e.target == btnExit)
               exit(0);
            else
            if (e.target == btnDelete)
            {
               try {ff.delete();} catch (Exception ee) {}
               exit(0);
            }
      }
   }

}
