/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2001 Andrew Chitty                                             *
 *  Copyright (C) 2001-2012 SuperWaba Ltda.                                      *
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

import totalcross.io.device.scanner.*;
import totalcross.res.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;

/** ZXing scanner demo
 */

public class ScannerZXing extends MainWindow implements KeyListener
{
   // by making the members private, the compiler can optimize them.
   private MultiEdit edtBarCode;
   private Bar headerBar;
   private PushButtonGroup pbg;

   public ScannerZXing()
   {
      super("ZXing Scanner Demo",NO_BORDER);
      setTitle("");
   }
   
   public void initUI()
   {
      setUIStyle(Settings.Android);
      setDefaultFont(font.adjustedBy(2,true));
      setBackColor(UIColors.controlsBack = Color.WHITE);
      UIColors.messageboxBack = Color.brighter(0x4A64AA);
      UIColors.messageboxFore = Color.WHITE;

      headerBar = new Bar("ZXing Scanner Demo");
      headerBar.setBackForeColors(0x0A246A,Color.WHITE);
      headerBar.addButton(Resources.exit);
      add(headerBar, LEFT,0,FILL,PREFERRED);
      if (!Settings.onJavaSE && !Settings.platform.equals(Settings.ANDROID))
         add(new Label("This program currently runs\nonly on the Android platform."),CENTER,CENTER);
      else
      {
         pbg = new PushButtonGroup(new String[]{"SCAN 1D barcodes","SCAN 2D QR codes","SCAN Both types"},fmH/2,3);
         add(pbg,LEFT,AFTER+fmH,FILL,PREFERRED+fmH);

         add(edtBarCode = new MultiEdit(3,1), LEFT, BOTTOM-fmH,FILL,PREFERRED);
         edtBarCode.setEditable(false);
         
         add(new Label("Result:"),LEFT,BEFORE);
         
         
         getParentWindow().addKeyListener(this); // exit app when user press back 
      }
   }

   public void keyPressed(KeyEvent e) {}
   public void actionkeyPressed(KeyEvent e) {}
   public void specialkeyPressed(KeyEvent e)
   {
      if (e.key == SpecialKeys.ESCAPE)
      {
         e.consumed = true;
         exit(0);
      }
   }
   
   private static final String msg = "Place a barcode inside the viewfinder rectangle to scan it";

   public void onEvent(Event event)
   {
      switch (event.type)
      {
         case ControlEvent.PRESSED:
            if (event.target == headerBar && headerBar.getSelectedIndex() == 1)
                  exit(0);
            else
            if (event.target == pbg)
            {
               int sel = pbg.getSelectedIndex();
               String mode = sel == 0 ? "1D" : sel == 1 ? "2D" : "";
               String scan = Scanner.readBarcode("mode="+mode+"&msg="+msg);
               if (scan != null)
                  edtBarCode.setText(scan);
            }
            break;
      }
   }
}
