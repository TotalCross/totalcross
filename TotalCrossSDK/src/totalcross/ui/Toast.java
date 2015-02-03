package totalcross.ui;

import totalcross.sys.*;
import totalcross.ui.anim.*;
import totalcross.ui.font.*;
import totalcross.ui.gfx.*;

/** Class used to show a message on screen.
 */
public class Toast
{
   /** Back color used in the toast. */
   public static int backColor = 0x303030;
   /** Fore color used in the toast. */
   public static int foreColor = Color.WHITE;
   /** X position of the toast. Defaults to CENTER. */
   public static int posX = Control.CENTER;
   /** Y position of the toast. Defaults to BOTTOM - fmH. */
   public static int posY = Control.BOTTOM - Font.NORMAL_SIZE;
   /** Width of the toast. Defaults to PREFERRED + fmH * 4. */
   public static int width = Control.PREFERRED + Font.NORMAL_SIZE * 4;
   /** Height of the toast. Defaults to PREFERRED. */
   public static int height = Control.PREFERRED;
   /** The stopping fade value, used on transparency. Defaults to 200. */
   public static int fade = 200;
   /** The font to be used. Defaults to the default bold font. */
   public static Font font = MainWindow.getDefaultFont().asBold();
   
   /** The toast component used to show the text. */
   public static Button btn;
   private static ControlAnimation animation;
   
   public static final int INFINITE = Convert.MAX_INT_VALUE;
   public static final int INFINITE_NOANIM = Convert.MIN_INT_VALUE;;
   
   /** Shows a toast message using the given parameters.
    * Sample:
    * <pre>
    * String message = "This is a test";
    * Toast.show("\n"+message+"\n", 2000);
    * </pre>
    * See the public static fields of this class to show how you can customize the appearance.
    * Calling with a nulled message will make the last toast disappear.
    * 
    * The text is splitted if wider than the screen.
    * 
    * If delay is INFINITE, it will wait forever until you call show(null,0).
    * If delay is INFINITE_NOANIM, it will wait forever until you call show(null,0) and will not use animation.
    */
   public static void show(final String message, final int delay)
   {
      MainWindow.getMainWindow().runOnMainThread(new Runnable()
      {
         public void run()
         {
            final Window parent = Window.getTopMost();
            if (btn != null && btn.parent != null)
            {
               btn.parent.remove(btn);
               btn = null;
               if (animation != null) animation.stop(true);
               animation = null;
            }
            if (message != null)
            try
            {
               String msg = message;
               if (message.indexOf('\n') == -1)
                  msg = Convert.insertLineBreakBalanced(Settings.screenWidth * 9 / 10, font.fm, message);
               btn = new Button("");
               btn.eventsEnabled = false;
               btn.setText(msg.trim());
               btn.setBorder(Button.BORDER_ROUND);
               btn.setBackForeColors(backColor, foreColor);
               btn.setFont(font);
               parent.add(btn, posX, posY, width, height);
               if (delay != INFINITE_NOANIM)
               {
                  FadeAnimation.maxFade = fade;
                  animation = FadeAnimation.create(btn, true, null, -1);
                  animation.delayAfterFinish = delay;
                  final Button thisBtn = btn;
                  animation.then(FadeAnimation.create(btn, false, new ControlAnimation.AnimationFinished()
                  {
                     public void onAnimationFinished(ControlAnimation anim)
                     {
                        if (btn != null && btn.parent != null)
                           btn.parent.remove(btn);
                        if (thisBtn == btn) // button may change if user tries to show several Toasts
                        {
                           btn = null;
                           animation = null;
                        }
                     }
                  }, -1)).start();
                  FadeAnimation.maxFade = FadeAnimation.DEFAULT_MAX_FADE;
               }
            }
            catch (Exception e)
            {
               e.printStackTrace();
               btn = null;
            }
         }
      },true);

   }
}
