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



package tc.samples.io.device.zxing;

import totalcross.io.device.scanner.*;
import totalcross.res.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.event.*;
import totalcross.ui.font.*;
import totalcross.ui.gfx.*;

/** ZXing scanner demo
 */

public class ZXingScanner extends MainWindow implements KeyListener
{
   public static final int BKGCOLOR = 0x0A246A;
   public static final int SELCOLOR = 0x829CE2; // Color.brighter(BKGCOLOR,120);
   static
   {
      Settings.useNewFont = true;
      Settings.uiAdjustmentsBasedOnFontHeight = true;
   }

   // by making the members private, the compiler can optimize them.
   private Button btScanner;
   private MultiEdit edtBarCode;
   private Bar headerBar;

   public ZXingScanner()
   {
      super("ZXing Scanner Demo",NO_BORDER);
      setTitle("");
   }
   
   public void initUI()
   {
      setUIStyle(Settings.Android);
      setBackColor(UIColors.controlsBack = Color.WHITE);
      UIColors.messageboxBack = Color.brighter(BKGCOLOR,64);
      UIColors.messageboxFore = Color.WHITE;

      int c1 = 0x0A246A;
      Font f = font.adjustedBy(2,true);
      headerBar = new Bar("ZXing Scanner Demo");
      headerBar.setFont(f);
      headerBar.setBackForeColors(c1,Color.WHITE);
      headerBar.addButton(Resources.exit);
      add(headerBar, LEFT,0,FILL,PREFERRED);
      if (!Settings.onJavaSE && !Settings.platform.equals(Settings.ANDROID))
         add(new Label("This program currently runs\nonly at the Android platform."),CENTER,CENTER);
      else
      {
         add(btScanner = new Button("Scan"), CENTER, BOTTOM - 100,SCREENSIZE+80,PREFERRED+50);
         add(edtBarCode = new MultiEdit(3,1), LEFT, CENTER,FILL,PREFERRED);
         add(new Label("Result:"),LEFT,BEFORE);
         edtBarCode.setEditable(false);
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

   public void onEvent(Event event)
   {
      switch (event.type)
      {
         case ControlEvent.PRESSED:
            if (event.target == headerBar && headerBar.getSelectedIndex() == 1)
                  exit(0);
            else
            if (event.target == btScanner)
            {
               String scan = Scanner.readBarcode("");
               if (scan != null && scan.startsWith("***"))
                  scan = Scanner.readBarcode("*"); // use a try-harder algorithm
               if (scan != null)
                  edtBarCode.setText(scan);
            }
            break;
      }
   }
}
