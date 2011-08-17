package tc.samples.ui.androidui;

import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;

public class ProgressBoxSamples extends BaseContainer
{
   ButtonMenu menu;
   
   public ProgressBoxSamples()
   {
      helpMessage = "These are ProgressBox samples in the Android user interface style. Press back to go to the main menu.";
   }
   
   public void initUI()
   {
      try
      {
         super.initUI();

         String[] items =
         {
            "Small ProgressBox (iPhone style)",
            "Big ProgressBox (iPhone style)",
            "Small ProgressBox (android style)",
            "Big ProgressBox (android style)"
         };
         
         menu = new ButtonMenu(items, ButtonMenu.SINGLE_COLUMN);
         menu.pressedColor = Color.GREEN;
         add(menu,LEFT,TOP,FILL,FILL);
         
         setInfo("Each test takes 5 seconds");
      }
      catch (Exception ee)
      {
         MessageBox.showException(ee,true);
      }
   }
   
   public void onEvent(Event e)
   {
      if (e.type == ControlEvent.PRESSED && e.target == menu)
      {
         int sel = menu.getSelectedIndex();
         Spinner.spinnerType = sel >= 2 ? Spinner.ANDROID : Spinner.IPHONE;
         String msg = sel == 0 || sel == 2 ? "Loading, please wait..." : "This device will explode in 5 seconds... throw it away!";
         ProgressBox pb = new ProgressBox("Message",msg,null);
         pb.popupNonBlocking();
         // we can't just block using Vm.sleep because it would also 
         // block a screen rotation from correctly paint the screen
         Vm.safeSleep(5000);
         pb.unpop();
         setInfo(sel == 1 || sel == 3 ? "BUM!!!!" : "Loaded");
      }
   }
}