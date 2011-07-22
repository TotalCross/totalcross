package tc.samples.ui.androidui;

import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;

public class MainMenu extends BaseContainer
{
   private ButtonMenu menu;
   
   public MainMenu()
   {
      helpMessage = "This is a sample that shows the Android user interface controls and the changes in the other controls to match the Android's look and feel. In this screen you can see the new Bar control (at the header and footer), and also the new ButtonMenu (the menu at the middle). You can drag the menu up and down.";
      transitionEffect = TRANSITION_CLOSE;
   }
   
   String[] items =
   {
      "Button",
      "Check",
      "Radio",
      "ProgressBar",
      "Edit",
      "MultiEdit",
      "ListContainer",
      "Spinner",
      "ProgressBox",
      "MessageBox",
   };

   Class[] itemClasses =
   {
      ButtonSamples.class,
      CheckSamples.class,
      RadioSamples.class,
      ProgressBarSamples.class,
      EditSamples.class,
   };
   
   public void initUI()
   {
      super.initUI(); // important!
      
      menu = new ButtonMenu(items, ButtonMenu.MULTIPLE_VERTICAL);
      menu.pressedColor = BKGCOLOR;
      
      add(menu,LEFT,TOP,FILL,FILL);
      setInfo("Click Info button for help.");
   }
   
   public void onEvent(Event e)
   {
      if (e.type == ControlEvent.PRESSED && e.target == menu)
         try
         {
            int idx = menu.getSelectedIndex();
            if (0 <= idx && idx < itemClasses.length)
            {
               BaseContainer bc = (BaseContainer)itemClasses[idx].newInstance();
               bc.show();
            }
         }
         catch (Exception ee)
         {
            MessageBox.showException(ee,true);
         }
      else
      super.onEvent(e);
   }
}