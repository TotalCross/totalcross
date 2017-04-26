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

import totalcross.net.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.event.*;

public class ServerSocketSample extends BaseContainer implements Runnable
{
   private Button btnStart;
   private Button btnStop;
   private Edit edPort;
   private ServerSocket serverSocket;
   private int port;
   private Socket clientSocket;

   private boolean threadIsRunning;

   public void initUI()
   {
      super.initUI();
      String ip;
      try
      {
         ip = ConnectionManager.getLocalHost();
      }
      catch (Exception ex)
      {
         add(new Label("Unable to get local host ip."),CENTER,CENTER);
         return;
      }
      
      add(new Label("IP: " + ip), LEFT + 2, TOP + 3);
      add(edPort = new Edit("12345"), RIGHT - 2, SAME);
      edPort.setValidChars(Edit.numbersSet);
      edPort.setText("7070");
      add(new Label("Port: "), BEFORE - 2, SAME);
      Label l;
      add(l=new Label("Write this ip and port in your browser"), LEFT, AFTER);

      Button.commonGap = fmH/4;
      add(btnStart = new Button("Start"), LEFT + 2, BOTTOM - 2);
      add(btnStop = new Button("Stop"), RIGHT - 2, BOTTOM - 2);
      Button.commonGap = 0;

      addLog(LEFT, AFTER + 3, FILL, FIT, l);

      toggleUI(true);
   }

   boolean stopThread;
   public void onEvent(Event e)
   {
      if (e.type == ControlEvent.PRESSED)
      {
         if (e.target == btnStart && validatePort())
            new Thread(this).start();
         else if (e.target == btnStop)
         {
            log("Wait until accept finishes...");
            stopThread = true;
         }
      }
   }

   String answer = "<HTML><HEAD><TITLE>TotalCross</TITLE></HEAD><BODY>Connected!</BODY></HTML>";

   private void startServer() throws Exception
   {
      stopThread = false;
      threadIsRunning = true;
      toggleUI(false);
      log("Starting...");

      serverSocket = new ServerSocket(port, 10000);

      log("Server started. Waiting for connections");

      do
      {
         clientSocket = serverSocket.accept();
         log("Still waiting...");
      }
      while (!stopThread && clientSocket == null);

      if (stopThread)
         return;
      
      log("Accepted new connection");
      clientSocket.readTimeout = 20000;

      log("========================");
      String s;
      while ((s = clientSocket.readLine()) != null && (s = s.trim()).length() > 0)
         log(s,false);
      log("========================");
      clientSocket.writeBytes(answer);
      clientSocket.close();
      clientSocket = null; // flsobral@tc120: must set to null, otherwise the method stopServer will try to close it again.
   }

   private void stopServer()
   {
      if (!threadIsRunning)
         return;
      threadIsRunning = false;

      toggleUI(true);

      try
      {
         if (clientSocket != null)
         {
            clientSocket.close();
            clientSocket = null;
            log("Closed connection");
         }
         if (serverSocket != null)
         {
            log("Stopping the server...");
            serverSocket.close();
            serverSocket = null;
            log("Server closed.");
         }
         repaintNow();
      }
      catch (Exception e)
      {
         log("EXCEPTION CAUGHT AT STOP SERVER");
      }
   }

   public void run()
   {
      try
      {
         startServer();
      }
      catch (Exception e)
      {
         // ignore exceptions thrown after the server was stopped
         if (threadIsRunning)
         {
            log("EXCEPTION CAUGHT AT SERVER START");
            log(e.getClass()+": "+e.getMessage());
         }
      }
      stopServer();
   }

   public void onRemove()
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
            log("Invalid port value.");
         }
      }
      return false;
   }
}
