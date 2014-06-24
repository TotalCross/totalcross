package totalcross.ui.anim;

import totalcross.ui.*;
import totalcross.ui.event.*;

public class FadeAnimation extends ControlAnimation implements TimerListener
{
   int a,a0,af,speed;
   boolean fadeIn;
   
   public FadeAnimation(Container c, boolean fadeIn)
   {
      super(c);
      this.fadeIn = fadeIn;
      a = a0 = fadeIn ? 0 : 255;
      af = fadeIn ? 255 : 0;
   }
   
   public void start() throws Exception
   {
      int dist = 255;
      int fps = totalTime / frameRate;
      speed = dist / fps;
      if (!fadeIn)
         speed = -speed;
      super.start();
   }
   
   public void animate()
   {
      a += speed;
      if (a > 255) a = 255; else if (a < 0) a = 0;
      if (c.offscreen != null)
         c.offscreen.alphaMask = a;
      Window.needsPaint = true;
      if (a == af)
         stop();
   }

   public static void create(Container c, boolean fadeIn)
   {
      try
      {
         new FadeAnimation(c,fadeIn).start();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }
}
