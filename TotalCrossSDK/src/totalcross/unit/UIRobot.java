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



package totalcross.unit;

import totalcross.io.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;
import totalcross.util.*;
import totalcross.util.Comparable;
import totalcross.util.concurrent.*;

/** This class permits the control of an User Interface window, issuing events as if it
 * was the user.
 * <p>
 * To start the robot, you must do something like:
 * <pre>
 * new Thread()
 * {
 *    public void run()
 *    {
 *       UIRobot robot = new UIRobot();
 *       robot.delayBetweenEvents = Settings.onJavaSE ? 500 : 1000; // change it accordingly
 *       ... send the events
 *    }
 * }.start(); 
 * </pre>
 * Note that a UIRobot cannot be restarted, you must create another one.
 * <br><br>
 * To be able to record the events that will be used in the UIRobot, you can use a Java IDE
 * and define in your application:
 * <pre>
 * Settings.dumpUIRobotEvents = true;
 * Settings.showMousePosition = true; // not needed, but shows mouse position
 * </pre>
 * To start the event recorder, press CONTROL+1. Then, do the events; you may even 
 * type things in the keyboard and press ENTER that they will be recorded. The logs are dumped
 * to the console. To stop the logging, press CONTROL+1 again. Then, copy/paste the events
 * in the run method as described above. You have to call it by some way from your application.
 * Remember that the application must be in the same state as when you recorded the events, or the
 * ui robot will not behave correctly.
 * @author Guilherme C. Hazan
 */
public class UIRobot
{
   private static final String TITLE = "User Interface Robot";
   public static final int IDLE      = 0;
   public static final int RECORDING = 1;
   public static final int PLAYBACK  = 2;
   public static int status;
   
   private static MainWindow mw = MainWindow.getMainWindow();
   private int lastTS;
   private File flog;
   private DataStreamLE fds;
   private int counter;
   
   private static String[] recordedRobots;
   
   /** Constructs a new UIRobot 
    */
   public UIRobot() throws Exception
   {
      if (status != IDLE) // another robot already's running?
         throw new Exception("Already running");
      // get from user if he wants to record or playback
      switch (showMessage("Please select the action:",new String[]{"Record","Playback","Cancel"},0))
      {
         case 0: record();   break;
         case 1: playback(); break;
      }
   }
   
   private void record() throws Exception
   {
      InputBox ib = new InputBox(TITLE,"Please type the robot name:","",new String[]{"Start record","Cancel"});
      ib.transitionEffect = Container.TRANSITION_NONE;
      ib.setBackForeColors(Color.ORANGE, 1);
      ib.buttonKeys = new int[]{SpecialKeys.ENTER,SpecialKeys.ESCAPE};
      ib.popup();
      if (ib.getPressedButtonIndex() == 1)
         throw new Exception("Cancelled");
      String name = ib.getValue() + ".robot";
      flog = new File(Settings.appPath+"/"+name,File.CREATE_EMPTY,1);
      fds = new DataStreamLE(flog);
      lastTS = Vm.getTimeStamp();
      status = RECORDING; 
   }

   private void playback() throws Exception
   {
      if (recordedRobots == null)
         fillListOfRecordedRobots();
      if (recordedRobots == null)
      {
         showMessage("No robots found.",null,1500);
         throw new Exception("No robots");
      }
      MultiListBox lb = new MultiListBox(recordedRobots);
      lb.setOrderIsImportant(true);
      ControlBox cb = new ControlBox(TITLE,"Select the robots in the\nsequence you want to run.",lb,Control.FILL,Control.FIT,
            new String[]{"Play selected","Play all","Play random","Dump contents","Delete selected","Cancel"},2);
      cb.transitionEffect = Container.TRANSITION_NONE;
      cb.setBackForeColors(Color.ORANGE, 1);
      cb.popup();
      IntVector order = lb.getSelectedIndexes();
      int n = order.size();
      int sel = cb.getPressedButtonIndex();
      switch (sel)
      {
         case 0: // play selected
            play(order.items, n, false,1);
            break;
         case 1: // play all
            play(null, recordedRobots.length, false,1);
            break;
         case 2: // play random
         {
            // get the number of repetitions
            InputBox ib = new InputBox(TITLE,"Type the number of runs","1");
            ib.getEdit().setValidChars(Edit.numbersSet);
            ib.transitionEffect = Container.TRANSITION_NONE;
            ib.setBackForeColors(Color.ORANGE, 1);
            ib.popup();
            if (ib.getPressedButtonIndex() == 1) // cancelled?
               throw new Exception("Cancelled");
            String countStr = ib.getValue();
            int repeat;
            try 
            {
               repeat = Convert.toInt(countStr);
            }
            catch (InvalidNumberException ine)
            {
               showMessage("Invalid number, operation cancelled.",null,1500);
               throw new Exception("Cancelled");
            }
            // fills an array with all indexes and them swap them randomly
            int[] s = order.items;
            Random r = new Random();
            for (int i = n*3; --i >= 0;)
            {
               int idx1 = r.between(0,n-1);
               int idx2 = r.between(0,n-1);
               if (idx1 != idx2)
               {
                  int temp = s[idx1];
                  s[idx1] = s[idx2];
                  s[idx2] = temp;
               }
            }
            play(s, n, false, repeat);
            break;
         }
         case 3:
            play(order.items, n, true, 1);
            break;
         case 4:
            if (showMessage("Do you want to delete the selected robots?", new String[]{"No","Yes"},0) == 1)
            {
               for (int i = 0; i < n; i++)
                  try
                  {
                     String item = recordedRobots[order.items[i]];
                     String fileName = item.substring(0,item.indexOf(' '));
                     new File(Settings.appPath+"/"+fileName).delete();
                  }
                  catch (Exception ee)
                  {
                     ee.printStackTrace();
                  }
               fillListOfRecordedRobots();
            }
            break;
      }
   }
   
