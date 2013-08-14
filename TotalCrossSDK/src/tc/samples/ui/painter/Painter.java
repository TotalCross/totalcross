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

package tc.samples.ui.painter;

import totalcross.io.*;
import totalcross.sys.Settings;
import totalcross.sys.Vm;
import totalcross.ui.*;
import totalcross.ui.dialog.MessageBox;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.Event;
import totalcross.ui.gfx.Color;
import totalcross.ui.image.Image;
import totalcross.ui.image.ImageException;

public class Painter extends MainWindow
{
   Button save;
   Button clear;
   Button load;
   Whiteboard paint;
   Label status;
   ComboBox cbColors;
   String catalogName = "BMP.PAin.BITM";
   String fileName = "/image.png";

   static
   {
      totalcross.sys.Settings.applicationId = "PAin";
   }

   public Painter()
   {
      setUIStyle(Settings.Android);
   }

   public void initUI()
   {
      save = new Button("Save");
      clear = new Button("Clear");
      load = new Button("Load");
      paint = new Whiteboard();
      status = new Label("Status", CENTER);

      // center the save/load/clear buttons on screen
      add(load, CENTER, BOTTOM - 5,PREFERRED+fmH,PREFERRED+fmH);
      add(save, BEFORE - 5, SAME,PREFERRED+fmH,PREFERRED+fmH); // before the load button
      add(clear, AFTER + 5, SAME,PREFERRED+fmH,PREFERRED+fmH, load); // after the load button add the paint control with a specified width
      add(paint);
      paint.borderColor = Color.BLACK;
      paint.setRect(CENTER, TOP + 5, SCREENSIZEMIN+90, SCREENSIZEMIN+50); // add the status
      add(status);
      status.setRect(0, AFTER + 5, FILL, PREFERRED);
      add(cbColors = new ComboBox(new ColorList()), CENTER, AFTER + 5);
      cbColors.setSelectedIndex(cbColors.indexOf(new ColorList.Item(Color.BLACK))); // select color 0
   }

   public void onEvent(Event event)
   {
      if (event.type == ControlEvent.PRESSED)
      {
         if (event.target == cbColors)
            paint.setPenColor(((ColorList.Item) cbColors.getSelectedItem()).value);
         else
         if (event.target == clear)
            paint.clear();
         else
         if (event.target == save)
         {
            // note: we could use the saveTo method of totalcross.ui.image.Image, but for
            // example purposes, we will show how to use the other method, createPng
            // create a buffer to store the image
            try
            {
               // create
               File f = new File(Settings.isOpenGL ? "/sdcard"+fileName : Settings.appPath+fileName, File.CREATE_EMPTY);
               PDBFile cat = new PDBFile(catalogName, PDBFile.CREATE_EMPTY); // always keep only one record on the catalog
               ByteArrayStream bas = new ByteArrayStream(500);
               try
               {
                  paint.getImage().createPng(bas);
                  int totalBytesWritten = bas.getPos();
                  byte[] pictureBytes = bas.getBuffer();

                  if (cat.getRecordCount() == 0)
                     cat.addRecord(totalBytesWritten);
                  else
                  {
                     cat.setRecordPos(0);
                     cat.resizeRecord(totalBytesWritten);
                  }
                  int written1 = f.writeBytes(pictureBytes, 0, totalBytesWritten);
                  int written2 = cat.writeBytes(pictureBytes, 0, totalBytesWritten);
                  cat.close();
                  f.close();
                  if (written1 < 0 || written2 < 0)
                     status.setText("Unable to write the image");
                  else
                     status.setText("Saved " + f.getPath() + " ("+totalBytesWritten + " bytes)");
               }
               catch (ImageException e1)
               {
                  Vm.alert(e1.getMessage());
               }
            }
            catch (totalcross.io.IOException e)
            {
               status.setText("Unable to create PDBFile");
               MessageBox.showException(e, false);
            }
         }
         else
         if (event.target == load)
         {
            try
            {
               try
               {
//                  File f = new File("totalcross/painter/image.png", File.READ_WRITE);
                  PDBFile cat = new PDBFile(catalogName, PDBFile.READ_WRITE);
                  cat.setRecordPos(0);
                  // get size of record
                  int size = cat.getRecordSize();
//                  int size = f.getSize();
                  // get a new byte array being as long as the current record
                  byte test[] = new byte[size];
                  // read the full image
                  int read = cat.readBytes(test, 0, size);
//                  int read = f.readBytes(test, 0, size);
                  cat.close();
//                  f.close();
                  if (read != size)
                     status.setText("Not fully read!");
                  else
                  {
                     // create a new image with the one read
                     Image img = new Image(test);
                     if (img.getWidth() > 0) // successfully loaded?
                     {
                        // and assign it to the paint control
                        paint.setImage(img);
                        status.setText(img.getWidth() > paint.getWidth() || img.getHeight() > paint.getHeight() ? "Loaded. Click and drag picture"
                              : "Loaded.");
                     }
                     else
                        status.setText("Unable to load image!");
                  }
               }
               catch (totalcross.io.FileNotFoundException e)
               {
                  status.setText("You must first save the image.");
                  return;
               }
            }
            catch (Exception e)
            {
               status.setText(e.getMessage());
               MessageBox.showException(e, false);
            }
         }
      }
   }
}
