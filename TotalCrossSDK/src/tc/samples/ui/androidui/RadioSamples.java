package tc.samples.ui.androidui;

import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.gfx.*;

public class RadioSamples extends BaseContainer
{
   public RadioSamples()
   {
      helpMessage = "These are Radio box samples in the Android user interface style. Press back to go to the main menu.";
   }
   
   public void initUI()
   {
      try
      {
         super.initUI();
         ScrollContainer sc = new ScrollContainer(false, true);
         sc.setInsets(gap,gap,gap,gap);
         Radio c;
         
         RadioGroupController rg = new RadioGroupController();
         
         sc.add(c = new Radio("Radio / cyan check",rg),LEFT,AFTER,PREFERRED+gap,PREFERRED+gap); 
         c.checkColor = Color.CYAN;
         c.setChecked(true);

         sc.add(c = new Radio("Radio / yellow background",rg),LEFT,AFTER+gap,PREFERRED+gap,PREFERRED+gap);
         c.setBackColor(Color.YELLOW);
         c.checkColor = Color.YELLOW;

         sc.add(c = new Radio("Radio / green foreground",rg),LEFT,AFTER+gap,PREFERRED+gap,PREFERRED+gap); 
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