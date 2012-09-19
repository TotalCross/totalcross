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



package tc.samples.io.device.irtest;

import totalcross.io.*;
import totalcross.io.device.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;

public class IRTest extends MainWindow
{
   private Button send, receive;
   private Edit dataEdit;
   private ListBox textArea;
   private PortConnector port;
   private byte receiveBuf[] = new byte[1024];

   public IRTest()
   {
      super("IRTest", RECT_BORDER);
   }

   //default constructor
   public void initUI()
   {
      //on load
      setBackColor(0xCDCDD2);
      //textArea is where the received messages will display
      Button.commonGap = 2;
      add(receive = new Button("Get"), RIGHT - 1, BOTTOM - 1);
      add(send = new Button("Send"), BEFORE - 1, SAME);
      add(dataEdit = new Edit());
      dataEdit.setRect(LEFT + 1, SAME, send.getRect().x - 4, PREFERRED);
      Button.commonGap = 0;

      textArea = new ListBox();
      add(textArea);
      textArea.setRect(LEFT + 1, TOP + 1, FILL - 1, FILL - dataEdit.getRect().height - 2);
      //data edit the edit where messages will be typed to send

      try
      {
         port = new PortConnector(PortConnector.IRCOMM, 9600);
      }
      catch (IllegalArgumentIOException e)
      {
         MessageBox.showException(e, true);
      }
      catch (IOException e)
      {
         MessageBox.showException(e, true);
      }
   }

   public void onEvent(Event event)
   {
      switch (event.type)
      {
         case ControlEvent.PRESSED:
            //if the button was send
            if (event.target == send)
            {
               // send button
               // if the IR port is open
               if (port != null)
               {
                  //if the message to send is not  blank
                  if (dataEdit.getLength() == 0) //the message was blank
                     new MessageBox("***Error***", "Must enter a message.").popupNonBlocking();
                  else
                  {
                     String messageToSend = dataEdit.getText();
                     byte[] messageBytes = messageToSend.getBytes();
                     try
                     {
                        port.setFlowControl(true);
                        port.readTimeout = 2000;
                        port.writeBytes(messageBytes, 0, messageBytes.length);
                     }
                     catch (IOException e)
                     {
                        MessageBox.showException(e, true);
                     }

                     textArea.add(">> " + messageToSend);
                     repaint();
                     dataEdit.setText("");
                  }
               }
            }//end of send event
            //if the button was receive
            else if (event.target == receive)
            {
               // receive button
               if (port != null)
               {
                  try
                  {
                     int count = port.readCheck();
                     int readCount = port.readBytes(receiveBuf, 0, 1024);
                     if (readCount > 0)
                     {
                        if (count != readCount)
                           textArea.add("Expected: " + count + " bytes. Received: " + readCount + " bytes.");
                        String receiveText = new String(receiveBuf);

                        textArea.add("<< " + receiveText);
                        repaint();
                     }
                  }
                  catch (IOException e)
                  {
                     MessageBox.showException(e, true);
                  }
               }
            }//end of receive event
            break;
      }//end of type switch
   }//end of onEvent

   public void onExit()
   {
      super.onExit();

      if (port != null)
      {
         try
         {
            port.close();
         }
         catch (IOException e)
         {
            MessageBox.showException(e, true);
         }
      }
   }
}
