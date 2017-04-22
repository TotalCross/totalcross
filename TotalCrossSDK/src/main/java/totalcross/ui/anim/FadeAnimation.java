package totalcross.ui.anim;

import totalcross.ui.*;
import totalcross.ui.event.*;

public class FadeAnimation extends ControlAnimation implements TimerListener
{
   private int a,at,af,_maxFade;
   private boolean fadeIn;
   public static int DEFAULT_MAX_FADE = 255;
   /** Change this will affect all fade animations until you reset it to DEFAULT_MAX_FADE.
    * @see totalcross.ui.Toast 
    */
   public static int maxFade = 255;
   
   private FadeAnimation(Control c, boolean fadeIn, AnimationFinished animFinish, int totalTime)
   {
      super(c,animFinish, totalTime);
      this.fadeIn = fadeIn;
      at = _maxFade = maxFade;
      a = fadeIn ? 0 : maxFade;
      af = fadeIn ? maxFade : 0;
      releaseScreenShot = maxFade == 255;
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
      if (a > _maxFade) a = _maxFade; else if (a < 0) a = 0;
      if (c.offscreen != null)
         c.offscreen.alphaMask = a;
      Window.needsPaint = true;
      if (a == af || speed == 0)
      {
         a = af;
         stop(false);
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
