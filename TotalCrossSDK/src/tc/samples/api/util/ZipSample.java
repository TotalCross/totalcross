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



package tc.samples.api.util;

import tc.samples.api.*;

import totalcross.io.*;
import totalcross.sys.*;
import totalcross.ui.dialog.*;
import totalcross.util.zip.*;

public class ZipSample extends BaseContainer
{
   byte[] buf = new byte[1024];

   public void initUI()
   {
      super.initUI();
      try
      {
         addLog(LEFT,TOP,FILL,FILL,null);
         FileChooserBox fcb = new FileChooserBox(null);
         fcb.setTitle("Select the files to Zip");
         fcb.multipleSelection = true;
         fcb.mountTree("device/");
         fcb.popup();

         if (fcb.getPressedButtonIndex() != 0)
            log("Cancelled");
         else
         {
            String answer = fcb.getAnswer();
            if (answer == null || answer.length() == 0)
            {
               log("No files selected. Operation cancelled");
               return;
            }
            String[] paths = Convert.tokenizeString(answer, ',');

            InputBox input = new InputBox("Zip file path", "Create new file at:", "device/sample.zip");
            input.popup();
            try
            {
               if (input.getPressedButtonIndex() == 0)
               {
                  int original = 0;
                  File destination = new File(input.getValue(), File.CREATE_EMPTY);
                  ZipStream zstream = new ZipStream(destination, ZipStream.DEFLATE);

                  for (int i = 0; i < paths.length; i++)
                  {
                     log("Adding "+paths[i]);
                     File file = new File(paths[i], File.DONT_OPEN);
                     if (!file.exists())
                        throw new FileNotFoundException();
                     zstream.putNextEntry(new ZipEntry(file.getPath().substring(1)));
                     if (!file.isDir())
                     {
                        file = new File(paths[i], File.READ_WRITE);
                        original += file.getSize();
                        int n;
                        while ((n = file.readBytes(buf, 0, 1024)) > 0)
                           zstream.writeBytes(buf, 0, n);
                        file.close();
                     }
                     zstream.closeEntry();
                  }
                  zstream.close();
                  log("Operation completed.");
                  log("Original size: "+original);
                  log("Final size: "+destination.getSize());
               }
            }
            catch (Exception ee)
            {
               MessageBox.showException(ee,true);
            }
         }
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }
}
