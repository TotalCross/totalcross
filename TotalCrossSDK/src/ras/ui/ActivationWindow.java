/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

package ras.ui;

import ras.*;
import totalcross.io.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.xml.soap.*;

public class ActivationWindow extends MainWindow
{
   private ActivationClient client;

   public ActivationWindow()
   {
      this.client = ActivationClient.getInstance();
   }

   public ActivationWindow(ActivationClient client)
   {
      super("Activation", TAB_ONLY_BORDER);
      setUIStyle(Settings.Android);

      this.client = client;
   }

   public void initUI()
   {
      ActivationHtml html = ActivationHtml.getInstance();
      if (html != null)
         html.popup();

      String text = "The TotalCross virtual machine needs to be activated. This process requires your device's internet connection to be properly set up.";
      Label l;
      add(l = new Label(Convert.insertLineBreak(Settings.screenWidth - 10, fm, text)));
      l.align = FILL;
      l.setRect(LEFT + 5, TOP + 2, FILL - 10, PREFERRED);
      repaintNow();

      try
      {
         client.activate();
         MessageBox mb = new MessageBox("Success", "TotalCross is now activated!\nPlease restart your application.");
         mb.setBackColor(0x00AA00);
         mb.yPosition = BOTTOM;
         mb.popup();
         exit(0);
      }
      catch (ActivationException ex)
      {
         Throwable cause = ex.getCause();
         String s = ex.getMessage() + " The activation process cannot continue. The application will be terminated.";

         if (cause instanceof SOAPException || cause instanceof IOException)
            s += " Try again 2 or 3 times if there's really an internet connection.";

         s = s.replace('\n', ' '); // guich@tc115_13
         MessageBox mb = new MessageBox("Failure", s, new String[] { "Exit" });
         mb.setTextAlignment(LEFT);
         mb.yPosition = BOTTOM;
         mb.popup();
         exit(1);
      }
   }
}
