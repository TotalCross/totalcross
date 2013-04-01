package tc.samples.ui.androidui;

import totalcross.ui.*;
import totalcross.ui.gfx.*;

public class MultiButtonSamples extends BaseContainer
{
   public MultiButtonSamples()
   {
      helpMessage = "These are MultiButton samples in the Android user interface style. Press back to go to the main menu.";
   }
   
   public void initUI()
   {
      super.initUI();
      ScrollContainer sc = new ScrollContainer(false, true);
      sc.setInsets(gap, gap, gap, gap);

      sc.add(new Label("Normal"),LEFT,TOP+fmH);
      MultiButton b = new MultiButton(new String[]{"+","-"});
      b.setBackColor(Color.ORANGE);
      b.is3dText = true;
      sc.add(b,SAME,AFTER,PREFERRED,fmH*3/2);

      sc.add(new Label("Sticky with 3d text, center disabled"),LEFT,AFTER+fmH);
      b = new MultiButton(new String[]{"Left","Center","Right"});
      b.setBackColor(Color.GREEN);
      b.isSticky = b.is3dText = true;
      sc.add(b,SAME,AFTER,PREFERRED+fmH*2,fmH*2);
      
      b.setEnabled(1,false);

      add(sc, LEFT, TOP, FILL, FILL);
      setInfo("Click Info button for help.");
   }
}