   private Vector threadPool = new Vector(10);
   private Lock tpLock;
   
   private PostThread popThread()
   {
      try
      {
         synchronized (tpLock)
         {
            return (PostThread)threadPool.pop();
         }
      }
      catch (ElementNotFoundException enfe)
      {
         PostThread t = new PostThread();
         t.start();
         return t;
      }
   }
   
   private void pushThread(PostThread t)
   {
      synchronized (tpLock)
      {
         threadPool.push(t);
      }
   }
   
   private class PostThread extends Thread
   {
      boolean running;
      int type, key,x,y,mods;
      Lock l;
      
      public PostThread()
      {
         l = new Lock();
      }
      
      public void set(int type, int key, int x, int y, int mods)
      {
         this.key = key;
         this.x = x;
         this.y = y;
         this.mods = mods;
         synchronized (l)
         {
            this.type = type;
         }
      }
      
      public void kill()
      {
         running = false;
      }
      
      public void run()
      {
         running = true;
         while (running)
         {
            int type;
            synchronized (l)
            {
               type = this.type;
            }
            if (type == 0)
               Vm.sleep(1);
            else
            {
               mw._postEvent(type, key, x, y, mods, 0);
               this.type = 0;
               pushThread(this);
            }
         }
      }
   }
   
   private void play(final int[] items, final int n, final boolean dump, final int repeat)
   {
      if (tpLock == null)
         tpLock = new Lock();
      new Thread()
      {
         public void run()
         {
            try
            {
               ListBox lb = null;
               ControlBox cb = null;
               if (!dump)
                  status = PLAYBACK;
               else
               {
                  lb = new ListBox();
                  cb = new ControlBox(TITLE,"Robot dump",lb,Control.FILL,Control.FIT, new String[]{"Ok"});
                  cb.transitionEffect = Container.TRANSITION_NONE;
                  cb.setBackForeColors(Color.ORANGE, 1);
               }
               for (int r = 1; r <= repeat && (dump || status == PLAYBACK); r++)
                  for (int i = 0; i < n && (dump || status == PLAYBACK); i++)
                  {
                     String item = recordedRobots[items == null ? i : items[i]];
                     String fileName = item.substring(0,item.indexOf(' '));
                     File f = new File(Settings.appPath+"/"+fileName,File.READ_WRITE);
                     DataStreamLE ds = new DataStreamLE(f);
                     String st = "Starting "+fileName;
                     if (repeat > 1)
                        st += " (run "+r+" of "+repeat+")";
                     Vm.debug(st);
                     if (!dump)
                        showMessage(st,null,1500);
                     else
                        lb.add(st);
                     for (int j = 0; dump || status == PLAYBACK; j++)
                     {
                        int type  = ds.readInt();
                        if (type == 0)
                           break;
                        int key   = ds.readInt();
                        int x     = ds.readInt();
                        int y     = ds.readInt();
                        int mods  = ds.readInt();
                        int delay = ds.readInt();
                        if (!dump && delay > 0)
                           Vm.sleep(delay);
                        if (dump || Settings.onJavaSE)
                        {
                           String s = dumpEvent(j,type,key,x,y,mods,delay);
                           if (!dump && Settings.onJavaSE)
                              Vm.debug(s);
                           lb.add(s);
                        }
                        if (!dump)
                        {
                           PostThread pt = popThread();
                           pt.set(type,key,x,y,mods);
                        }
                     }
                     f.close();
                     if (!dump)
                     {
                        Vm.sleep(1000);
                        showMessage("Finished "+fileName,null,1500);
                     }
                     else
                        lb.add("====================");
                  }
               if (dump)
               {
                  cb.popup();
               }
               else
               {
                  // kill all tasks in the thread pool
                  for (int i = threadPool.size(); --i >= 0;)
                     ((PostThread)threadPool.items[i]).kill();
                  threadPool.removeAllElements();
                  Vm.sleep(500); // give a time so all can get killed
                  status = IDLE;
               }
            }
            catch (Exception e)
            {
               e.printStackTrace();
            }
         }
      }.start();
   }
   
