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

package totalcross.ui;

import totalcross.sys.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;
import totalcross.util.concurrent.*;

/** Spinner is a control that shows an image indicating that something is running in
 * the background. It has two styles: IPHONE and ANDROID. Its used in the ProgressBox.
 * 
 * To start the spin call the start method, and to stop it call the stop method.
 * 
 * If you try to run a spinner inside a tight loop, be sure to call <code>spinner.update()</code> or the spinner will not be
 * updated. Without this, it will work in Java but not on devices. 
 * 
 * @since TotalCross 1.3
 */
public class Spinner extends Control implements Runnable
{
   /** Used in the type field */
   public static final int IPHONE = 1;
   /** Used in the type field */
   public static final int ANDROID = 2;
   
   /** Defines the type of spinner for all instances. Defaults for IPHONE when running in iPhone and
    * ANDROID for all other platforms. 
    */
   public static int spinnerType = Settings.isIOS() ? IPHONE : ANDROID;
   
   private Coord []coords;
   private int []colors;
   private int slots, slot0, size, type;
   private boolean running;
   private Lock lock;
   private Image gif,gif0;
   
   public Spinner()
   {
      type = spinnerType;
      slots = 8;
      if (UIColors.spinnerBack != -1) backColor = UIColors.spinnerBack;
      foreColor = UIColors.spinnerFore;
      lock = new Lock();
   }
   
   /** Creates a spinner from an animated GIF.
    * You can download additional animations from: <a href='http://preloaders.net/en'>here</a>. 
    * Select image type as GIF and transparent background as Yes.
    */

   public Spinner(Image gif)
   {
      lock = new Lock();
      this.gif0 = gif;
   }

   public void onBoundsChanged(boolean screenChanged)
   {
      size = width < height ? width : height;
      if ((size % 2) == 0) size--;
      
      if (gif0 != null)
         try
         {
            gif = gif0.smoothScaledFixedAspectRatio(size,true);
         }
         catch (ImageException e)
         {
            gif = null;
         }
      if (gif == null && (!screenChanged || coords == null))
      {
         int xyc = size/2;
         // find the number of slots
         int bestn=0;
         switch (type)
         {
            case IPHONE: 
               bestn = height >= 24 ? 16 : 12;
               break;
            case ANDROID: 
               bestn = height >= 21 ? 12 : 8; 
               break;
         }
         if (slots != bestn)
         {
            slots = bestn;
            onColorsChanged(true);
         }
         
         if (type == ANDROID)
            return;
         
         int astep = 360 / slots;
         int a = 0;
         
         Graphics g = getGraphics();
         coords = new Coord[slots*3];
         for (int i = 0; i < coords.length;)
         {
            g.getAnglePoint(xyc,xyc,xyc+1,xyc+1,a-1,coords[i++] = new Coord());
            g.getAnglePoint(xyc,xyc,xyc,  xyc,  a,  coords[i++] = new Coord());
            g.getAnglePoint(xyc,xyc,xyc+1,xyc+1,a+1,coords[i++] = new Coord());
            a += astep;
         }
      }
   }
   
   public void onColorsChanged(boolean changed)
   {
      if (gif == null && (changed || colors == null))
      {
         if (colors == null || colors.length != slots)
            colors = new int[slots];
         for (int i = 0; i < slots; i++) 
            colors[i] = Color.interpolate(backColor,foreColor,100*i/slots);
      }
   }
   
   public void onPaint(Graphics g)
   {
      synchronized (lock)
      {
         if (gif != null)
         {
            if (!Settings.isOpenGL)
            {
               g.backColor = backColor;
               g.fillRect(0,0,width,height);
            }
            g.drawImage(gif, (width-gif.getWidth())/2,(height-gif.getHeight())/2);
         }
         else
         {
            int astep = 360/slots;
            int a = 360;
            
            int div = type == IPHONE ? 3 : 1;
            int xyc = size/2;
            for (int i = slots * div; --i >= 0;)
            {
               int idx = (i/div+slot0)%slots;
               switch (type)
               {
                  case ANDROID:
                     g.backColor = colors[idx];
                     g.foreColor = backColor;
                     g.fillPie(xyc,xyc,xyc,a-astep,a);
                     //g.drawPie(xyc,xyc,xyc,a-astep,a);
                     a -= astep;
                     break;
                  case IPHONE:
                     g.backColor = g.foreColor = colors[idx];
                     g.drawLine(coords[i].x,coords[i].y,xyc,xyc);
                     break;
               }
               g.backColor = backColor;
               g.fillCircle(xyc,xyc,xyc/2);
            }
         }
      }
   }
   
   /** Starts the spinning thread. */
   public void start()
   {
      if (running)
         return;
      running = true;
      new Thread(this).start();
   }
   
   /** Stops the spinning thread. */
   public void stop()
   {
      running = false;
   }

   /** Returns if the spin is running. */
   public boolean isRunning()
   {
      return running;
   }

   private void step()
   {
      if (getParentWindow() == Window.topMost) // don't update if we loose focus
      {
         if (gif != null)
            gif.nextFrame();
         else
            slot0++;
         threadsafeUpdateScreen();
      }
   }
   
   public void run()
   {
      while (running)
      {
         step();
         Vm.sleep(gif != null ? 80 : 120); // with safeSleep, the vm starts to behave slowly and strangely
      }      
   }
   
   int last;
   /** Updates the spinner; call this when using the spinner inside a loop. */
   public void update()
   {
      int now = Vm.getTimeStamp();
      if ((now - last) > (gif != null ? 80 : 120)) // prevents calling pumpEvents too fast
      {
         step();
         if (!MainWindow.isMainThread())
            Vm.sleep(1);
         last = now;
      }
   }
}
