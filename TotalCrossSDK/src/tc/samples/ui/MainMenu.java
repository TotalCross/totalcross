/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2014 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  This file is covered by the GNU LESSER GENERAL PUBLIC LICENSE VERSION 3.0    *
 *  A copy of this license is located in file license.txt at the root of this    *
 *  SDK or can be downloaded here:                                               *
 *  http://www.gnu.org/licenses/lgpl-3.0.txt                                     *
 *                                                                               *
 *********************************************************************************/

package tc.samples.ui;

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
      "Camera",
      "Chart",
      "Check/Radio",
      "Combo/ListBox",
      "Dyn ScrollContainer",
      "Edit",
      "Font sizes",
      "Grid",
      "ImageControl",
      "Image Animation",
      "Image book",
      "Image modifiers",
      "ListContainer",
      "MessageBox",
      "MultiButton",
      "MultiEdit",
      "Multi touch",
      "ProgressBar",
      "ProgressBox",
      "ScrollContainer",
      "Signature",
      "Spinner",
      "TabbedContainer",
      "Other controls",
   };
   
   Class[] itemClasses =
   {
      AlignedLabelsSample.class,
      ButtonSample.class,
      ButtonMenuSample.class,
      CameraSample.class,
      ChartSample.class,
      CheckRadioSample.class,
      ComboListSample.class,
      DynScrollContainerSample.class,
      EditSample.class,
      FontSample.class,
      GridSample.class,
      ImageControlSample.class,
      AnimationSample.class,
      ImageBookSample.class,
      ImageModifiersSample.class,
      ListContainerSample.class,
      MessageBoxSample.class,
      MultiButtonSample.class,
      MultiEditSample.class,
      MultitouchSample.class,
      ProgressBarSample.class,
      ProgressBoxSample.class,
      ScrollContainerSample.class,
      SignatureSample.class,      
      SpinnerSample.class,
      TabbedContainerSample.class,
      OtherControlsSample.class,
   };
   
   String[] tips = 
   {

      "Shows a way to align different text sizes with Edits",
      "Shows multi-lined buttons, buttons with images, with image and text, and a check to disable them.",
      "Shows a single row menu and also a multi-row one",
      "Shows how to take photos using the camera",
      "Shows pie, column and line charts",
      "Shows multicolored check and radio boxes",
      "Shows a ComboBox, a sorted ComboBox, and two Listbox",
      "Shows how to create ScrollContainers with thousands of items",
      "Shows the way to enter text in a single line, in several formats",
      "Shows the internal font in several sizes",
      "Shows some options available in Grid",
      "Shows hardware realtime scale using ImageControl. Click heart to change the name", 
      "Shows an animated Gif with some image transformations",
      "Shows a book with images that load and frees the images dynamically",
      "Shows an image that can be rotated, scaled, change contrast and brightness",
      "Shows a list of containers. Enter once to load 30, exit and enter again to show 3000 items",
      "Shows some MessageBox styles",
      "Shows a Single button that contains other buttons",
      "Shows an edit with more than one line, and a justified readonly one",
      "Shows how to zoom in and out using multitouch in an ImageControl", 
      "Shows a bench using a few ProgressBars",
      "Shows a MessageBox with a spinner to indicate progress",
      "Shows three ScrollContainers that can be dragged in vertical, horizontal or both directions",
      "Shows how to capture a signature, store in png and restore it",
      "Shows two spinner types",
      "Shows some container with tabs",
      "Shows other controls that doesn't belong to above ones",
   };

   BaseContainer[] itemInstances = new BaseContainer[itemClasses.length];
   
   protected String getHelpMessage()
   {
      return "This is a TotalCross "+Settings.versionStr+" sample that shows most of the user interface controls available in the SDK. In this screen you can see the Bar control (at the header and footer), and also the new ButtonMenu (the menu at the middle). You may drag the menu up and down. Device information: screen: "+Settings.screenWidth+"x"+Settings.screenHeight+", device id: "+Settings.deviceId+", font size: "+Font.NORMAL_SIZE;
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
      
      ToolTip.distY = fmH*3;
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
      BaseContainer c = itemInstances[idx] == null ? itemInstances[idx] = (BaseContainer)itemClasses[idx].newInstance() : itemInstances[idx];
      c.info = "Press Back for main menu";
      c.show();
      c.setInfo(c.info);
      if (c.isSingleCall)         
         itemInstances[idx] = null;
   }
}