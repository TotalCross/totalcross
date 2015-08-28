package tc.samples.api.ui.transluc;

import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;

public class ViewScreen extends TranslucentBaseContainer
{
   private static ViewScreen inst;
   public static ViewScreen getInstance() {return inst == null ? inst = new ViewScreen() : inst;}
   
   public void initUI()
   {
      try
      {
         super.initUI();
         setBackgroundImage(4);
         
         Button t1 = createTransBarButton("ui/images/bt_view.png");
         Button t2 = createTransBarButton("ui/images/bt_back.png");
         t2.addPressListener(new PressListener() {public void controlPressed(ControlEvent e) {goback();}});

         Button[] bar = {t1, t2};
         for (int i = 0, n = bar.length; i < n; i++)
            add(bar[i], i == 0 ? LEFT : AFTER,BOTTOM, PARENTSIZE-n,fmH*3);
      }
      catch (Exception ee)
      {
         MessageBox.showException(ee,true);
      }
   }
}
