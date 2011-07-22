package tc.samples.ui.androidui;

import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.gfx.*;

public class CheckSamples extends BaseContainer
{
   public CheckSamples()
   {
      helpMessage = "These are Check box samples in the Android user interface style. Press back to go to the main menu.";
   }
   
   public void initUI()
   {
      try
      {
         super.initUI();
         ScrollContainer sc = new ScrollContainer(false, true);
         sc.setInsets(gap,gap,gap,gap);
         Check c;
         
         sc.add(c = new Check("Check box / cyan check"),LEFT,AFTER,PREFERRED+gap,PREFERRED+gap); 
         c.checkColor = Color.CYAN;
         c.setChecked(true);

         sc.add(c = new Check("Check box / yellow background"),LEFT,AFTER+gap,PREFERRED+gap,PREFERRED+gap);
         c.setBackColor(Color.YELLOW);
         c.checkColor = Color.YELLOW;

         sc.add(c = new Check("Check box / green foreground"),LEFT,AFTER+gap,PREFERRED+gap,PREFERRED+gap); 
         c.setForeColor(Color.darker(Color.GREEN));
         c.checkColor = Color.GREEN;

         add(sc,LEFT,TOP,FILL,FILL);
         setInfo("Click Info button for help.");
      }
      catch (Exception ee)
      {
         MessageBox.showException(ee,true);
      }
   }
}