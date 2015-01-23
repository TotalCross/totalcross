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

package tc.samples.api.io;

import tc.samples.api.*;

import totalcross.io.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.util.*;

public class FileSample extends BaseContainer implements Runnable
{
   private String rootPath = Settings.appPath+"/";

   private boolean recursiveList(String path, Vector v)
   {
      if (path == null)
         return false;
      try
      {
         File file = new File(path);
         String[] list = file.listFiles();
         if (list != null)
         {
            for (int i = 0; i < list.length; i++)
            {
               if (list[i] != null)
               {
                  v.addElement(path + list[i]);
                  if (list[i].endsWith("/")) // is a path?
                     recursiveList(path + list[i], v);
               }
            }
         }
         file.close();
      }
      catch (IOException ioe)
      {
         ioe.printStackTrace();
         Vm.debug(ioe.getMessage());
         return false;
      }
      return true;
   }

   private void testFileList()
   {
      log("===== testFileList =====");
      Vector v = new Vector(50);
      if (!recursiveList(rootPath, v))
         log("recursiveList threw an exception");
      else
      {
         log("recursiveList successful");
         int start;
         start = Vm.getTimeStamp();
         String[] files = (String[]) v.toObjectArray();
         if (files == null)
         {
            log("recursiveList found no files");
            files = new String[] {"No files"};
         }
         else
         {
            log("recursiveList found " + files.length);
            if (files[0].charAt(1) == '[') // is it a volume label?
               files[0] = files[0].substring(1); // remove the preceding slash
         }
         add(new ComboBox(files), LEFT, TOP);
         log("recursiveList took " + (Vm.getTimeStamp() - start) + "ms");
      }
   }

   private void testDirectory()
   {
      log("===== testDirectory =====");
      try
      {
         File f = new File(rootPath + "TempDir");
         boolean exists = f.exists();
         log(rootPath + "TempDir.exists? " + exists);
         if (!exists)
         {
            log("Creating dir...");
            f.createDir();
         }
         log(rootPath + "TempDir.exists after create? " + f.exists());
         log(rootPath + "TempDir isDir?" + f.isDir());
         f.delete();
         f = new File(rootPath + "TempDir");
         log(rootPath + "TempDir.exists after delete? " + f.exists());

         log("testDirectory successful");
      }
      catch (IOException ioe)
      {
         log("testDirectory threw an exception " + ioe.getMessage());
         ioe.printStackTrace();
      }
   }

   private void testFileRename()
   {
      log("===== testFileRename =====");
      try
      {
         File f = new File(rootPath + "TempRename");
         if (!f.exists())
         {
            log(rootPath + "TempRename does not exist. Creating...");
            f.createDir();
         }
         log(rootPath + "TempRename created? " + f.exists());
         log("renaming tempRename to testRename...");
         f.rename(rootPath + "TestRename");
         // file object is now invalid. create a new one.
         f = new File(rootPath + "TestRename");
         log("TestRename.isDir? " + f.isDir());
         f = new File(rootPath + "TestRename/Teste.txt", File.CREATE);
         log("Renaming Teste.txt to Teste2.txt...");
         f.rename(rootPath + "TestRename/Teste2.txt");
         // file object is now invalid. create a new one
         f = new File(rootPath + "TestRename/Teste2.txt");
         log("Teste2.txt exists? " + f.exists());
         log("Teste2.txt isDir? " + f.isDir());
         f.delete();
         f = new File(rootPath + "TestRename");
         log(rootPath + "TestRename.isDir? " + f.isDir());
         log("Deleting " + rootPath + "TestRename...");
         f.delete();

         f = new File(rootPath + "TestRename");
         log(rootPath + "TestRename.exists? " + f.exists());

         log("testFileRename successful");
      }
      catch (IOException ioe)
      {
         log("testFileRename threw an exception:\n" + ioe.getMessage());
         ioe.printStackTrace();
      }
   }

