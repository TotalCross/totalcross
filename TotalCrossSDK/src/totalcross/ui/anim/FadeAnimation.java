package totalcross.ui.anim;

import totalcross.ui.*;
import totalcross.ui.event.*;

public class FadeAnimation extends ControlAnimation implements TimerListener
{
   private int a,at,af;
   private boolean fadeIn;
   
   private FadeAnimation(Control c, boolean fadeIn, AnimationFinished animFinish, int totalTime)
   {
      super(c,animFinish, totalTime);
      this.fadeIn = fadeIn;
      at = 255;
      a = fadeIn ? 0 : 255;
      af = fadeIn ? 255 : 0;
   }

   public void start() throws Exception
   {
      super.start();
      c.offscreen.alphaMask = a;
   }
   
   protected void animate()
   {
      int speed = (int)computeSpeed(at);
      at -= speed;
      a += fadeIn ? speed : -speed;
      if (a > 255) a = 255; else if (a < 0) a = 0;
      if (c.offscreen != null)
         c.offscreen.alphaMask = a;
      Window.needsPaint = true;
      if (a == af || speed == 0)
      {
         a = af;
         stop();
      }
   }

   /** Creates a path animation, moving the control in a direction.
    * @param c The control to be moved
    * @param fadeIn True will make the control appear, false will make it disappear.
    * @param animFinish An interface method to be called when the animation finished, or null if none.
    * @param totalTime The total time in millis that the animation will take, or -1 to use the default value (800ms).
    */
   public static FadeAnimation create(Control c, boolean fadeIn, AnimationFinished animFinish, int totalTime)
   {
      try
      {
         return new FadeAnimation(c,fadeIn, animFinish, totalTime);
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      return null;
   }
}
