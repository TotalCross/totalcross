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

import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;
import totalcross.net.*;
import totalcross.sys.*;

public class ThreadedSocket extends MainWindow implements Runnable
{
   Button directionButton;
   Button pauseButton,unpauseButton;
   static boolean paused,paused0;
   Container[] containers;
   int direction = Settings.platform.equals(Settings.JAVA) || Settings.platform.equals(Settings.WIN32) ? 1 : 4;  //      moving right
   Thread slideThread;
   boolean running,finished;
   Label lmem;

   public ThreadedSocket()
   {
      setTitle("Threaded Socket");
   }

   public void initUI()
   {
      Label l;
      Vm.tweak(Vm.TWEAK_DUMP_MEM_STATS,true);
      if (Settings.platform.equals(Settings.PALMOS)) // palm require netlib to be loaded in the main execution line
      {
         add(l = new Label("WAIT, OPENING SOCKET\nBEFORE STARTING THREADS.\nSEE THE DIALOG BELOW"),CENTER,TOP);
         repaintNow();
         try
         {
            new Socket("www.google.com",80,10000).close();
         }
         catch (Exception e)
         {
            new MessageBox("Error","Error when connecting. Aborting the sample... ("+e+")").popup();
            exit(1);
            return;
         }
         remove(l);
         Vm.setAutoOff(false); // palm halts if using sockets in a thread and the device turns off
      }

      directionButton = new Button("Switch Direction");
      containers = new Container[3];
      containers[0] = new TypingContainer(true);
      containers[1] = new HTTPContainer();
      containers[2] = new TypingContainer(false);
      add(directionButton,LEFT,TOP);
      add(pauseButton = new Button("Pause"),RIGHT,TOP);
      add(unpauseButton = new Button("Unpause"),RIGHT,TOP);
      unpauseButton.setVisible(false);
      add(lmem = new Label("",CENTER),AFTER,SAME,FIT,PREFERRED,directionButton);

      for (int i = 0, x=0; i < containers.length; i++, x += width)
         add(containers[i], x,i==0 ? AFTER : SAME,width,FILL);

      slideThread = new Thread(this);
      slideThread.start();
   }

   public void pause()
   {
      paused = !paused;
      pauseButton.setVisible(!paused);
      unpauseButton.setVisible(paused);
   }

   public void onEvent(Event event)
   {
      if (event.type == ControlEvent.PRESSED)
      {
         if (event.target == directionButton)
         {
            running = false;
            while (!finished)
               Vm.sleep(1);
            direction = -direction;
            slideThread = new Thread(this);
            slideThread.start();
         }
         else
         if (event.target == pauseButton || event.target == unpauseButton)
             pause();
      }
      super.onEvent(event);
   }

   public void run()
   {
      running = true;
      finished = false;
      while (running)
      {
         Rect r = containers[0].getRect();

         Container temp;
         if (direction > 0)
         {
            temp = containers[2];
            containers[2] = containers[1];
            r.x = 0;
            containers[2].setRect(r);

            containers[1] = containers[0];
            r.x -= width;
            containers[1].setRect(r);

            containers[0] = temp;
            r.x -= width;
            containers[0].setRect(r);
         }
         else
         {
            temp = containers[0];
            containers[0] = containers[1];
            r.x = 0;
            containers[0].setRect(r);

            containers[1] = containers[2];
            r.x += width;
            containers[1].setRect(r);

            containers[2] = temp;
            r.x += width;
            containers[2].setRect(r);
         }
         while (running && containers[1].getRect().x != 0)
         {
            if (!paused)
            {
               for (int i = 0; i < containers.length; i++)
               {
                  r = containers[i].getRect();
                  r.x += direction;
                  containers[i].setRect(r);
               }
               lmem.setText(Convert.toString(Vm.getFreeMemory()));
               try {repaintNow();} catch (Throwable t) {t.printStackTrace();}
            }
            Vm.sleep(5); // without this, events are blocked in Palm OS.
         }
         if (running)
         {
            paused0 = true;
            Vm.sleep(1000);
            paused0 = false;
         }
      }
      finished = true;
   }
}