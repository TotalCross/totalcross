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

/** Spinner is a control that shows an image indicating that something is running in
 * the background. 
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
   /** Used in the type field */
   public static final int SYNC = 3;
   
   /** Defines the type of spinner for all instances. Defaults for IPHONE when running in iPhone and
    * ANDROID for all other platforms. 
    */
   public static int spinnerType = Settings.isIOS() ? IPHONE : ANDROID;
   
   private static Image[] loaded = new Image[4];
   private static String[] files =
   {
      null,
      "totalcross/res/spinner_iphone.gif",      
      "totalcross/res/spinner_android.gif",      
      "totalcross/res/spinner_sync.gif",      
   };
   private boolean running;
   private Image anim,anim0;
   
   /** Creates a spinner with the defined spinnerType. */
   public Spinner()
   {
      this(spinnerType);
   }

   /** Creates a spinner of the given type. */
   public Spinner(int type)
   {
      setType(type);
   }
   
   /** Changes the Spinner to one of the predefined types. */
   public void setType(int t)
   {
      if (t < IPHONE || t > SYNC)
         throw new IllegalArgumentException("Invalid type");
      try 
      {
         anim0 = loaded[t] == null ? loaded[t] = new Image(files[t]) : loaded[t];
         anim = null;
      } 
      catch (Exception e) 
      {
         if (Settings.onJavaSE) 
            e.printStackTrace();
      }
   }

   /** Creates a spinner from an animated GIF.
    * You can download additional animations from: <a href='http://preloaders.net/en'>here</a>. 
    * Change only the given settings:
    * <ul>
    *  <li> Image type: GIF
    *  <li> Transparent background: Yes
    *  <li> Foreground color: FFFFFF if the animation is only black, 000000 if it has fade.
    *  <li> Background color: 000000
    *  <li> Keep size 128 x 128
    * </ul>
    * Then press Generate preloader and download the gif file that will appear at the right pane.
    * If the spinner is moving counterclockwise, you can make it go clickwise by changing also, under the  Advanced options:
    * <ul>
    *  <li> Flip image: Hor
    *  <li> Reverse animation: Yes
    * </ul>
    * The image is colorized with the foreground color. 
    * If it appears not filled, try selecting the "Invert colors" option, and use 000000 as foreground color.
    */

   public Spinner(Image anim)
   {
      this.anim0 = anim;
      if (UIColors.spinnerBack != -1) backColor = UIColors.spinnerBack;
      foreColor = UIColors.spinnerFore;
   }
   
   /** Changes the gif image of this Spinner */
   public void setImage(Image anim)
   {
      this.anim0 = anim;
      this.anim = null;
   }

   public void onBoundsChanged(boolean screenChanged)
   {
      anim = null;
   }
   
   public void onColorsChanged(boolean changed)
   {
      anim = null;
   }
   
   public void onPaint(Graphics g)
   {
      if (!Settings.isOpenGL)
      {
         g.backColor = backColor;
         g.fillRect(0,0,width,height);
      }
      if (anim == null) checkAnim();
      if (anim != null)
         g.drawImage(anim, (width-anim.getWidth())/2,(height-anim.getHeight())/2);
   }
   
   private void checkAnim()
   {
      try
      {
         anim = anim0.smoothScaledFixedAspectRatio(width < height ? width : height,true);
         anim.applyColor2(getForeColor() | 0xAA000000);
      } catch (Exception e) {anim = null;}
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
      if (anim == null) checkAnim();
      if (getParentWindow() == Window.topMost && anim != null) // don't update if we loose focus
      {
         anim.nextFrame();
         safeRepaintNow();
      }
   }
   
   public void run()
   {
      while (running)
      {
         step();
         Vm.sleep(anim != null ? 80 : 120); // with safeSleep, the vm starts to behave slowly and strangely
      }      
   }
   
   int last;
   /** Updates the spinner; call this when using the spinner inside a loop. */
   public void update()
   {
      int now = Vm.getTimeStamp();
      if ((now - last) > (anim != null ? 80 : 120)) // prevents calling pumpEvents too fast
      {
         step();
         if (!MainWindow.isMainThread())
            Vm.sleep(1);
         last = now;
      }
   }
}
