/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
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



package tc.samples.ui.gadgets;

import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;
import totalcross.util.*;

/** An example that shows the new user interface gadgets. */

public class UIGadgets extends MainWindow
{
   static
   {
      Settings.applicationId = "UiGd";
      Settings.closeButtonType = Settings.MINIMIZE_BUTTON;
      Settings.useNewFont = true;
      Settings.useNewFont = true;
   }

   private MenuItem miPenless,miGeoFocus,miShowKeys,miUnmovableSIP;
   private MenuBar mbar;
   private Random rand = new Random();

   public UIGadgets()
   {
      super("UI Gadgets", NO_BORDER);
      Settings.vibrateMessageBox = true; // guich@tc122_51
      if (Settings.appSettings != null) // user already selected a user interface style?
         try {setUIStyle((byte)Convert.toInt(Settings.appSettings));} catch (InvalidNumberException ine) {}
      switch (Settings.uiStyle)
      {
         case Settings.PalmOS: 
            setBorderStyle(TAB_ONLY_BORDER); 
            break;
         case Settings.Flat: 
            gradientTitleStartColor = 0x0A246A;
            gradientTitleEndColor = 0xA6CAF0;
            titleColor = Color.WHITE;
            setBorderStyle(HORIZONTAL_GRADIENT);
            break;
         case Settings.WinCE:
            setBorderStyle(RECT_BORDER);
            break;
         case Settings.Vista:
            gradientTitleStartColor = 0x0A246A;
            gradientTitleEndColor = 0xA6CAF0;
            titleColor = Color.WHITE;
            setBorderStyle(VERTICAL_GRADIENT);
            break;
         case Settings.Android:
            setBorderStyle(ROUND_BORDER);
            break;
      }
      Vm.interceptSpecialKeys(new int[]{SpecialKeys.LEFT, SpecialKeys.RIGHT, SpecialKeys.PAGE_UP, SpecialKeys.PAGE_DOWN, SpecialKeys.ACTION, SpecialKeys.FIND});
      Settings.deviceRobotSpecialKey = SpecialKeys.FIND;
   }

   public void initUI()
   {
      if (Settings.platform.equals(Settings.ANDROID))
         Vm.debug(Vm.ALTERNATIVE_DEBUG);
      MenuItem col0[] =
      {
         new MenuItem("File"),  
         new MenuItem("Minimize"),
         new MenuItem("Exit"),
         new MenuItem(),
         miShowKeys = new MenuItem("Show key codes", false),
      };
      String p = Settings.platform;
      col0[1].isEnabled = p.equals(Settings.JAVA) || p.equals(Settings.ANDROID) || p.equals(Settings.BLACKBERRY) || Settings.isWindowsDevice() || p.equals(Settings.WIN32);
         
      MenuItem col1[] =
      {
         new MenuItem("UIStyle"),
         new MenuItem("WinCE"),
         new MenuItem("PalmOS"),
         new MenuItem("Flat"),
         new MenuItem("Vista"),
         new MenuItem("Android"),
         new MenuItem(),
         miPenless = new MenuItem("Penless device",false),
         miGeoFocus= new MenuItem("Geographical focus",false),
         new MenuItem(),
         miUnmovableSIP = new MenuItem("Unmovable SIP",false),
      };
      MenuItem col2[] =
      {
         new MenuItem("Tests1"),
         new MenuItem("Standard controls"),
         new MenuItem("TabbedContainer with images"),
         new MenuItem("Masked Edit"),
         new MenuItem("Image and text buttons"),
         new MenuItem("Scaled Image button"),
         new MenuItem("Justified MultiEdit and Label"),
      };
      MenuItem col3[] =
      {
         new MenuItem("Tests2"),
         new MenuItem("Scroll Container"),
         new MenuItem("File Chooser with Tree"),
         new MenuItem("SpinList ToolTip ProgressBar"),
         new MenuItem("Drag scroll"),
         new MenuItem("AlignedLabelsContainer"),
         new MenuItem("ListContainer"),
      };
      
      setMenuBar(mbar = new MenuBar(new MenuItem[][]{col0,col1,col2,col3}));
      mbar.getMenuItem(101+Settings.uiStyle).isEnabled = false; // disable the current style
      if (Settings.keyboardFocusTraversable) // if this is a penless device, set it as marked and disable
      {
         miPenless.isChecked = true;
         miPenless.isEnabled = false;
      }
      if (Settings.unmovableSIP)
      {
         miUnmovableSIP.isChecked = true;
         miUnmovableSIP.isEnabled = false;
      }
      if (Settings.unmovableSIP)
      {
         miUnmovableSIP.isChecked = true;
         miUnmovableSIP.isEnabled = false;
      }
      String s = getCommandLine();
      int t = 0;
      if (s != null && s.toLowerCase().startsWith("/t"))
         try {t = Convert.toInt(s.substring(2));} catch (InvalidNumberException ine) {}
      switchToTest(t);
      if (Settings.uiStyle != Settings.PalmOS)
         mbar.setAlternativeStyle(Color.BLUE,Color.WHITE);
      mbar.addPressListener(new PressListener()
      {
         public void controlPressed(ControlEvent e)
         {
            int idx = 0;
            switch (mbar.getSelectedIndex())
            {
               case 1: minimize(); break;
               case 2: exit(0); break;
               case 4:
                  totalcross.sys.Vm.showKeyCodes(miShowKeys.isChecked);
                  break;
               case 101: 
               case 102: 
               case 103: 
               case 104:
               case 105:
                  Settings.appSettings = Convert.toString(mbar.getSelectedIndex()-101);
                  new MessageBox("User Interface changed","Press close to quit the\nprogram, then call it again.").popup();
                  exit(0);
                  break;
               case 107:
                  Settings.keyboardFocusTraversable = miPenless.isChecked;
                  new MessageBox("Penless","Penless is now "+(miPenless.isChecked?"enabled":"disabled")+"\nduring this running instance").popup();
                  needsPaint = true;
                  break;
               case 108:
                  Settings.keyboardFocusTraversable = Settings.geographicalFocus = miPenless.isChecked = miGeoFocus.isChecked;
                  new MessageBox("Geographical focus","Geographical focus and penless are now\n"+(miGeoFocus.isChecked?"enabled":"disabled")+" during this running instance").popup();
                  needsPaint = true;
                  break;
               case 110: 
                  Settings.unmovableSIP = Settings.virtualKeyboard = miUnmovableSIP.isChecked;
                  UIColors.shiftScreenColor = Color.getRGBEnsureRange(rand.between(0,255),rand.between(0,255),rand.between(0,255)); // random color
                  new MessageBox("Unmovable SIP",miUnmovableSIP.isChecked?"Now enabled":"Now disabled").popup();
                  break;
               case 403: idx++;
               case 402: idx++;
               case 401: idx++;
               case 306: idx++;
               case 305: idx++;
               case 304: idx++;
               case 303: idx++; 
               case 302: idx++; 
               case 301: idx++; 
               case 206: idx++; 
               case 205: idx++; 
               case 204: idx++; 
               case 203: idx++; 
               case 202: idx++; 
               case 201:
                  switchToTest(idx); break;
            }
         }
      });
   }

