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



package tc.samples.ui.image.test;

import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.image.*;
import totalcross.net.*;

public class ImageTest extends MainWindow
{
   static
   {
      Settings.useNewFont = true;
   }

   private Label lbStatus;
   private ComboBox cbChoices;
   private ImageControl ic;
   String[] imageTitles =
   {
      "church jpeg",
      "pngnow png",
      "rgb gif",
      "rgb jpeg",
      "stop png",
      "tclogo gif",
      "barbara jpg (http)"
   };
   String[] imageNames =
   {
      "church.jpeg",
      "pngnow.png",
      "rgb.gif",
      "rgb.jpeg",
      "stop.png",
      "tclogo.gif",
      "http://www.superwaba.com.br/etc/barbara.jpg"
   };

   public void initUI()
   {
      add(new Label("Select one:"),LEFT+2,TOP+2);
      add(cbChoices = new ComboBox(imageTitles),AFTER+5,SAME-2);
      add(lbStatus = new Label("", CENTER),LEFT,BOTTOM);
      lbStatus.setText((Settings.screenBPP > 16 ? "True Color" : Settings.screenBPP == 16 ?"High Color" : "color") + (!Settings.onJavaSE?" device":" desktop emul."));
      // place button between combo and label
      add(ic = new ImageControl(), LEFT,AFTER+3,FILL,FIT,cbChoices);
      ic.centerImage = true;
      ic.setEventsEnabled(true);
      //ic.borderColor = Color.GREEN;
   }

   public void onEvent(Event event)
   {
      switch (event.type)
      {
         case ControlEvent.PRESSED:
            if (event.target == cbChoices)
               showImage(cbChoices.getSelectedIndex());
            break;
      }
   }

   public void showImage(int imageNo)
   {
      Image img=null;
      if (imageNo == -1) return;
      try
      {
         lbStatus.setText("Loading...");
         lbStatus.repaintNow();
         int ini = Vm.getTimeStamp();
         if (imageTitles[imageNo].indexOf("(http)") > 0)
            img = new Image(new HttpStream(new URI(imageNames[imageNo])));
         else
            img = new Image(imageNames[imageNo]);
         lbStatus.setText("Image "+img.getWidth()+"x"+img.getHeight()+" loaded in "+(Vm.getTimeStamp()-ini)+"ms");
         ic.setImage(img);
      }
      catch (Exception e)
      {
         if (e.getMessage() != null)
            lbStatus.setText(e.getMessage());
         else
            MessageBox.showException(e,false);
         ic.setImage(null);
      }
      ic.setRect(ic.getRect());
      repaint();
   }
}
