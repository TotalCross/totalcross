/*********************************************************************************
 *  TotalCross Software Development Kit - Litebase                               *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/



package tc.samples.api.phone;

import tc.samples.api.*;

import totalcross.io.*;
import totalcross.phone.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;

/** Sample program that shows how to send and receive SMS messages. */

public class PhoneSmsSample extends BaseContainer
{
   Edit edNumber;
   MultiEdit edMsg;
   Button btSend;
   Button btReceive;

   public void initUI()
   {
      super.initUI();
      if (!Settings.platform.equals(Settings.WINDOWSPHONE) && !Settings.onJavaSE)
      {
         add(new Label("This sample works only on Windows Phone"),CENTER,CENTER);
         return;
      }
      add(new Label("Number: "),LEFT+2,TOP+2);
      add(edNumber = new Edit(), AFTER, TOP);
      edNumber.setKeyboard(Edit.KBD_NUMERIC);
      add(btSend = new Button("Send"), LEFT + 3, BOTTOM);
      //add(btReceive = new Button("Receive"), RIGHT - 3, BOTTOM);
      add(edMsg = new MultiEdit(), LEFT, AFTER, FILL, FIT, edNumber);
   }

   public void onEvent(Event event)
   {
      if (event.type == ControlEvent.PRESSED)
      {
         try
         {
            if (event.target == btSend)
            {
               SMS.send(edNumber.getText(), edMsg.getText());
               new MessageBox("Status","SMS message sent.").popup();
            }
/*            else if (event.target == btReceive)
            {
               edMsg.setText("Program is blocked until a message is received.");
               edMsg.repaintNow();
               String[] answer = SMS.receive();
               edNumber.setText(answer[0]);
               edMsg.setText(answer[1]);
            }
*/         }
         catch (IOException e)
         {
            MessageBox.showException(e, false);
         }
      }
   }
}
