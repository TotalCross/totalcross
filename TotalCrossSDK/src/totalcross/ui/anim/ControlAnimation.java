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
   
   public int totalTime = 800;
   public int frameRate = 20;
   
   public static interface AnimationFinished
   {
      public void onAnimationFinished(ControlAnimation anim);
   }
   
   public ControlAnimation(Control c, AnimationFinished animFinish)
   {
      this.c = c;
      this.animFinish = animFinish;
   }
   
   public ControlAnimation(Control c)
   {
      this(c,null);
   }

   public void start() throws Exception
   {
      if (c.offscreen == null)
      {
         te = c.addTimer(frameRate);
         c.addTimerListener(this);
         Window.enableUpdateScreen = false; // removes flick when clicking outside the TopMenu
         c.takeScreenShot();
         Window.needsPaint = true;
      }
      if (with != null) with.start();
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
   
   public ControlAnimation with(ControlAnimation other)
   {
      this.with = other;
      other.c = c;
      return this;
   }
   
   public ControlAnimation then(ControlAnimation other)
   {
      this.then = other;
      other.c = c;
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
