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



package tc.samples.api.io.device;

import tc.samples.api.*;

import totalcross.io.device.gps.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;

public class GpsSample extends BaseContainer
{
   private static final int SECONDS = 30;
   
   static class GpsThread implements Runnable 
   {
      public static boolean running;
      public static Thread thread;
      public static boolean gpsDesligado;
      private boolean stopThread;
      public static boolean colectingGps;

      public void start() 
      {
         stopThread = false;
         if (!running) 
         {
            running = true;
            if (thread == null)
               thread = new Thread(this);
            thread.start();
         }
      }

      public void stop() 
      {
         stopThread = true;
      }

      public void run() 
      {
         try 
         {
            while (true)
            {
               if (stopThread) 
               {
                  stopThread = false;
                  return;
               }
               try 
               {
                  if (!colectingGps) 
                  {
                     colectingGps = true;
                     int ini = Vm.getTimeStamp();
                     try 
                     {
                        GPS gps = new GPS();
                        int endTime = Vm.getTimeStamp() + Math.min(3*60*1000,SECONDS*1000*2/3); // try for some seconds, but a max of 3 minutes
                        do
                        {
                           if (gps.retrieveGPSData())
                           {
                              log(gps.lastFix+": "+gps.getLatitude()+", "+ gps.getLongitude()+". sat: "+gps.satellites+", err dist: "+gps.pdop);
                              break;
                           }
                           Vm.sleep(50);
                        }
                        while (Vm.getTimeStamp() < endTime);
                        gps.stop();
                     } 
                     catch (Exception e) 
                     {
                     }
                     int end = Vm.getTimeStamp();
                     
                     Vm.sleep(SECONDS*1000 - (end-ini)); // dont consider the time took to get the coords again
                  }
               } 
               finally 
               {
                  colectingGps = false;
               }
            }
         } 
         catch (Exception e) 
         {
            log(new Time()+" ERROR: "+e.getMessage());
         } 
         finally 
         {
            running = false;
         }
      }

   }
   
   public static GpsThread gpsThread;

   MenuBar mbar;
   Button btnGps;

   public void initUI() 
   {
      super.initUI();
      try 
      {
         add(btnGps = new Button("  Start GPS Logger  "), CENTER, TOP + 10);
         btnGps.setBackColor(Color.getRGB(188, 238, 104));
         addLog(LEFT, AFTER+10, FILL, FILL,null);
      } 
      catch (Exception e) 
      {
         MessageBox.showException(e, false);
      }
   }

   public void onEvent(final Event e) 
   {
      try 
      {
         switch (e.type) 
         {
            case ControlEvent.PRESSED:
               if (e.target == btnGps) 
               {
                  log("Gps started with intervals of "+SECONDS+" seconds");
                  if (gpsThread == null) 
                     gpsThread = new GpsThread();
                  gpsThread.start();
               } 
               break;
            }
      } 
      catch (Exception ex) 
      {
         MessageBox.showException(ex,true);
      }
   }
   
   public void onRemove()
   {
      if (gpsThread != null)
         gpsThread.stop();
   }
}
