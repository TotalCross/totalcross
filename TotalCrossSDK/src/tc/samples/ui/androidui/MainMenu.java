package tc.samples.ui.androidui;

import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.font.*;

public class MainMenu extends BaseContainer
{
   private ButtonMenu menu;
   
   public MainMenu()
   {
      helpMessage = "This is a sample that shows the Android user interface controls and the changes in the other controls to match the Android's look and feel. In this screen you can see the new Bar control (at the header and footer), and also the new ButtonMenu (the menu at the middle). You can drag the menu up and down. Device information: screen: "+Settings.screenWidth+"x"+Settings.screenHeight+", device id: "+Settings.deviceId+", font size: "+Font.getDefaultFontSize();
      transitionEffect = TRANSITION_CLOSE;
   }
   
   String[] items =
   {
      "Button",
      "Check/Radio",
      "Combo/ListBox",
      "ProgressBar",
      "Edit",
      "MultiEdit",
      "ListContainer",
      "Spinner",
      "ProgressBox",
      "MessageBox",
      "ButtonMenu",
      "PopupMenu",
      "TabbedContainer",
   };

   Class[] itemClasses =
   {
      ButtonSamples.class,
      CheckRadioSamples.class,
      ComboListSamples.class,
      ProgressBarSamples.class,
      EditSamples.class,
      MultiEditSamples.class,
      ListContainerSamples.class,
      SpinnerSamples.class,
      ProgressBoxSamples.class,
      MessageBoxSamples.class,
      ButtonMenuSamples.class,
      PopupMenuSamples.class,
      TabbedContainerSamples.class,
   };
   
   BaseContainer[] itemInstances = new BaseContainer[itemClasses.length];
   
   public void initUI()
   {
      super.initUI(); // important!
      
      menu = new ButtonMenu(items, ButtonMenu.MULTIPLE_VERTICAL);
      menu.pressedColor = BKGCOLOR;
      if (Math.max(Settings.screenWidth,Settings.screenHeight) >= 800)
      {
         menu.borderGap = 100;
         menu.buttonHorizGap = menu.buttonVertGap = 200;
      }
      else menu.buttonHorizGap = 50;
      
      add(menu,LEFT,TOP,FILL,FILL);
      setInfo("Click Info button for help.");

      String cmd = MainWindow.getCommandLine();
      if (cmd != null && cmd.startsWith("/t"))
         try 
         {
            showSample(Convert.toInt(cmd.substring(2)));
            return;
         }
         catch (Exception e) {}
   }
   
   public void onEvent(Event e)
   {
      if (e.type == ControlEvent.PRESSED && e.target == menu)
         try
         {
            int idx = menu.getSelectedIndex();
            if (0 <= idx && idx < itemClasses.length)
               showSample(idx);
         }
         catch (Exception ee)
         {
            MessageBox.showException(ee,true);
         }
   }

   private void showSample(int idx) throws Exception
   {
      if (itemInstances[idx] == null)
         itemInstances[idx] = (BaseContainer)itemClasses[idx].newInstance();
      itemInstances[idx].show();
      if (itemClasses[idx] == ListContainerSamples.class || itemClasses[idx] == ProgressBarSamples.class) // these samples will change each time it is called
         itemInstances[idx] = null;
   }
}