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

import totalcross.io.*;
import totalcross.net.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.util.*;

public class SocketSample extends MainWindow
{
   Button btnOpen, btnHang;
   ListBox lb;
   Edit edA,edP;
   ComboBox cboNetworks;
   Socket socket;
   Vector networks;
   
   public SocketSample()
   {
      super("Socket Test",TAB_ONLY_BORDER);
   }

   public void initUI()
   {
      super.initUI();
      boolean wifi = false, cell = false;
      
      try
      {
         wifi = ConnectionManager.isAvailable(ConnectionManager.WIFI);
      }
      catch (IOException e) {}
      
      try
      {
         cell = ConnectionManager.isAvailable(ConnectionManager.CELLULAR);
      }
      catch (IOException e) {}
      
      if (!wifi && !cell) // no connection available
      {
         new MessageBox("Warning", "No network connection available.\nSocketTest will exit now.").popup();
         exit(1);
      }
      
      networks = new Vector();
      if (wifi)
         networks.addElement("WI-FI");
      if (cell)
         networks.addElement("CELLULAR");
      if (networks.size() > 1) // more than one network
         networks.insertElementAt("ANY", 0);
      
      add(new Label("Address: "),LEFT, TOP+1);
      add(edA = new Edit(""), AFTER+3, SAME);
      edA.setText("www.superwaba.com.br");
      add(new Label("Port: "), LEFT, AFTER+3);
      add(edP = new Edit("8080"), AFTER+3, SAME);
      edP.setText("80");
      
      add(cboNetworks = new ComboBox(networks.toObjectArray()), AFTER+3, SAME);
      cboNetworks.setSelectedIndex(0);
      
      add(btnOpen = new Button("Open connection"), LEFT, AFTER+3);
      add(btnHang = new Button("Disconnect"), RIGHT, SAME);
      
      add(lb = new ListBox());
      lb.enableHorizontalScroll();
      lb.setRect(LEFT,AFTER+3,FILL,FILL);
   }

   private void status(String s)
   {
      if (s != null)
      {
         lb.add(s);
         lb.selectLast();
      }
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
               status("openSocket failed!");
               status(e1.getMessage());
               e1.printStackTrace();
            }
         else
         if (e.target == btnHang)
         {
            status("Disconnecting...");
            try
            {
               ConnectionManager.close();
            }
            catch (IOException e1)
            {
               status("disconnect failed!");
               status(e1.getMessage());
               e1.printStackTrace();
            }
            status("Disconnected.");
         }
      }
   }

   private void openSocket() throws totalcross.net.UnknownHostException,  totalcross.io.IOException
   {
      repaintNow(); // release the button
      status("opening connection...");
      int port = 80;
      if (edP.getLength() > 0)
         try {port = Convert.toInt(edP.getText());} catch (InvalidNumberException ine) {}

      String network = (String)cboNetworks.getSelectedItem();
      if (network.equals("ANY"))
         ConnectionManager.open();
      else if (network.equals("WI-FI"))
         ConnectionManager.open(ConnectionManager.WIFI);
      else if (network.equals("CELLULAR"))
         ConnectionManager.open(ConnectionManager.CELLULAR);
         
      socket = new Socket(edA.getText(), port, 25000);
      socket.readTimeout = 30000;

      status("Socket opened");
      status("Sending HttpGet");
      byte []bytes = "GET / HTTP/1.0\n\n".getBytes();
      socket.writeBytes(bytes);
      status("===== RESPONSE =====");
      
      // some sites can take a longer delay to start sending things,
      // so we loop until we find something to read
      LineReader lr = new LineReader(socket); // note: using socket.readLine is VERY slow.
      String line;
      try
      {
         while ((line = lr.readLine()) != null)
            status(line);
      }
      catch (SocketTimeoutException ex)
      {
         status("Read: Timeout!");
      }
      status("===================");
      status("Closing socket");
      socket.close();
      status("Socket closed");
   }
}