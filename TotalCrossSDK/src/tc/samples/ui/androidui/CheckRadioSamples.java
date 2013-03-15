package tc.samples.ui.androidui;

import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.gfx.*;

public class CheckRadioSamples extends BaseContainer
{
   public CheckRadioSamples()
   {
      helpMessage = "These are Check and Radio box samples in the Android user interface style. Press back to go to the main menu.";
   }
   
   public void initUI()
   {
      try
      {
         super.initUI();
         setTitle("Check and Radio");

         ScrollContainer sc = new ScrollContainer(false, true);
         sc.setInsets(gap,gap,gap,gap);
         Check c;
         
         sc.add(c = new Check("Check box / cyan check"),LEFT,AFTER,PREFERRED+gap,PREFERRED+gap); 
         c.checkColor = Color.CYAN;
         c.setChecked(true);

         sc.add(c = new Check("Check box / yellow background"),LEFT,AFTER+gap,PREFERRED+gap,PREFERRED+gap);
         c.setBackColor(Color.YELLOW);
         c.textColor = Color.BLUE;
         c.checkColor = Color.YELLOW;

         sc.add(c = new Check("Check box / green foreground"),LEFT,AFTER+gap,PREFERRED+gap,PREFERRED+gap); 
         c.setForeColor(Color.darker(Color.GREEN));
         c.checkColor = Color.GREEN;

         RadioGroupController rg = new RadioGroupController();
         
         Radio r;
         sc.add(r = new Radio("Radio / cyan check",rg),LEFT,AFTER+gap*2,PREFERRED+gap,PREFERRED+gap); 
         r.checkColor = Color.CYAN;
         r.setChecked(true);

         sc.add(r = new Radio("Radio / yellow background",rg),LEFT,AFTER+gap,PREFERRED+gap,PREFERRED+gap);
         r.setBackColor(Color.YELLOW);
         r.textColor = Color.BLUE;
         r.checkColor = Color.YELLOW;

         sc.add(r = new Radio("Radio / green foreground",rg),LEFT,AFTER+gap,PREFERRED+gap,PREFERRED+gap); 
         r.setForeColor(Color.darker(Color.GREEN));
         r.checkColor = Color.GREEN;
         
         add(sc,LEFT,TOP,FILL,FILL);
         setInfo("Click Info button for help.");
      }
      catch (Exception ee)
      {
         MessageBox.showException(ee,true);
      }
   }
}