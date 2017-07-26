package tc.samples.api.ui;

import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.font.*;

import tc.samples.api.*;

public class SliderSample extends BaseContainer
{
   private Label l;
   
   public void initUI()
   {
      try
      {
         super.initUI();

         add(l = new Label("", CENTER),LEFT,TOP);

         Slider sl;
         sl = new Slider(ScrollBar.HORIZONTAL); sl.setFont(Font.getFont(false,Font.NORMAL_SIZE)); 
         add(sl, LEFT+fmH, TOP+fmH*2, PARENTSIZE+50, PREFERRED); 
         sl.appId = 1; 
         sl.setLiveScrolling(true);
         
         sl = new Slider(ScrollBar.HORIZONTAL); sl.setFont(Font.getFont(false,Font.NORMAL_SIZE/2*3)); 
         add(sl, SAME, AFTER+fmH, PARENTSIZE+50, PREFERRED);
         sl.appId = 2; 
         sl.setUnitIncrement(10); 
         sl.drawTicks = true;
         
         sl = new Slider(ScrollBar.HORIZONTAL); 
         sl.setFont(Font.getFont(false,Font.NORMAL_SIZE/2*4)); 
         add(sl, SAME, AFTER+fmH, PARENTSIZE+50, PREFERRED);
         sl.appId = 3; 
         sl.invertDirection = true;
       
         sl = new Slider(ScrollBar.VERTICAL);   
         sl.setFont(Font.getFont(false,Font.NORMAL_SIZE)); 
         add(sl, PARENTSIZE+15, AFTER+fmH, PREFERRED, FONTSIZE+400);
         sl.appId = 4; 
         sl.setLiveScrolling(true);
         
         sl = new Slider(ScrollBar.VERTICAL);
         sl.setFont(Font.getFont(false,Font.NORMAL_SIZE/2*3)); 
         add(sl, PARENTSIZE+35, SAME, PREFERRED, FONTSIZE+400);
         sl.appId = 5; sl.setUnitIncrement(10); sl.drawTicks = true; 
         sl.setLiveScrolling(true);
         
         sl = new Slider(ScrollBar.VERTICAL);
         sl.setFont(Font.getFont(false,Font.NORMAL_SIZE/2*4)); 
         add(sl, PARENTSIZE+55, SAME, PREFERRED, FONTSIZE+400);
         sl.appId = 6; 
         sl.invertDirection = true;
      }
      catch (Exception ee)
      {
         MessageBox.showException(ee,true);
      }
   }

   public void onEvent(Event e)
   {
      if (e.type == ControlEvent.PRESSED && e.target instanceof Slider)
      {
         Slider s = (Slider)e.target;
         l.setText(s.appId+": "+s.getValue());
      }
   }
}