   private String dumpEvent(int j, int type, int key, int x, int y, int mods, int delay)
   {
      switch (type)
      {
         case PenEvent.PEN_DOWN:          return "PEN_DOWN  "+x+","+y+" @ "+delay+"ms";
         case PenEvent.PEN_UP:            return "PEN_UP    "+x+","+y+" @ "+delay+"ms";
         case PenEvent.PEN_DRAG:          return "PEN_DRAG  "+x+","+y+" @ "+delay+"ms";
         case KeyEvent.KEY_PRESS:         return "KEY_PRESS "+(key < 10 ? "  " : key < 100 ? " " : "")+key+" '"+(char)key+"'"+(mods == 0 ? " @ " : " - "+mods+" @ ")+delay+"ms";
         case KeyEvent.SPECIAL_KEY_PRESS: return "SPECIAL_KEY_PRESS "+key+(mods == 0 ? " @ " : " ("+mods+") @ ")+delay+"ms";
      }
      return "";
   }
   
   private int showMessage(String msg, String[] btns, int delay)
   {
      MessageBox mb = delay > 0 ? new MessageBox(TITLE,msg,null) : btns == null ? new MessageBox(TITLE, msg) : new MessageBox(TITLE,msg,btns);
      mb.transitionEffect = Container.TRANSITION_NONE;
      mb.setBackForeColors(Color.ORANGE, 1);
      if (delay == 0)
         mb.popup();
      else
      {
         mb.popupNonBlocking();
         Vm.sleep(delay);
         mb.unpop();
      }
      return mb.getPressedButtonIndex();
   }
   
   /* Called from the Window class to record the event posted */
   public void onEvent(int type, int key, int x, int y, int modifiers)
   {
      switch (type)
      {
         case PenEvent.PEN_DOWN:
         case PenEvent.PEN_UP:
         case PenEvent.PEN_DRAG:
         case KeyEvent.KEY_PRESS:
         case KeyEvent.SPECIAL_KEY_PRESS:
            // handle these events
            break;
         default:
            // skip all others
            return;
      }
      try
      {
         int timestamp = Vm.getTimeStamp();
         int elapsed = timestamp - lastTS;
         lastTS = timestamp;
         // int type, int key, int x, int y, int modifiers, int timeStamp
         fds.writeInt(type);
         fds.writeInt(key);
         fds.writeInt(x);
         fds.writeInt(y);
         fds.writeInt(modifiers);
         fds.writeInt(elapsed);
         if (Settings.onJavaSE)
            Vm.debug((counter++)+" "+type+" "+key+" "+x+" "+y+" "+modifiers+" "+elapsed);
      }
      catch (Exception ee)
      {
         MessageBox.showException(ee,true);
      }
   }

   public void stop() throws Exception
   {
      if (status == RECORDING)
      {
         if (flog.getSize() == 0) // if nothing was written, just cancel the log
            flog.delete();
         else
         {
            fds.writeInt(0); // end mark
            flog.close();
         }
         flog = null;
         fillListOfRecordedRobots();
         showMessage("Finished recording",null,2000);
      }
      status = IDLE;
   }
   
   private static class StrTime implements Comparable
   {
      String s;
      long l;
      
      StrTime(String s, Time t)
      {
         this.s = s;
         l = t.getTimeLong();
      }
      
      public int compareTo(Object other) throws ClassCastException
      {
         StrTime st = (StrTime)other;
         long res = l - st.l;
         return res > 0 ? 1 : res < 0 ? -1 : 0;
      }
      
   }
   
   private void fillListOfRecordedRobots() throws Exception
   {
      String[] list = new File(Settings.appPath).listFiles();
      recordedRobots = null;
      if (list != null && list.length > 0)
      {
         Vector v = new Vector(10);
         for (int i =0; i < list.length; i++)
            if (list[i].endsWith(".robot"))
            {
               File f = new File(Settings.appPath+"/"+list[i],File.READ_WRITE);
               Time t = f.getTime(File.TIME_MODIFIED);
               f.close();
               v.addElement(new StrTime(list[i]+" ("+new Date(t)+" "+t+")", t));
            }
         int n = v.size();
         if (n > 0)
         {
            v.qsort();
            recordedRobots = new String[n];
            while (--n >= 0)
               recordedRobots[n] = ((StrTime)v.items[n]).s;
         }
      }
   }
}
