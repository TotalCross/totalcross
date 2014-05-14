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

public class FileSample extends BaseContainer
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

   private void testFileList(ListBox lb)
   {
      lb.add("===== testFileList =====");
      Vector v = new Vector(50);
      if (!recursiveList(rootPath, v))
         lb.add("recursiveList threw an exception");
      else
      {
         lb.add("recursiveList successful");
         int start;
         start = Vm.getTimeStamp();
         String[] files = (String[]) v.toObjectArray();
         if (files == null)
         {
            lb.add("recursiveList found no files");
            files = new String[] {"No files"};
         }
         else
         {
            lb.add("recursiveList found " + files.length);
            if (files[0].charAt(1) == '[') // is it a volume label?
               files[0] = files[0].substring(1); // remove the preceding slash
         }
         add(new ComboBox(files), LEFT, TOP);
         lb.add("recursiveList took " + (Vm.getTimeStamp() - start) + "ms");
      }
   }

   private void testDirectory(ListBox lb)
   {
      lb.add("===== testDirectory =====");
      try
      {
         File f = new File(rootPath + "TempDir");
         boolean exists = f.exists();
         lb.add(rootPath + "TempDir.exists? " + exists);
         if (!exists)
         {
            lb.add("Creating dir...");
            f.createDir();
         }
         lb.add(rootPath + "TempDir.exists after create? " + f.exists());
         lb.add(rootPath + "TempDir isDir?" + f.isDir());
         f.delete();
         f = new File(rootPath + "TempDir");
         lb.add(rootPath + "TempDir.exists after delete? " + f.exists());

         lb.add("testDirectory successful");
      }
      catch (IOException ioe)
      {
         lb.add("testDirectory threw an exception " + ioe.getMessage());
         ioe.printStackTrace();
      }
   }

   private void testFileRename(ListBox lb)
   {
      lb.add("===== testFileRename =====");
      try
      {
         File f = new File(rootPath + "TempRename");
         if (!f.exists())
         {
            lb.add(rootPath + "TempRename does not exist. Creating...");
            f.createDir();
         }
         lb.add(rootPath + "TempRename created? " + f.exists());
         lb.add("renaming tempRename to testRename...");
         f.rename(rootPath + "TestRename");
         // file object is now invalid. create a new one.
         f = new File(rootPath + "TestRename");
         lb.add("TestRename.isDir? " + f.isDir());
         f = new File(rootPath + "TestRename/Teste.txt", File.CREATE);
         lb.add("Renaming Teste.txt to Teste2.txt...");
         f.rename(rootPath + "TestRename/Teste2.txt");
         // file object is now invalid. create a new one
         f = new File(rootPath + "TestRename/Teste2.txt");
         lb.add("Teste2.txt exists? " + f.exists());
         lb.add("Teste2.txt isDir? " + f.isDir());
         f.delete();
         f = new File(rootPath + "TestRename");
         lb.add(rootPath + "TestRename.isDir? " + f.isDir());
         lb.add("Deleting " + rootPath + "TestRename...");
         f.delete();

         f = new File(rootPath + "TestRename");
         lb.add(rootPath + "TestRename.exists? " + f.exists());

         lb.add("testFileRename successful");
      }
      catch (IOException ioe)
      {
         lb.add("testFileRename threw an exception:\n" + ioe.getMessage());
         ioe.printStackTrace();
      }
   }

   private void testFileReadWrite(ListBox lb)
   {
      lb.add("===== testFileReadWrite =====");
      try
      {
         File f = new File(rootPath + "Teste.txt", File.CREATE);
         lb.add("writing values to file...");
         DataStream ds = new DataStream(f);
         ds.writeString("Test");
         ds.writeInt(1234);
         f.setPos(0);
         lb.add("File size now is: " + f.getSize());
         String s = ds.readString();
         int i = ds.readInt();
         lb.add("read: " + s + "," + i);
         lb.add("changing values...");
         f.setPos(0);
         ds.writeString("Abcd");
         f.setPos(0);
         lb.add("File size now is: " + f.getSize());
         s = ds.readString();
         i = ds.readInt();
         lb.add("read: " + s + "," + i);
         f.delete();

         f = new File(rootPath + "Teste.txt");
         lb.add("file deleted? " + !f.exists());

         lb.add("testFileReadWrite successful");
      }
      catch (totalcross.io.IOException ioe)
      {
         lb.add("Test failed");
         lb.add("testFileReadWrite threw an exception " + ioe.getMessage());
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

   private void testAttrTime(ListBox lb)
   {
      lb.add("===== testAttrTime =====");
      lb.add("creating file " + rootPath + "TestAttr.txt");
      try
      {
         File f = new File(rootPath + "TestAttr.txt", File.CREATE);
         int attr = f.getAttributes();
         lb.add("File attributes: " + getAttrDescription(attr));
         lb.add("Setting to hidden...");
         f.setAttributes(attr | File.ATTR_HIDDEN);
         attr = f.getAttributes();
         lb.add("Attributes changed to " + getAttrDescription(attr));

         Time t;
         lb.add("File Created Time:");
         t = f.getTime(File.TIME_CREATED);
         try
         {
            lb.add("" + new Date(t) + " " + t);
         }
         catch (InvalidDateException ide)
         {
            lb.add(ide.getMessage());
         }

         lb.add("File Modified Time:");
         t = f.getTime(File.TIME_MODIFIED);
         try
         {
            lb.add("" + new Date(t) + " " + t);
         }
         catch (InvalidDateException ide)
         {
            lb.add(ide.getMessage());
         }

         lb.add("File Acessed Time:");
         t = f.getTime(File.TIME_ACCESSED);
         try
         {
            lb.add("" + new Date(t) + " " + t);
         }
         catch (InvalidDateException ide)
         {
            lb.add(ide.getMessage());
         }

         lb.add("Changing Modified time to:");
         lb.add("25/03/2000 13:30:15");
         f.setTime(File.TIME_MODIFIED, new Time(2000, 3, 25, 13, 30, 15, 0));
         lb.add("File Modified Time now is:");
         t = f.getTime(File.TIME_MODIFIED);
         try
         {
            lb.add("" + new Date(t) + " " + t);
         }
         catch (InvalidDateException ide)
         {
            lb.add(ide.getMessage());
         }

         lb.add("Deleting file...");
         f.delete();

         f = new File(rootPath + "TestAttr.txt");
         lb.add("File deleted? " + !f.exists());

         lb.add("testAttrTime successful");
      }
      catch (totalcross.io.IOException ioe)
      {
         lb.add("Test failed");
         lb.add("testAttrTime threw an exception " + ioe.getMessage());
         ioe.printStackTrace();
      }
   }

   public void initUI()
   {
      String sdId = null;
      MessageBox mb = new MessageBox("Attention", "Please wait,\nrunning tests...", null);
      mb.popupNonBlocking();

      ListBox lb;
      add(lb = new ListBox());
      lb.enableHorizontalScroll();

      if (sdId != null)
         lb.add("SD unique id: " + sdId);
      else
         lb.add("Not a SD card");

      try
      {
         testFileList(lb);
      }
      catch (OutOfMemoryError oome)
      {
         lb.add("Not all files are shown");
      }
      lb.setRect(LEFT, AFTER + 2, FILL, FILL);
      testAttrTime(lb);
      testDirectory(lb);
      testFileRename(lb);
      testFileReadWrite(lb);

      mb.unpop();
   }
}
