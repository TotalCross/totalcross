package tc.samples.ui.androidui;

import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.gfx.*;

public class ComboListSamples extends BaseContainer
{
   public ComboListSamples()
   {
      helpMessage = "These are ComboBox (without and with search enabled) and ListBox samples in the Android user interface style. Press back to go to the main menu.";
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
         
         String[] items = {"One","Two","Three","Four","Five","Six","Seven","Eight","Nine","Ten","Um","Dois","Tres","Quatro","Cinco","Seis","Sete","Oito","Nove","Dez"};
         ComboBox cb = new ComboBox(items);
         cb.popupTitle = "Select the item";
         cb.enableSearch = false;
         cb.setBackColor(Color.BRIGHT);
         cb.checkColor = Color.GREEN;
         sc.add(cb,LEFT,AFTER,FILL,PREFERRED+gap);
         
         String[] items2 = {"cyan","black","blue","bright","green","dark","magenta","orange","pink","red","white","yellow"};
         cb = new ComboBox(items2);
         cb.popupTitle = "Select the item";
         cb.setBackColor(Color.BRIGHT);
         cb.checkColor = Color.CYAN;
         sc.add(cb,LEFT,AFTER+gap,FILL,PREFERRED+gap);

         ListBox l = new ListBox(items);
         l.setBackColor(SELCOLOR);
         sc.add(l,LEFT,AFTER+gap,FILL,fmH*7+4);
         
         setInfo("Click Info button for help.");
      }
      catch (Exception ee)
      {
         MessageBox.showException(ee,true);
      }
   }
}