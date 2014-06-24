package totalcross.ui.anim;

import totalcross.ui.*;
import totalcross.ui.event.*;

public abstract class ControlAnimation implements TimerListener
{
   Container c;
   TimerEvent te;
   public int totalTime = 1000;
   public int frameRate = 25;
   
   public ControlAnimation(Container c)
   {
      this.c = c;
   }

   protected void start() throws Exception
   {
      te = c.addTimer(frameRate);
      c.addTimerListener(this);
      c.takeScreenShot();
      Window.needsPaint = true;
   }

   public void stop()
   {
      c.removeTimer(te);
      te = null;
      c.releaseScreenShot();
   }
   
   public void timerTriggered(TimerEvent e)
   {
      if (te != null && te.triggered)
         animate();
   }
   
   public abstract void animate();
}
