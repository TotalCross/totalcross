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



package tc.samples.api.net;

import tc.samples.api.*;

import totalcross.io.*;
import totalcross.net.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.event.*;
import totalcross.util.*;

public class SocketSample extends BaseContainer
{
   Button btnOpen;
   Edit edA,edP;
   Socket socket;
   Vector networks;
   
   public void initUI()
   {
      super.initUI();
      add(new Label("Address: "),LEFT, TOP+1);
      add(edA = new Edit(""), AFTER+3, SAME);
      edA.setText("www.superwaba.com.br");
      add(new Label("Port: "), LEFT, AFTER+3);
      add(edP = new Edit("8080"), AFTER+3, SAME);
      edP.setText("80");
      
      add(btnOpen = new Button(" Open connection "), CENTER, AFTER+3,PREFERRED,PREFERRED+fmH/4);
      
      addLog(LEFT,AFTER+3,FILL,FILL,null);
   }

   public void onEvent(Event e)
   {
      if (e.type == ControlEvent.PRESSED)
      {
         if (e.target == btnOpen)
            try
            {
               openSocket();
            }
            catch (IOException e1)
            {
               log("openSocket failed!");
               log(e1.getMessage());
               e1.printStackTrace();
            }
      }
   }

   private void openSocket() throws totalcross.net.UnknownHostException,  totalcross.io.IOException
   {
      repaintNow(); // release the button
      log("opening connection...");
      int port = 80;
      if (edP.getLength() > 0)
         try {port = Convert.toInt(edP.getText());} catch (InvalidNumberException ine) {}

      socket = new Socket(edA.getText(), port, 25000);
      socket.readTimeout = 30000;

      log("Socket opened");
      log("Sending HttpGet");
      byte []bytes = "GET / HTTP/1.0\n\n".getBytes();
      socket.writeBytes(bytes);
      log("===== RESPONSE =====");
      
      // some sites can take a longer delay to start sending things,
      // so we loop until we find something to read
      LineReader lr = new LineReader(socket); // note: using socket.readLine is VERY slow.
      String line;
      try
      {
         while ((line = lr.readLine()) != null)
            log(line);
      }
      catch (SocketTimeoutException ex)
      {
         log("Read: Timeout!");
      }
      log("===================");
      log("Closing socket");
      socket.close();
      log("Socket closed");
   }
}