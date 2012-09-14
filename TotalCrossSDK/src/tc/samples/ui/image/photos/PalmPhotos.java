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



package tc.samples.ui.image.photos;

import totalcross.ui.ImageControl;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.image.*;
import totalcross.util.*;
import totalcross.io.*;

/** This program shows the photos that are stored in the main
  * memory of a zire71 (or any other Palm OS with builtin camera).
  */

public class PalmPhotos extends MainWindow
{
   static
   {
      Settings.useNewFont = true;
   }

   private ComboBox cbPhotos;
   private ImageControl display;

   public void initUI()
   {
      if (!Settings.platform.equals(Settings.PALMOS) && !Settings.platform.equals(Settings.JAVA))
      {
         repaintNow();
         new MessageBox("Attention","This program runs only\non Palm OS devices.").popup();
         exit(0);
         return;
      }
      // find photo files
      Vector vProgs = new Vector();
      String []pdbs = PDBFile.listPDBs(Convert.chars2int("Foto"),0);
      if (pdbs != null)
      {
         int n = pdbs.length;
         for (int i = 0; i < n; i++)
            if (pdbs[i] != null) // not a system catalog?
            {
               String ext = pdbs[i].substring(pdbs[i].indexOf('.'));
               boolean isPhoto = ext.startsWith(".Foto") || ext.startsWith(".jpg"); // win32 and applet shows the crid only
               if (isPhoto)
                  vProgs.addElement(pdbs[i]);//.substring(0,catalogs[i].indexOf('.')));
            }
         vProgs.qsort();
      }
      if (vProgs.size() == 0)
         add(new Label("No photo available"),CENTER,TOP);
      else
      {
         add(new Label("Photo: "),LEFT,TOP+2);
         cbPhotos = new ComboBox(vProgs.toObjectArray());
         add(cbPhotos,AFTER,SAME-2);
      }

      add(display = new ImageControl());
      display.setRect(LEFT,AFTER+2,FILL,FILL);
   }

   public void onEvent(Event event)
   {
      switch (event.type)
      {
         case ControlEvent.PRESSED:
            if (event.target == cbPhotos)
               showImage((String)cbPhotos.getSelectedItem());
            break;
      }
   }

   public void showImage(String name)
   {
      Image img=null;
      try
      {
         // Open the catalog
         PDBFile cat = new PDBFile(name, PDBFile.READ_WRITE);
         int ini = Vm.getTimeStamp();
         img = new Image(new RecordPipe(cat, 0, cat.getRecordCount()-1, 8));
         int elapsed = Vm.getTimeStamp()-ini;
         if (img.getWidth() > 0)
         {
            display.setImage(img);
            repaint();
         }
         else
            throw new Exception("Unable to read image!");
         Vm.debug("Loaded in "+elapsed+" ms");
      }
      catch (Exception e)
      {
         MessageBox.showException(e,false);
      }
   }
}
