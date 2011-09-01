package tc.samples.ui.gadgets;

import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;

public class TabbedContainerWithImages extends Container
{
   public void initUI()
   {
      try
      {
         Image[] images =
         {
            new Image("tab0.gif"),
            new Image("tab1.gif"),
            new Image("tab2.gif"),
            new Image("tab3.gif"),
            new Image("tab4.gif"),
         };
         final Check ch;
         final TabbedContainer tp;
         add(ch = new Check("Disable tabs 1 and 3"), LEFT,TOP+2);
         tp = new TabbedContainer(images,0);
         if (uiAndroid)
            tp.setBackColor(Color.darker(backColor,32));
         tp.activeTabBackColor = Color.ORANGE;
         add(tp, LEFT+5,AFTER+5,FILL-5,FILL-5);
         tp.getContainer(3).setBackColor(Color.CYAN);
         tp.useOnTabTheContainerColor = true;
         ch.addPressListener(new PressListener()
         {
            public void controlPressed(ControlEvent e)
            {
               tp.setEnabled(1,!ch.isChecked());
               tp.setEnabled(3,!ch.isChecked());
            }
         });
      }
      catch (Exception e)
      {
         MessageBox.showException(e, false);
      }
   }
}