   private void testFileReadWrite()
   {
      log("===== testFileReadWrite =====");
      try
      {
         File f = new File(rootPath + "Teste.txt", File.CREATE);
         log("writing values to file...");
         DataStream ds = new DataStream(f);
         ds.writeString("Test");
         ds.writeInt(1234);
         f.setPos(0);
         log("File size now is: " + f.getSize());
         String s = ds.readString();
         int i = ds.readInt();
         log("read: " + s + "," + i);
         log("changing values...");
         f.setPos(0);
         ds.writeString("Abcd");
         f.setPos(0);
         log("File size now is: " + f.getSize());
         s = ds.readString();
         i = ds.readInt();
         log("read: " + s + "," + i);
         f.delete();

         f = new File(rootPath + "Teste.txt");
         log("file deleted? " + !f.exists());

         log("testFileReadWrite successful");
      }
      catch (totalcross.io.IOException ioe)
      {
         log("Test failed");
         log("testFileReadWrite threw an exception " + ioe.getMessage());
         ioe.printStackTrace();
      }
   }

   private String getAttrDescription(int attr)
   {
      String s = "";
      if ((attr & File.ATTR_ARCHIVE) != 0)
         s += "A";
      if ((attr & File.ATTR_HIDDEN) != 0)
         s += "H";
      if ((attr & File.ATTR_READ_ONLY) != 0)
         s += "R";
      if ((attr & File.ATTR_SYSTEM) != 0)
         s += "S";
      return s;
   }

   private void testAttrTime()
   {
      log("===== testAttrTime =====");
      log("creating file " + rootPath + "TestAttr.txt");
      try
      {
         File f = new File(rootPath + "TestAttr.txt", File.CREATE);
         int attr = f.getAttributes();
         log("File attributes: " + getAttrDescription(attr));
         log("Setting to hidden...");
         f.setAttributes(attr | File.ATTR_HIDDEN);
         attr = f.getAttributes();
         log("Attributes changed to " + getAttrDescription(attr));

         Time t;
         log("File Created Time:");
         t = f.getTime(File.TIME_CREATED);
         try
         {
            log("" + new Date(t) + " " + t);
         }
         catch (InvalidDateException ide)
         {
            log(ide.getMessage());
         }

         log("File Modified Time:");
         t = f.getTime(File.TIME_MODIFIED);
         try
         {
            log("" + new Date(t) + " " + t);
         }
         catch (InvalidDateException ide)
         {
            log(ide.getMessage());
         }

         log("File Acessed Time:");
         t = f.getTime(File.TIME_ACCESSED);
         try
         {
            log("" + new Date(t) + " " + t);
         }
         catch (InvalidDateException ide)
         {
            log(ide.getMessage());
         }

         log("Changing Modified time to:");
         log("25/03/2000 13:30:15");
         f.setTime(File.TIME_MODIFIED, new Time(2000, 3, 25, 13, 30, 15, 0));
         log("File Modified Time now is:");
         t = f.getTime(File.TIME_MODIFIED);
         try
         {
            log("" + new Date(t) + " " + t);
         }
         catch (InvalidDateException ide)
         {
            log(ide.getMessage());
         }

         log("Deleting file...");
         f.delete();

         f = new File(rootPath + "TestAttr.txt");
         log("File deleted? " + !f.exists());

         log("testAttrTime successful");
      }
      catch (totalcross.io.IOException ioe)
      {
         log("Test failed");
         log("testAttrTime threw an exception " + ioe.getMessage());
         ioe.printStackTrace();
      }
   }

   public void run()
   {
      String sdId = null;
      MessageBox mb = new MessageBox("Attention", "Please wait,\nrunning tests...", null);
      mb.popupNonBlocking();
      if (sdId != null)
         log("SD unique id: " + sdId);
      else
         log("Not a SD card");

      try
      {
         testFileList();
      }
      catch (OutOfMemoryError oome)
      {
         log("Not all files are shown");
      }
      testAttrTime();
      testDirectory();
      testFileRename();
      testFileReadWrite();

      mb.unpop();
   }
   
   public void initUI()
   {
      super.initUI();
      addLog(LEFT, TOP + fmH*2, FILL, FILL,null);
      MainWindow.getMainWindow().runOnMainThread(this); // allow animation
   }
}
