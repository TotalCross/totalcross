package tc.samples.ui.gadgets;

import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.image.*;

public class ImageButtonResolutionsScale extends Container
{
   public void initUI()
   {
      String[] imageNames = {"cancel.png", "ok.png"}; // images are 300x300
      int imgRes = 2048;
      int targetRes[] = {480,320,240};
      int k=0;
      final Button []btns = new Button[imageNames.length * targetRes.length];

      try
      {
         for (int i = 0; i < 2; i++)
         {
            Image original = new Image(imageNames[i]);

            for (int j = 0; j < targetRes.length; j++)
            {
               double factor = (double) targetRes[j] / (double) imgRes;
               Image img2 = original.smoothScaledBy(factor, factor);
               Button btn = btns[k++] = new Button(img2);
               if (j == 0) // just a demo for the user
                  btn.pressedImage = img2.getTouchedUpInstance((byte)64,(byte)0);
               btn.setBorder(Button.BORDER_NONE);
               add(btn, j == 0 ? (i == 0 ? LEFT+5 : RIGHT-15) : CENTER_OF, j == 0 ? TOP+5 : AFTER+5);
            }
         }
         final Check c = new Check("Enabled");
         add(c, RIGHT,BOTTOM);
         c.setChecked(true);
         c.addPressListener(new PressListener()
         {
            public void controlPressed(ControlEvent e)
            {
               for (int i =0; i < btns.length; i++)
                  btns[i].setEnabled(c.isChecked());
            }
         });
      }
      catch (Exception e)
      {
         MessageBox.showException(e, true);
      }
   }

}
