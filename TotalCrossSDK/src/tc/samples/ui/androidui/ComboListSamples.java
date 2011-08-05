package tc.samples.ui.androidui;

import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.gfx.*;

public class ComboListSamples extends BaseContainer
{
   public ComboListSamples()
   {
      helpMessage = "These are ComboBox and ListBox samples in the Android user interface style. Press back to go to the main menu.";
   }
   
   public void initUI()
   {
      try
      {
         super.initUI();
         setTitle("ComboBox and ListBox");
         
         ScrollContainer sc = new ScrollContainer(false, true);
         sc.setInsets(gap,gap,gap,gap);
         add(sc,LEFT,TOP,FILL,FILL);
         
         Control c;
         String[] items = {"One","Two","Three","Four","Five","Six","Seven","Eight","Nine","Ten","Um","Dois","Tres","Quatro","Cinco","Seis","Sete","Oito","Nove","Dez"};
         c = new ComboBox(items);
         ((ComboBox)c).popupTitle = "Select the item";
         c.setBackColor(Color.BRIGHT);
         ((ComboBox)c).checkColor = Color.GREEN;
         sc.add(c,LEFT,AFTER,FILL,PREFERRED+gap);
         
         c = new ListBox(items);
         c.setBackColor(SELCOLOR);
         sc.add(c,LEFT,AFTER+gap,FILL,fmH*7+4);
         
         setInfo("Click Info button for help.");
      }
      catch (Exception ee)
      {
         MessageBox.showException(ee,true);
      }
   }
}