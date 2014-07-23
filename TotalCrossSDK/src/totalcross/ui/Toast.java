package totalcross.ui;

import totalcross.ui.anim.*;
import totalcross.ui.font.*;

/** Class used to show a message on screen.
 */
public class Toast
{
   /** Shows a toast message using the given parameters.
    * Sample:
    * <pre>
    * String message = "This is a test";
    * Toast.show("\n"+message+"\n", 0x303030, Color.WHITE, 2000);
    * </pre>
    */
   public static void show(final String message, final int backColor, final int foreColor, final int delay)
   {
      try
      {
         final Window parent = Window.getTopMost();
         final Button msgBtn = new Button("");
         msgBtn.setText(message);
         msgBtn.setBorder(Button.BORDER_ROUND);
         msgBtn.setBackForeColors(backColor, foreColor);
         int fmH = Font.NORMAL_SIZE;
         FadeAnimation.maxFade = 200;
         parent.add(msgBtn, Control.CENTER, Control.BOTTOM-fmH, Control.PREFERRED+fmH*4, Control.PREFERRED);
         ControlAnimation animation = FadeAnimation.create(msgBtn, true, null, -1);
         animation.delayAfterFinish = 2000;
         animation.then(FadeAnimation.create(msgBtn, false, new ControlAnimation.AnimationFinished()
         {
            public void onAnimationFinished(ControlAnimation anim)
            {
               parent.remove(msgBtn);
            }
         }, -1)).start();
         FadeAnimation.maxFade = FadeAnimation.DEFAULT_MAX_FADE;
      }
      catch (Exception e)
      {
         e.printStackTrace();

      }

   }
}
