package totalcross.ui.anim;

import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.event.*;

public abstract class ControlAnimation implements TimerListener
{
   Control c;
   TimerEvent te;
   ControlAnimation with,then;
   AnimationFinished animFinish;
   int initialTime;
   boolean slave;
   public int id;
   private static int _id;
   
   public int totalTime = 800;
   public int frameRate = Settings.platform.equals(Settings.WINDOWSPHONE) ? 30 : 20;
   
   public static interface AnimationFinished
   {
      public void onAnimationFinished(ControlAnimation anim);
   }
   
   public ControlAnimation(Control c, AnimationFinished animFinish)
   {
      this.c = c;
      this.animFinish = animFinish;
      id = _id++;
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
      if (then != null)
         try {then.start();} catch (Exception e) {if (Settings.onJavaSE) e.printStackTrace();}
   }
   
   protected double computeSpeed(double distance)
   {
      int elapsed = totalTime-(Vm.getTimeStamp()-initialTime);
      if (elapsed <= 0)
         return 0;
      double ret = distance * frameRate / elapsed;
      return ret;
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
   
   public abstract void animate();
}
