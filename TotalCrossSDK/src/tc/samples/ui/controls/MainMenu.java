package tc.samples.ui.controls;

import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.font.*;
import totalcross.ui.gfx.*;

public class MainMenu extends BaseContainer
{
   private ButtonMenu menu;
   
   static String DEFAULT_INFO = "Click Info for help. Hold button for tip";
   String[] items =
   {
      "AlignedLabelsContainer",
      "Button",
      "ButtonMenu",
      "Check/Radio",
      "Combo/ListBox",
      "Edit",
      "ListContainer",
      "MessageBox",
      "MultiButton",
      "MultiEdit",
      "ProgressBar",
      "ProgressBox",
      "ScrollContainer",
      "Spinner",
      "TabbedContainer",
      "Scale with ImageControl",
      "Other controls",
   };
   
   String[] tips = 
   {

      "AlignedLabelsContainer shows a way to align different text sizes with Edits",
      "Button shows multi-lined buttons, buttons with images, with image and text, and a check to disable them.",
      "ButtonMenu shows a single row menu and also a multi-row one",
      "Check/Radio shows multicolored check and radio boxes",
      "Combo/ListBox shows a ComboBox, a sorted ComboBox, and two Listbox",
      "Edit shows the way to enter text in a single line, in several formats",
      "ListContainer shows a list of containers. Enter once to load 30, exit and enter again to show 3000 items",
      "MessageBox shows some MessageBox styles",
      "MultiButton shows a Single button that contains other buttons",
      "MultiEdit shows a standard MultiEdit, and a justified readonly one",
      "ProgressBar shows a bench using a few ProgressBars",
      "ProgressBox shows a MessageBox with a spinner",
      "ScrollContainer shows three ScrollContainers that can be dragged in vertical, horizontal or both directions",
      "Spinner shows two spinner types",
      "TabbedContainer shows some container with tabs",
      "Realtime scale using ImageControl", 
      "Other controls shows other controls that doesn't belong to these ones",
   };

   Class[] itemClasses =
   {
      AlignedLabelsSample.class,
      ButtonSample.class,
      ButtonMenuSample.class,
      CheckRadioSample.class,
      ComboListSample.class,
      EditSample.class,
      ListContainerSample.class,
      MessageBoxSample.class,
      MultiButtonSample.class,
      MultiEditSample.class,
      ProgressBarSample.class,
      ProgressBoxSample.class,
      ScrollContainerSample.class,
      SpinnerSample.class,
      TabbedContainerSample.class,
      ImageControlSample.class,
      OtherControlsSample.class,
   };
   
   BaseContainer[] itemInstances = new BaseContainer[itemClasses.length];
   
   public MainMenu()
   {
      helpMessage = "This is a TotalCross "+Settings.versionStr+" sample that shows the user interface controls and the changes in the other controls to match the new look and feel. In this screen you can see the new Bar control (at the header and footer), and also the new ButtonMenu (the menu at the middle). You may drag the menu up and down. Device information: screen: "+Settings.screenWidth+"x"+Settings.screenHeight+", device id: "+Settings.deviceId+", font size: "+Font.NORMAL_SIZE;
   }
   
   private void addToolTip(Control c, String text)
   {
      ToolTip t = new ToolTip(c,text);
      t.millisDelay = 500;
      t.millisDisplay = 5000;
      t.borderColor = Color.BLACK;
      t.setBackColor(0xF0F000);
   }

   public void initUI()
   {
      super.initUI(); // important!
      
      ToolTip.distY = fmH;
      menu = new ButtonMenu(items, ButtonMenu.MULTIPLE_VERTICAL);
      
      menu.pressedColor = BKGCOLOR;
      if (Math.max(Settings.screenWidth,Settings.screenHeight)/Font.NORMAL_SIZE > 30)
      {
         menu.borderGap = 100;
         menu.buttonHorizGap = menu.buttonVertGap = 200;
      }
      else menu.buttonHorizGap = 50;
      
      add(menu,LEFT,TOP,FILL,FILL);
      for (int i = 0; i < tips.length; i++)
         addToolTip(menu.getButton(i), ToolTip.split(tips[i],fm));
      if (!Settings.isOpenGL && !Settings.onJavaSE)
         menu.getButton(tips.length-2).setEnabled(false);

      setInfo(DEFAULT_INFO);

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
      itemInstances[idx].setInfo("Press Back for main menu");
      if (itemClasses[idx] == ImageControlSample.class || itemClasses[idx] == ListContainerSample.class || itemClasses[idx] == ProgressBarSample.class) // these samples will change each time it is called
         itemInstances[idx] = null;
   }
}