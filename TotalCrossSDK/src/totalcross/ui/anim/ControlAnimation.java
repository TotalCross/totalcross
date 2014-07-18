package totalcross.ui.anim;

import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.event.*;

/** Abstract class used to create and handle animations
 * @since TotalCross 3.03 
 */

public abstract class ControlAnimation implements TimerListener
{
   protected Control c;
   protected int totalTime;
   private TimerEvent te;
   private ControlAnimation with,then;
   private AnimationFinished animFinish;
   private int initialTime;
   private boolean slave;
   
   public static int frameRate = Settings.platform.equals(Settings.WINDOWSPHONE) ? 30 : 20;
   /** A delay issued right after the animation finishes */
   public int delayAfterFinish;
   
   public static interface AnimationFinished
   {
      public void onAnimationFinished(ControlAnimation anim);
   }
   
   public ControlAnimation(Control c, AnimationFinished animFinish, int totalTime)
   {
      this.c = c;
      this.animFinish = animFinish;
      this.totalTime = totalTime <= 0 ? 800 : totalTime;
   }
      
   public ControlAnimation(Control c, AnimationFinished animFinish)
   {
      this(c, animFinish, -1);
   }
   
   public ControlAnimation(Control c)
   {
      this(c,null);
   }

   public void start() throws Exception
   {
      if (!slave && c.offscreen == null)
      {
         te = c.addTimer(frameRate);
         c.addTimerListener(this);
         Window.enableUpdateScreen = false; // removes flick when clicking outside the TopMenu
         c.takeScreenShot();
         Window.needsPaint = true;
      }
      if (with != null) with.start();
      initialTime = Vm.getTimeStamp();
   }

   public void stop()
   {
      if (te != null)
      {
         c.removeTimer(te);
         te = null;
         c.releaseScreenShot();
      }
      if (animFinish != null)
         animFinish.onAnimationFinished(this);
      Vm.sleep(delayAfterFinish);
      if (then != null)
         try {then.start();} catch (Exception e) {if (Settings.onJavaSE) e.printStackTrace();}
   }
   
   protected double computeSpeed(double distance)
   {
      int remaining = totalTime-(Vm.getTimeStamp()-initialTime);
      if (remaining <= 0)
         return 0;
      return distance * frameRate / remaining;
   }
   
   public ControlAnimation with(ControlAnimation other)
   {
      this.with = other;
      other.slave = true;
      return this;
   }
   
   public ControlAnimation then(ControlAnimation other)
   {
      this.then = other;
      return this;
   }
   
   public void timerTriggered(TimerEvent e)
   {
      if (te != null && te.triggered)
         animatePriv();
   }
   
   private void animatePriv()
   {
      animate();
      if (with != null)
         with.animatePriv();
      Window.enableUpdateScreen = true;
   }
   
   protected abstract void animate();
}
