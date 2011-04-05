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



package tc.samples.io.sync.painter;

import totalcross.io.*;
import totalcross.io.sync.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.image.*;

public class SyncPanel extends Container
{
   String imageName = "image2.png";
   byte[] imageBytes;

   class PaintRecord extends RemotePDBRecord
   {
      protected void read(DataStream ds) throws totalcross.io.IOException
      {
         // write to the file
         File out = new File(Convert.appendPath(Settings.appPath, imageName), File.CREATE_EMPTY);
         imageBytes = new byte[size];
         ds.readBytes(imageBytes, 0, size);
         out.writeBytes(imageBytes, 0, size);
         out.close();
      }

      protected void write(DataStream ds)
      {
      }
   }

   private Image bmp; // set by the PaintRecord
   Label status;

   public void initUI()
   {
      add(status = new Label("", CENTER), LEFT, BOTTOM);
      MainWindow.getMainWindow().repaintNow();

      showStatus("Synchronizing Painter for " + Settings.userName, false);
      String catName = "BMP.PAin.BITM";

      String[] cats = RemotePDBFile.listPDBs(Convert.chars2int("PAin"), Convert.chars2int("BITM"));
      if (cats == null)
         showStatus("No bitmaps to synchronize", true);
      else if (cats[0].equals(catName))
      {
         showStatus(cats[0], false);
         try
         {
            RemotePDBFile rc = new RemotePDBFile(catName);
            showStatus("Retrieving record...", false);
            PaintRecord pr = new PaintRecord();
            if (rc.getRecordCount() > 0 && rc.readRecord(0, pr))
            {
               rc.delete(); // already closes it (of course).
               showStatus("Bitmap stored on /TotalCross/" + imageName, true);
            }
            else
            {
               rc.close();
               showStatus("Error! Bitmap not synchronized.", true);
            }
            
            try
            {
               bmp = new Image(imageBytes); // show it here
               add(new ImageControl(bmp), CENTER, TOP + 3);
               repaintNow();
               Vm.sleep(3000); // let the user view the image                
            }
            catch (ImageException e)
            {
               MessageBox.showException(e, true);
            }            
         }
         catch (IOException e)
         {
            MessageBox.showException(e, true);
         }
      }
   }

   private void showStatus(String s, boolean log2)
   {
      status.setText(s);
      status.repaintNow();
      if (log2)
      {
         Conduit.log(s);
         Vm.sleep(1000);
      }
   }
}
