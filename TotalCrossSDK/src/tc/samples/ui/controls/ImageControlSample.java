package tc.samples.ui.controls;

import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.image.*;

public class ImageControlSample extends BaseContainer
{
   Image img;
   TimerEvent timer;
   boolean grow=true;
   ImageControl ic;
   
   public void initUI()
   {
      try
      {
         super.initUI();
         setTitle("Scale with ImageControl");
         add(new Label("To Natasha"),CENTER,TOP);
         img = new Image("heart.png");
         ic = new ImageControl(img);
         ic.centerImage = true;
         add(ic,LEFT,AFTER,FILL,FILL);
         timer = addTimer(25);
      }
      catch (Exception ee)
      {
         MessageBox.showException(ee,true);
      }
   }
   public void onEvent(Event e)
   {
      if (e.type == TimerEvent.TRIGGERED && timer != null && timer.triggered)
      {
         if (img.hwScaleH > 5)
            grow = false;
         img.hwScaleH = img.hwScaleW = img.hwScaleH * (grow ? 1.05 : 0.95);
         if (!grow && img.hwScaleH <= 0.5)
            grow = true;
         ic.setImage(img); // this actually just computes the center position
      }
   }
   public void onRemove()
   {
      removeTimer(timer);
      timer = null;
   }
}