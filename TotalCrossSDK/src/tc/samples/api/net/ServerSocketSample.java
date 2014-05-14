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
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;

public class ServerSocketSample extends BaseContainer implements Runnable
{
   private Button btnStart;
   private Button btnStop;
   private ListBox lb;
   private Edit edPort;
   private ServerSocket serverSocket;
   private int port;
   private Socket clientSocket;
   private Thread acceptThread;

   private boolean threadIsRunning;

   public void initUI()
   {
      super.initUI();
      String ip = null;
      try
      {
         ip = ConnectionManager.getLocalHost();
      }
      catch (IOException ex)
      {
         MessageBox.showException(ex, true);
      }
      
      add(new Label("IP: " + ip), LEFT + 2, AFTER + 3);
      add(edPort = new Edit("12345"), RIGHT - 2, SAME);
      edPort.setValidChars(Edit.numbersSet);
      edPort.setText("7070");
      add(new Label("Port: "), BEFORE - 2, SAME);
      Label l;
      add(l=new Label("Write this ip and port in your browser"), LEFT, AFTER);

      add(btnStart = new Button("Start"), LEFT + 2, BOTTOM - 2);
      add(btnStop = new Button("Stop"), RIGHT - 2, BOTTOM - 2);

      lb = new ListBox();
      lb.enableHorizontalScroll();
      add(lb, LEFT, AFTER + 3, FILL, FIT, l);

      toggleUI(true);
   }

   public void onEvent(Event e)
   {
      if (e.type == ControlEvent.PRESSED)
      {
         if (e.target == btnStart && validatePort())
         {
            acceptThread = new Thread(this);
            acceptThread.start();
         }
         else if (e.target == btnStop)
            stopServer();
      }
   }

   String answer = "<HTML><HEAD><TITLE>TotalCross</TITLE></HEAD><BODY>Connected!</BODY></HTML>";

   private void startServer() throws IOException
   {
      toggleUI(false);
      status("Starting...");

      serverSocket = new ServerSocket(port, 10000);

      status("Server started");
      status("Waiting for connections");

      do
      {
         clientSocket = serverSocket.accept();
         status("Still waiting...");
      }
      while (clientSocket == null);

      status("Accepted new connection");
      clientSocket.readTimeout = 2000;

      status("========================");
      String s;
      while ((s = clientSocket.readLine()) != null && (s = s.trim()).length() > 0)
         status(s);
      status("========================");
      clientSocket.writeBytes(answer);
      clientSocket.close();
      clientSocket = null; // flsobral@tc120: must set to null, otherwise the method stopServer will try to close it again.
   }

   private void stopServer()
   {
      if (!threadIsRunning)
         return;

      toggleUI(true);

      try
      {
         if (clientSocket != null)
         {
            clientSocket.close();
            clientSocket = null;
            status("Closed connection");
         }
         if (serverSocket != null)
         {
            status("Stopping the server...");
            serverSocket.close();
            serverSocket = null;
            status("Server closed.");
         }
         repaintNow();
      }
      catch (IOException e)
      {
         status("EXCEPTION CAUGHT AT STOP SERVER");
         MessageBox.showException(e, true);
      }
   }

   public void run()
   {
      try
      {
         startServer();
      }
      catch (IOException e)
      {
         // ignore exceptions thrown after the server was stopped
         if (threadIsRunning)
         {
            status("EXCEPTION CAUGHT AT START SERVER");
            MessageBox.showException(e, true);
         }
      }
      stopServer();
   }

   public void onExit()
   {
      // remember to stop the server on exit.
      stopServer();
   }

   /**
    * Updates the UI according to the application state.
    * 
    * @param enabled
    */
   private void toggleUI(boolean enabled)
   {
      threadIsRunning = !enabled;
      edPort.setEnabled(enabled);
      btnStart.setEnabled(enabled);
      btnStop.setEnabled(!enabled);
      repaintNow();
   }

   /**
    * Checks the given port number is a valid number.
    * 
    * @return
    */
   private boolean validatePort()
   {
      if (edPort.getLength() > 0)
      {
         try
         {
            port = Convert.toInt(edPort.getText());
            return true;
         }
         catch (InvalidNumberException e)
         {
            new MessageBox("Error", "Invalid port value.").popup();
         }
      }
      return false;
   }

   /**
    * Auxiliary function to manipulate the list box.
    * 
    * @param s
    */
   private void status(String s)
   {
      lb.add(s);
      lb.selectLast();
   }
}
