package totalcross.ui;

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
   
   /** Shows a toast message using the given parameters.
    * Sample:
    * <pre>
    * String message = "This is a test";
    * Toast.show("\n"+message+"\n", 2000);
    * </pre>
    * See the public static fields of this class to show how you can customize the appearance.
    */
   public static void show(final String message, final int delay)
   {
      try
      {
         final Window parent = Window.getTopMost();
         btn = new Button("");
         btn.eventsEnabled = false;
         btn.setText(message);
         btn.setBorder(Button.BORDER_ROUND);
         btn.setBackForeColors(backColor, foreColor);
         btn.setFont(font);
         FadeAnimation.maxFade = fade;
         parent.add(btn, posX, posY, width, height);
         ControlAnimation animation = FadeAnimation.create(btn, true, null, -1);
         animation.delayAfterFinish = delay;
         animation.then(FadeAnimation.create(btn, false, new ControlAnimation.AnimationFinished()
         {
            public void onAnimationFinished(ControlAnimation anim)
            {
               parent.remove(btn);
               btn = null;
            }
         }, -1)).start();
         FadeAnimation.maxFade = FadeAnimation.DEFAULT_MAX_FADE;
      }
      catch (Exception e)
      {
         e.printStackTrace();
         btn = null;
      }

   }
}
