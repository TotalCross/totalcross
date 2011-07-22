package tc.samples.ui.androidui;

import totalcross.res.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;

public class ButtonSamples extends BaseContainer
{
   public ButtonSamples()
   {
      helpMessage = "These are Button samples in the Android user interface style. Press back to go to the main menu.";
   }
   
   public void initUI()
   {
      try
      {
         super.initUI();
         ScrollContainer sc = new ScrollContainer(false, true);
         sc.setInsets(gap,gap,gap,gap);
         Button c;
         
         Button.commonGap = gap;
         sc.add(new Button("Simple button"),LEFT,AFTER,PREFERRED,PREFERRED-gap);
         
         sc.add(c = new Button("This is\na multi-line\nButton"),LEFT,AFTER+gap); 
         c.setPressedColor(Color.ORANGE);
         
         Image img = Resources.warning.getSmoothScaledInstance(fmH,fmH,-1);
         img.applyColor2(BKGCOLOR);
         sc.add(c = new Button("This is an image Button", img, LEFT, gap),LEFT,AFTER+gap);
         c.setBackColor(SELCOLOR);
         
         add(sc,LEFT,TOP,FILL,FILL);
         setInfo("Click Info button for help.");
      }
      catch (Exception ee)
      {
         MessageBox.showException(ee,true);
      }
   }
}