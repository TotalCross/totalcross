package totalcross.ui.anim;

import totalcross.ui.*;
import totalcross.ui.event.*;

public abstract class ControlAnimation implements TimerListener
{
   Container c;
   TimerEvent te;
   ControlAnimation next;
   AnimationFinished animFinish;
   
   public int totalTime = 1000;
   public int frameRate = 20;
   
   public static interface AnimationFinished
   {
      public void onAnimationFinished(ControlAnimation anim);
   }
   
   public ControlAnimation(Container c, AnimationFinished animFinish)
   {
      this.c = c;
      this.animFinish = animFinish;
   }
   
   public ControlAnimation(Container c)
   {
      this(c,null);
   }

   public void start() throws Exception
   {
      if (c.offscreen == null)
      {
         te = c.addTimer(frameRate);
         c.addTimerListener(this);
         c.takeScreenShot();
         Window.needsPaint = true;
      }
      if (next != null) next.start();
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
   }
   
   public ControlAnimation concat(ControlAnimation other)
   {
      this.next = other;
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
      if (next != null)
         next.animatePriv();
   }
   
   public abstract void animate();
}
