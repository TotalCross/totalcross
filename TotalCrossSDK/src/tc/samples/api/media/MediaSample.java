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

package tc.samples.api.media;

import tc.samples.api.*;

import totalcross.io.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.media.*;

public class MediaSample extends BaseContainer
{
   public void initUI()
   {
      try
      {
         super.initUI();
         if (!Settings.onJavaSE && !Settings.platform.equals(Settings.ANDROID) && !Settings.isIOS() && !Settings.platform.equals(Settings.WINDOWSPHONE))
         {
            add(new Label("This sample runs only on the\nAndroid, iOS and Windows Phone platforms",CENTER),CENTER,CENTER);
            return;
         }
         // you may write the file only once; here we write always because it is pretty small, just 29k
         new File("device/sample.mp3", File.CREATE_EMPTY).writeAndClose(Vm.getFile("tc/samples/api/sample.mp3"));
         
         final Button b = new Button("Play MP3 sample");
         add(b, CENTER,CENTER);
         b.addPressListener(new PressListener() 
         {
            public void controlPressed(ControlEvent e)
            {
               try
               {
                  Sound.play("device/sample.mp3");
               }
               catch (Exception ee)
               {
                  MessageBox.showException(ee,true);
               }
            }
         });
      }
      catch (Exception e)
      {
         MessageBox.showException(e,true);
      }
   }
}
