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

// $Id: UIRobot.java,v 1.32 2011-01-04 13:19:26 guich Exp $

package totalcross.unit;

import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;

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
   private static MainWindow mw = MainWindow.getMainWindow();;
   private static Graphics myg = mw.getGraphics();
   private static final int CLICK_EVENT = 1302;
   private static final int TYPEIT_EVENT = 1303;
   
   public static int counter;
   
   /** Set this to something different of 0 to create a delay between each event, measured in miliseconds.
    * @since TotalCross 1.15 
    */ 
   public int delayBetweenEvents; // guich@tc115_49

   /** Constructs a new UIRobot */
   public UIRobot()
   {
      counter = 0;
   }

   /** Simulates a click (pen down followed by a pen up) in the given control, at relative position 0,0. */
   public void click(Control c)
   {
      click(c,0,0);
   }

   /** Simulates a pen down in the given control, at relative position 0,0. */
   public void penDown(Control c)
   {
      penDown(c,0,0);
   }

   /** Simulates a pen up in the given control, at relative position 0,0. */
   public void penUp(Control c)
   {
      penUp(c,0,0);
   }

   /** Simulates a pen drag in the given control, at relative position 0,0. */
   public void penDrag(Control c)
   {
      penDrag(c,0,0);
   }

   /** Simulates a click (pen down followed by a pen up) in the given control, at relative position deltaX,deltaY. */
   public void click(Control c, int deltaX, int deltaY)
   {
      Rect r = c.getAbsoluteRect();
      click(r.x+deltaX, r.y+deltaY);
   }

   /** Simulates a pen down in the given control, at relative position deltaX,deltaY. */
   public void penDown(Control c, int deltaX, int deltaY)
   {
      Rect r = c.getAbsoluteRect();
      penDown(r.x+deltaX, r.y+deltaY);
   }

   /** Simulates a pen up in the given control, at relative position deltaX,deltaY. */
   public void penUp(Control c, int deltaX, int deltaY)
   {
      Rect r = c.getAbsoluteRect();
      penUp(r.x+deltaX, r.y+deltaY);
   }

   /** Simulates a pen drag in the given control, at relative position deltaX,deltaY. */
   public void penDrag(Control c, int deltaX, int deltaY)
   {
      Rect r = c.getAbsoluteRect();
      penDrag(r.x+deltaX, r.y+deltaY);
   }

   /** Simulates a click (pen down followed by a pen up) at the given absolute position. */
   public void click(int x, int y)
   {
      showCursor(x,y);
      postEvent(x,y,0,null,CLICK_EVENT);
   }

   /** Simulates a pen down at the given absolute position. */
   public void penDown(int x, int y)
   {
      showCursor(x,y);
      postEvent(x,y,0,null,PenEvent.PEN_DOWN);
   }

   /** Simulates a pen up at the given absolute position. */
   public void penUp(int x, int y)
   {
      showCursor(x,y);
      postEvent(x,y,0,null,PenEvent.PEN_UP);
   }

   /** Simulates a pen drag at the given absolute position. */
   public void penDrag(int x, int y)
   {
      showCursor(x,y);
      postEvent(x,y,0,null,PenEvent.PEN_DRAG);
   }

   /** Adds a delay between two events. */
   public void delay(int ms)
   {
      Vm.sleep(ms);
   }

   /** Simulates a key press with the given key. */
   public void keyPress(int key)
   {
      postEvent(0,0,key,null,KeyEvent.KEY_PRESS);
   }

   /** Simulates the press of the ENTER key. */
   public void enter()
   {
      postEvent(0,0,SpecialKeys.ENTER,null,KeyEvent.SPECIAL_KEY_PRESS);
   }
   
   /** Simulates a special key press with the given key. */
   public void specialKeyPress(int key)
   {
      postEvent(0,0,key,null,KeyEvent.SPECIAL_KEY_PRESS);
   }

   /** Simulates the typying of the given string, as a series of key press events. */
   public void type(String s)
   {
      postEvent(0,0,0,s,TYPEIT_EVENT);
   }
   
   private void showCursor(int x, int y)
   {
      myg.backColor = Color.GREEN;
      myg.fillCircle(x,y,4);
      Window.updateScreen();
   }

   private void postEvent(final int x, final int y, final int key, final String s, final int type)
   {
      if (Settings.onJavaSE) Vm.debug(Convert.toString(++counter));
      final int ts = Vm.getTimeStamp();
      new Thread() 
      {
         public void run() 
         {
            switch (type)
            {
               case CLICK_EVENT:
                  mw._postEvent(PenEvent.PEN_DOWN, key, x, y, 0, ts);
                  mw._postEvent(PenEvent.PEN_UP, key, x, y, 0, ts+5);
                  break;
               case TYPEIT_EVENT:
                  for (int i =0,n = s.length(); i < n; i++)
                     mw._postEvent(KeyEvent.KEY_PRESS, s.charAt(i), 0,0,0, ts+i);
                  break;
               default:
                  mw._postEvent(type, key, x, y, 0, ts);                           
            }
         }
      }.start();
      if (delayBetweenEvents > 0)
         Vm.sleep(delayBetweenEvents);
   }
}
