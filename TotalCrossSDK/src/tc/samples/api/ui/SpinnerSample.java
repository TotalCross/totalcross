package tc.samples.api.ui;

import tc.samples.api.*;

import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;

public class SpinnerSample extends BaseContainer
{
   private Spinner sp1,sp2;
   private Button bt;
   private Label l;
   
   public void initUI()
   {
      try
      {
         super.initUI();
         
         Spacer s;
         add(s = new Spacer(1,1),CENTER,CENTER);
         Spinner.spinnerType = Spinner.ANDROID;
         add(sp1 = new Spinner(),CENTER,BEFORE-gap,fmH*2,fmH*2, s);
         Spinner.spinnerType = Spinner.IPHONE;
         add(sp2 = new Spinner(),CENTER,AFTER+gap,fmH*2,fmH*2, s);
         add(bt = new Button("Start"),CENTER,TOP+gap,PARENTSIZE+50,PREFERRED);
         l = new Label("Note that we are blocked in a loop, like if we're downloading something from internet or other kind of processing.");
         l.autoSplit = true;
         add(l,LEFT,TOP,FILL,PREFERRED);
         l.setVisible(false);
      }
      catch (Exception ee)
      {
         MessageBox.showException(ee,true);
      }
   }
   
   public void onEvent(Event e)
   {
      if (e.type == ControlEvent.PRESSED && e.target == bt)
      {
         if (!sp1.isRunning())
         {
            bt.setVisible(false);
            l.setVisible(true);
            int end = Vm.getTimeStamp()+5000;
            while (Vm.getTimeStamp() < end)
            {
               sp1.update(); // this makes the magic that updates the spinner
               sp2.update();
               // you may do other processing here...
            }
            onRemove();
         }
      }
   }

   public void onRemove() // stop spinners at end
   {
      l.setVisible(false);
      bt.setVisible(true);
   }
}
