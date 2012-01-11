package tc.samples.ui.androidui;

import totalcross.res.Resources;
import totalcross.ui.Button;
import totalcross.ui.ScrollContainer;
import totalcross.ui.gfx.Color;
import totalcross.ui.image.Image;

public class ButtonSamples extends BaseContainer
{
   public ButtonSamples()
   {
      helpMessage = "These are Button samples in the Android user interface style. Press back to go to the main menu.";
   }
   
   public void initUI()
   {
      super.initUI();
      ScrollContainer sc = new ScrollContainer(false, true);
      sc.setInsets(gap, gap, gap, gap);
      Button c;

      Button.commonGap = gap;

      sc.add(new Button("Simple button"), LEFT, AFTER, PREFERRED, PREFERRED - gap);

      sc.add(c = new Button("This is\na multi-line\nButton"), LEFT, AFTER + gap);
      c.setPressedColor(Color.ORANGE);

      Image img = Resources.warning.getSmoothScaledInstance(fmH, fmH, -1);
      img.applyColor2(BKGCOLOR);
      sc.add(c = new Button("This is an image Button", img, LEFT, gap), LEFT, AFTER + gap);
      c.setBackColor(SELCOLOR);

      Button.commonGap = 0;

      add(sc, LEFT, TOP, FILL, FILL);
      setInfo("Click Info button for help.");
   }
}