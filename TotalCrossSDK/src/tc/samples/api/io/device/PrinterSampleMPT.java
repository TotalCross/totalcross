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



package tc.samples.api.io.device;

import totalcross.io.*;
import totalcross.io.device.printer.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.gfx.*;

public class PrinterSampleMPT extends PrinterSampleBase
{
   protected void printSample(Stream s) throws Exception
   {
      MPTPrinter cp = null;
      try
      {
         cp = new MPTPrinter(s);
         // change the font and print something
         cp.setFont(false, false, false, false);
         cp.print("Bárbara Hazan\n\nDaddy loves you!\n");
         cp.newLine();

         // create a dynamic image...
         int k = 100;
         MonoImage img = new MonoImage(k,k);
         Graphics g = img.getGraphics();
         g.backColor = Color.WHITE;
         g.fillRect(0,0,k,k);
         g.foreColor = Color.BLACK;
         g.drawRect(0,0,k-1,k-1);
         g.drawLine(0,0,k,k);
         g.drawLine(k,0,0,k);

         // ...and print it
         cp.print(img);
         cp.newLine();

         // change the font to a big one
         cp.setFont(true, true, false, true);
         cp.print("*** Barbara ***\n");
         cp.setFont(false, false, false, false);

         // print a png file.
         try
         {
            cp.print(new MonoImage("tc/samples/api/io/device/barbara.png"));
            cp.newLine();
         }
         catch (OutOfMemoryError oome) {add(new Label("No memory to load image"),CENTER,AFTER);}

         cp.newLine(6);
      }
      catch (Exception e)
      {
         MessageBox.showException(e,true);
      }
   }
}