   private Class[] testClasses = 
      {
         StandardControls.class,
         TabbedContainerWithImages.class,
         MaskedEditTest.class,
         ImageAndTextButton.class,
         ImageButtonResolutionsScale.class,
         MultiEditTest.class,
         ScrollContainerTest.class,
         FileChooserTest.class,
         SpinToolColor.class,
         DragScroll.class,
         AlignedLabelsTest.class,
         ListContainerTest.class,
      };
   
   private Container[] testInstances = new Container[testClasses.length];
   private int testMenuItems[] = {201,202,203,204,205,206,301,302,303,304,305,306};
   
   private void switchToTest(int idx)
   {
      try
      {
         nextTransitionEffect = (idx & 1) == 1 ? TRANSITION_OPEN : TRANSITION_CLOSE;
         setTitle(mbar.getMenuItem(testMenuItems[idx]).caption);
         if (testInstances[idx] == null)
            testInstances[idx] = (Container)testClasses[idx].newInstance();
         testInstances[idx].swapToTopmostWindow();
         // disable the used menuitem
         for (int i = 0; i < testMenuItems.length; i++)
            mbar.getMenuItem(testMenuItems[i]).isEnabled = idx != i;
      }
      catch (Exception e)
      {
         MessageBox.showException(e,true);
      }
   }
   
   // Called by the system to pass events to the application.
   public void onEvent(Event event)
   {
      if (event.type == KeyEvent.SPECIAL_KEY_PRESS && ((KeyEvent)event).key == SpecialKeys.POWER_ON)
         new MessageBox("Attention","Device has powered on.").popup();
   }
   
   public void onMinimize()
   {
      if (testInstances[0] != null)
      {
         Label lStatus = ((StandardControls)testInstances[0]).lStatus;
         lStatus.setText("UIGadgets minimized");
         lStatus.repaintNow();
      }
   }
   
   public void onRestore()
   {
      if (testInstances[0] != null)
      {
         Label lStatus = ((StandardControls)testInstances[0]).lStatus;
         lStatus.setText(lStatus.getText() + " and restored");
         lStatus.repaintNow();
      }
   }
}
