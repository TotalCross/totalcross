/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
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



package tc.samples.lang.thread.socket;

import totalcross.io.*;
import totalcross.net.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.gfx.*;

public class HTTPContainer extends Container implements Runnable
{
   MultiEdit me;
   Label label;

   public void initUI()
   {
      setBackColor(Color.WHITE);
      setBorderStyle(BORDER_RAISED);

      add(label = new Label(), LEFT,TOP,FILL,PREFERRED);
      add(me = new MultiEdit(0,0), LEFT,AFTER,FILL,FILL);
      me.setEditable(false);
      me.hasCursorWhenNotEditable = false;

      Thread t = new Thread(this);
      t.setPriority(3); // priority must be set BEFORE the thread is started! - palm os never puts working threads with priority 1 if there's another one with priority 5
      t.start();
   }

   public void run()
   {
      Socket socket = null;
      while (true)
      {
         try
         {
            me.setText("Content is Loading...");
            if (ThreadedSocket.paused || ThreadedSocket.paused0) me.repaintNow();
            socket = new Socket("www.google.com",80,25000);
            socket.readTimeout = 5000;
            String requestString = "GET /index.html HTTP/1.0\n\n";
            byte[] get = requestString.getBytes();
            socket.writeBytes(get);

            int totalCount = 0;
            int count = 0;

            byte[] buff = new byte[100];
            boolean done = false;
            StringBuffer responseBuffer = new StringBuffer(64);
            while (!done)
            {
                count = socket.readBytes(buff,0,buff.length);
                String text = new String(buff, 0, count);
                responseBuffer.append(text);
                totalCount += count;
                label.setText("Read " + totalCount + " from www.google.com");
                if (ThreadedSocket.paused || ThreadedSocket.paused0) label.repaintNow();
                if (count < buff.length)
                    done = true;
                Vm.sleep(100);
            }
            socket.close();

            String googleHTML = responseBuffer.toString();
            int index = googleHTML.indexOf('<');
            if (index >= 0)
                googleHTML = googleHTML.substring(index);

            me.setText(googleHTML);
            if (ThreadedSocket.paused || ThreadedSocket.paused0) me.repaintNow();
         }
         catch (IOException ioE)
         {
            me.setText("IOException - " + ioE.getMessage());
            me.repaintNow();
         }
         Vm.sleep(5000);
      }
   }
}
