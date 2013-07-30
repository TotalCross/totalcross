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



package tc.samples.util.zip.zip;

import totalcross.io.File;
import totalcross.io.FileNotFoundException;
import totalcross.io.IOException;
import totalcross.io.IllegalArgumentIOException;
import totalcross.sys.*;
import totalcross.ui.MainWindow;
import totalcross.ui.dialog.FileChooserBox;
import totalcross.ui.dialog.InputBox;
import totalcross.ui.dialog.MessageBox;
import totalcross.util.zip.ZipEntry;
import totalcross.util.zip.ZipException;
import totalcross.util.zip.ZipStream;

public class ZipSample extends MainWindow
{
   byte[] buf = new byte[1024];

   public ZipSample()
   {
      super("ZipSample", RECT_BORDER);
   }

   public void initUI()
   {
      try
      {
         FileChooserBox fcb = new FileChooserBox(null);
         fcb.setTitle("Select the files");
         fcb.multipleSelection = true;
         fcb.mountTree("device/");
         fcb.popup();

         if (fcb.getPressedButtonIndex() == 0)
         {
            String answer = fcb.getAnswer();
            String[] paths = Convert.tokenizeString(answer, ',');

            InputBox input = new InputBox("Zip file path", "Create new file at:", "device/sample.zip");
            input.popup();
            try
            {
               if (input.getPressedButtonIndex() == 0)
               {
                  File destination = new File(input.getValue(), File.CREATE_EMPTY);
                  ZipStream zstream = new ZipStream(destination, ZipStream.DEFLATE);

                  for (int i = 0; i < paths.length; i++)
                  {
                     File file = new File(paths[i], File.DONT_OPEN);
                     if (!file.exists())
                        throw new FileNotFoundException();
                     zstream.putNextEntry(new ZipEntry(file.getPath().substring(1)));
                     if (!file.isDir())
                     {
                        file = new File(paths[i], File.READ_WRITE);
                        int n;
                        while ((n = file.readBytes(buf, 0, 1024)) > 0)
                           zstream.writeBytes(buf, 0, n);
                        file.close();
                     }
                     zstream.closeEntry();
                  }
                  zstream.close();
                  new MessageBox("Success!", "Operation successful!").popup();
               }
            }
            catch (IllegalArgumentIOException e)
            {
               e.printStackTrace();
            }
            catch (FileNotFoundException e)
            {
               e.printStackTrace();
            }
            catch (ZipException e)
            {
               e.printStackTrace();
            }
         }
         MainWindow.exit(0);
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }
}
