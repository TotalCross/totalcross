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

import totalcross.io.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.font.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;
import totalcross.unit.*;
import totalcross.util.*;

/** An example that shows the new user interface gadgets. */

public class UIGadgets extends MainWindow
{
   static
   {
      totalcross.sys.Settings.applicationId = "UiGd";
      totalcross.sys.Settings.closeButtonType = Settings.MINIMIZE_BUTTON;
   }
   private Button btnClock;
   private Button btnInput;
   private Button btnMessage1,btnMessage2;
   private MenuItem miPenless,miGeoFocus,miShowKeys,miUnmovableSIP;
   private Check ch;
   private Radio rdEnab,rdDisab;
   private Edit ed;
   private ComboBox cb1,cb2;
   private TabbedContainer tp,tp2;
   private Button btnBench;
   private Label lStatus;
   private MenuBar mbar;
   private Slider sb1;
   private ScrollBar sb2;
   private MultiListBox lbox;
   private ComboBoxEditable cbe;
   private boolean initialized;
   private Random rand = new Random();
   private static boolean isAndroid = Settings.platform.equals(Settings.ANDROID);

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

   private int samples[] = {201,202,203,204,205,206,301,302,303,304,305,306};
   
   public void initUI()
   {
      if (isAndroid)
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
      switchToTest(samples[t]);
      if (Settings.uiStyle != Settings.PalmOS)
         mbar.setAlternativeStyle(Color.BLUE,Color.WHITE);
      mbar.addPressListener(new PressListener()
      {
         public void controlPressed(ControlEvent e)
         {
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
                  if (btnMessage1 != null && btnMessage1.isVisible())
                     btnMessage1.requestFocus();
                  else
                  if (btnMessage2 != null && btnMessage2.isVisible())
                     btnMessage2.requestFocus();
                  repaint();
                  break;
               case 108:
                  Settings.keyboardFocusTraversable = Settings.geographicalFocus = miPenless.isChecked = miGeoFocus.isChecked;
                  new MessageBox("Geographical focus","Geographical focus and penless are now\n"+(miGeoFocus.isChecked?"enabled":"disabled")+" during this running instance").popup();
                  repaint();
                  break;
               case 110: 
                  Settings.unmovableSIP = Settings.virtualKeyboard = miUnmovableSIP.isChecked;
                  UIColors.shiftScreenColor = Color.getRGBEnsureRange(rand.between(0,255),rand.between(0,255),rand.between(0,255)); // random color
                  new MessageBox("Unmovable SIP",miUnmovableSIP.isChecked?"Now enabled":"Now disabled").popup();
                  break;
               case 201: 
               case 202: 
               case 203: 
               case 204: 
               case 205: 
               case 206: 
               case 301: 
               case 302: 
               case 303: 
               case 304:
               case 305:
               case 306:
               case 401:
               case 402:
               case 403:
                  switchToTest(mbar.getSelectedIndex()); break;
            }
         }
      });
   }
   
   private void testStandardControls() throws ImageException
   {
      String []items = {"one","two","three","four","five","six","seven","eight","nine","one zero","one one","one two","one three","one four","one five","one six","one seven","one eight","one nine","two zero","two one","two two","two three","two four","two five","two six"};
      String []items2 = {"one","two","three"};
      //Button.commonGap = 1;
      btnMessage1 = new Button(" Message ");
      btnMessage1.setBorder(Button.BORDER_3D_VERTICAL_GRADIENT);
      add(btnMessage1,LEFT+3,TOP+3);
      btnMessage2 = new Button(" Message ");
      btnMessage2.setBorder(Button.BORDER_3D_HORIZONTAL_GRADIENT);
      add(btnMessage2,SAME,SAME,SAME,SAME);
      btnMessage2.setVisible(false);
      add(btnInput = new Button("Input"),CENTER,CENTER_OF);
      add(btnBench = new Button("Bench"),RIGHT-3,CENTER_OF);
      //Button.commonGap = 0;
      add(lStatus = new Label("",CENTER), LEFT,AFTER);
      lStatus.setHighlighted(true);
      add(new Ruler(),LEFT,AFTER+2, FILL, PREFERRED+4);
      add(ch = new Check("Enable:"),LEFT,AFTER+1); if (uiAndroid) ch.checkColor = Color.CYAN;     ch.setChecked(true);
      RadioGroupController rg = new RadioGroupController();
      add(rdEnab  = new Radio("Enable",rg),AFTER+5,SAME); rdEnab.setChecked(true);
      add(rdDisab = new Radio("Disable",rg),AFTER+5,SAME);
      // create Clock button
      Image clock = new Image(fm.charWidth('@'),(rdEnab.getHeight()+1)/2*2);
      int xx = clock.getWidth();
      int yy = clock.getHeight();
      Graphics g = clock.getGraphics();
      g.backColor = Color.WHITE; g.fillRect(0,0,xx,yy);
      g.foreColor = Color.BLUE;
      g.drawCircle(xx/2,yy/2,xx/2);
      g.drawLine(xx/2,yy/2,xx,yy/2);
      g.drawLine(xx/2,yy/2,xx/2,yy/3);
      btnClock = new Button(clock);
      btnClock.setBorder(Button.BORDER_NONE);
      add(btnClock, RIGHT, SAME-2,PREFERRED,PREFERRED);
      
      add(lbox = new MultiListBox(items2),LEFT+2,AFTER+3);
      lbox.setOrderIsImportant(true);
      add(ed = new Edit("000000000000"), AFTER+2, SAME);
      String[] items3 = {"Ana","Barbara","Raul","Marcelo","Eduardo","Denise","Michelle","Guilherme","Vera","Dulce","Leo","Andre","Gustavo","Cathy","Renato","Zelia","Helio"};
      cbe = new ComboBoxEditable(items3);
      cbe.qsort();
      cbe.setAutoAdd(true,true); // auto add new items and keep it sorted
      add(cbe, AFTER+2,SAME,Settings.screenWidth < 240 ? FILL : PREFERRED,PREFERRED);
      add(cb1 = new ComboBox(new MultiListBox(items)),SAME,AFTER+5,ed);
      add(cb2 = new ComboBox(new String[]{"no border","rect","round","tab","tab only","h grad","v grad"}),AFTER+3,SAME);
      cb2.enableHorizontalScroll();
      cb2.setSelectedIndex(getBorderStyle());

      Edit e;
      add(tp2 = new TabbedContainer(new String[]{"Curr.","Date","Pass","Pass all"}));
      tp2.activeTabBackColor = Color.darker(backColor);
      tp2.setType(TabbedContainer.TABS_BOTTOM); // must set the properties before calling setRect
      tp2.setRect(LEFT,BOTTOM,SCREENSIZE+60,PREFERRED+ed.getPreferredHeight()+fmH/2,lbox);
      tp2.getContainer(0).add(e = new Edit("999999.99"), CENTER,CENTER); e.setMode(Edit.CURRENCY); if (uiAndroid) e.setKeyboard(Edit.KBD_NUMERIC);
      tp2.getContainer(1).add(e = new Edit("99/99/9999"), CENTER,CENTER); e.setMode(Edit.DATE);
      tp2.getContainer(2).add(e = new Edit("999999"), CENTER,CENTER); e.setMode(Edit.PASSWORD);
      tp2.getContainer(3).add(e = new Edit("999999"), CENTER,CENTER); e.setMode(Edit.PASSWORD_ALL);

      add(tp = new TabbedContainer(new String[]{"Normal","Btn","Check"}), LEFT,AFTER+2,SCREENSIZE+60,FIT-fmH/2,lbox);
      tp.pressedColor = Color.BRIGHT;
      tp.getContainer(0).add(new PushButtonGroup(new String[]{"one","two","three","four","five","six"},false,-1,-1,4,2,true,PushButtonGroup.NORMAL),CENTER,CENTER);
      tp.getContainer(1).add(new PushButtonGroup(items2,false,-1,-1,4,0,false,PushButtonGroup.BUTTON),CENTER,CENTER);
      tp.getContainer(2).add(new PushButtonGroup(items2,false,-1,-1,4,0,true,PushButtonGroup.CHECK),CENTER,CENTER);

      add(sb1 = new Slider(ScrollBar.HORIZONTAL),RIGHT, BOTTOM-2, SCREENSIZE+30, PREFERRED, lbox);
      sb1.drawTicks = true;
      sb1.setLiveScrolling(true);
      sb1.setValues(1,1,1,6);

      add(sb2 = new ScrollBar(ScrollBar.VERTICAL), RIGHT, BEFORE-4, PREFERRED, SCREENSIZE+30);
      sb2.setVisibleItems(10);
      sb2.setValues(1,1,1,6);

      btnInput.setBackColor(0x2DDF00);
      tp.setBackForeColors(0x147814, 0x00A000);
      tp.setCaptionColor(Color.GREEN);
      tp.getContainer(0).setBackColor(0x409B00);
      tp.getContainer(1).setBackColor(0xF68009);
      tp.getContainer(2).setBackColor(0x4200CA);
      tp.useOnTabTheContainerColor = true;
      lbox.setBackForeColors(0xDCC8A0, 0x782850);
      ed.setForeColor(Color.RED);
      ed.setBackColor(0xFFC896);
      sb1.setBackColor(0x64C8FF);
      cbe.setForeColor(0x0000F0);
      rdEnab.setForeColor(0x0000F0);
      rdDisab.setForeColor(0x0000F0);
      cb1.setBackForeColors(0xC88CA0, 0x3C2850);
      cb2.setBackForeColors(0x8CC8A0, 0x285050);
      tp2.setCaptionColor(0x0028FF);
      // change the fore color of some ListBox items. See also ListBox.ihtBackColors.
      IntHashtable htf = new IntHashtable(1);
      htf.put(0,Color.RED);
      htf.put(1,Color.GREEN);
      htf.put(2,Color.BLUE);
      lbox.ihtForeColors = htf;
      // change he MenuBar to use the alernative style
      if (Settings.uiStyle != Settings.PalmOS)
         mbar.setAlternativeStyle(Color.BLUE,Color.WHITE);
         
      if (Settings.uiStyle == Settings.Vista) // guich@tc126_25
      {
         setTextShadowColor(Color.BLACK);
         btnInput.setTextShadowColor(BRIGHTER_BACKGROUND);
         tp.setTextShadowColor(DARKER_BACKGROUND);
         lbox.setTextShadowColor(DARKER_BACKGROUND);
         ed.setTextShadowColor(DARKER_BACKGROUND);
         cbe.setTextShadowColor(Color.WHITE);
         mbar.setTextShadowColor(DARKER_BACKGROUND);
         cb1.setTextShadowColor(BRIGHTER_BACKGROUND);
         cb2.setTextShadowColor(BRIGHTER_BACKGROUND);
      }

      if (!initialized)
      {
         initialized = true;
         if (Settings.appSecretKey == null) // display the marquee only once per 5 runs.
         {
            Settings.appSecretKey = "1";
            lStatus.setMarqueeText("Click title to see the menu options and also be able to select other user interface tests.", 100, 1, -5);
         }
         else 
         {
            Settings.appSecretKey += "1";
            if (Settings.appSecretKey.length() >= 5)
               Settings.appSecretKey = null;
         }
      }
   }

   private boolean standard;
   private void switchToTest(int idx)
   {
      try
      {
         nextTransitionEffect = (idx & 1) == 1 ? TRANSITION_OPEN : TRANSITION_CLOSE;
         removeAll();
         setTitle(mbar.getMenuItem(idx).caption);
         standard = false;
         switch (idx)
         {
            case 201: testStandardControls();            standard = true; break;
            case 202: testTabbedContainerWithImages();   break;
            case 203: testMaskedEdit();                  break;
            case 204: testImageAndTextButton();          break;
            case 205: testImageButtonResolutionsScale(); break;
            case 206: testMultiEdit();                   break;
            case 301: testScrollContainer();             break;
            case 302: testFileChooser();                 break;
            case 303: testSpinToolColor();               break;
            case 304: testDragScroll();                  break;
            case 305: testLabelContainer();              break;
            case 306: testListContainer();               break;
         }
         // disable the used menuitem
         for (int i = 0; i < samples.length; i++)
            mbar.getMenuItem(samples[i]).isEnabled = (idx != samples[i]);
      }
      catch (Exception e)
      {
         MessageBox.showException(e,true);
      }
   }
   
   // Called by the system to pass events to the application.
   public void onEvent(Event event)
   {
      try
      {
         if (event.type == KeyEvent.SPECIAL_KEY_PRESS && ((KeyEvent)event).key == SpecialKeys.POWER_ON)
            new MessageBox("Attention","Device has powered on.").popup();
         if (!standard)
            return;
         if (event instanceof UIRobotEvent)
            lStatus.setMarqueeText(event.type == UIRobotEvent.ROBOT_SUCCEED ? "Robot succeed" : "Robot failed: "+((UIRobotEvent)event).failureReason, 100,1,-5);
         else
         if (event.type == ControlEvent.PRESSED)
         {
            if (event.target == btnClock)
            {
               TimeBox tb = new TimeBox();
               tb.popup();
               lStatus.setText(tb.getTime().toString());
            }
            else
            if (event.target == cb2 && cb2.getSelectedIndex() >= 0)
            {
               setBorderStyle((byte)cb2.getSelectedIndex());
               removeAll();
               testStandardControls();
            }
            else
            if (event.target == btnMessage1 || event.target == btnMessage2)
            {
               boolean b = btnMessage1.isVisible(); // swap buttons
               btnMessage1.setVisible(!b);
               btnMessage2.setVisible(b);
               
               String []btns = { "Bárbara", "Celine" };
               MessageBox mb = new MessageBox("Hi","Who is more beaultiful?", btns);
               mb.popup();
               String s;
               lStatus.setText(s = "The winner is: "+btns[mb.getPressedButtonIndex()]);
               setDeviceTitle(s);
            }
            else
            if (event.target == btnBench)
            {
               Vm.gc();
               int repaintCount = 30;
               int ini = Vm.getTimeStamp();
               for (int i =0; i < repaintCount; i++)
               {
                  enableButtons((i % 2) == 0);
                  repaintNow();
                  repaintNow();
               }
               int fim = Vm.getTimeStamp();
               lStatus.setText("Elapsed: "+(fim-ini)+"ms");
               Vm.debug("Elapsed: "+(fim-ini)+"ms");
               enableButtons(true);
            }
            else
            if (event.target == btnInput)
            {
               InputBox id = new InputBox("Attention","Please type your name","");
               id.popup();
               if (id.getPressedButtonIndex() == 0)
                  lStatus.setText(id.getValue());
            }
            else
            if (event.target == ch)
            {
               rdEnab.setEnabled(ch.isChecked());
               rdDisab.setEnabled(ch.isChecked());
               tp.setEnabled(2, ch.isChecked());
            }
            else
            if (event.target == rdEnab || event.target == rdDisab)
               enableButtons(rdEnab.isChecked());
            else
            if (event.target instanceof PushButtonGroup && !lStatus.isMarqueeRunning())
            {
               PushButtonGroup pbg = (PushButtonGroup)event.target;
               lStatus.setText(pbg.getSelectedItem()+" - "+pbg.getSelectedIndex());
            }
            else
            if (event.target == sb1 || event.target == sb2)
            {
               int value = ((ScrollBar)event.target).getValue();
               sb1.setValue(value);
               sb2.setValue(value);
            }
            else
            if (event.target instanceof MultiListBox)
               lStatus.setText("Last selected: "+((MultiListBox)event.target).getLastSelectedItem());
            else
            if (event.target == tp)
            {
               String s = null;
               switch (tp.getActiveTab())
               {
                  case 0: s = "normal type: only one button can be selected at a time"; break;
                  case 1: s = "button type: the button will be selected and unselected immediately, acting like a real button"; break;
                  case 2: s = "check type: one click in the button will select it and another click will unselect it. However, only one button can be selected at a time"; break;
               }
               lStatus.setMarqueeText(s, 100, 1, -5);
            }
         }
      }
      catch (Exception e)
      {
         MessageBox.showException(e,true);
      }
   }
   
   public void onMinimize()
   {
      if (standard)
      {
         lStatus.setText("UIGadgets minimized");
         lStatus.repaintNow();
      }
   }
   
   public void onRestore()
   {
      if (standard)
      {
         lStatus.setText(lStatus.getText() + " and restored");
         lStatus.repaintNow();
      }
   }

   private void enableButtons(boolean b)
   {
      btnInput.setEnabled(b);
      btnMessage1.setEnabled(b);
      btnMessage2.setEnabled(b);
      btnBench.setEnabled(b);
      ch.setEnabled(b);
      ed.setEnabled(b);
      cb1.setEnabled(b);
      cb2.setEnabled(b);
      tp.setEnabled(b);
      tp2.setEnabled(b);
      sb1.setEnabled(b);
      sb2.setEnabled(b);
      lbox.setEnabled(b);
      lStatus.setEnabled(b);
      cbe.setEnabled(b);
   }
   
   public void testDragScroll()
   {
      ScrollContainer sc;
      MultiEdit me;

      add(new Label("Click and drag over the controls"),LEFT,TOP);

      add(sc = new ScrollContainer());

      sc.setBorderStyle(BORDER_SIMPLE);
      sc.setRect(LEFT + 10, AFTER + 10, FILL - 20, FILL - 20);

      ListBox lb = new ListBox(new String[]
      {
          "Version is " + Settings.versionStr,
          "Platform is " + Settings.platform
      });
      sc.add(lb);
      lb.setRect(0, 0, sc.getWidth()-20 , sc.getClientRect().height);

      me = new MultiEdit(10,5);
      sc.add(me, AFTER, TOP, SAME,SAME);
      me.setEditable(false);
      me.setText("SuperWaba interprets Java Bytecodes. TotalCross uses a proprietary set of bytecodes to improve program´s security and performance: TotalCross is about two times faster than SuperWaba. The translation between the java bytecodes to our opcodes is done automatically when the application is deployed. Regarding security, using SuperWaba is very easy to recover the sources from the application's PDB file. We can extract the .class files from the PDB and then decompile them to the .java files. In TotalCross this is IMPOSSIBLE: there are no decompilers. So, don't forget to take backups of your source files, because it will be impossible to recover them. Don't trust developers, trust only your set of backups!" );

      String []items = // taken from HelloWorld
      {
         "Version is " + Settings.versionStr,
         "Platform is " + Settings.platform,
         "User is " + Settings.userName,
         "Pen is " + (Settings.keyboardFocusTraversable ? "missing" : "available"),
         "Vistual keyboard is " + Settings.virtualKeyboard,
         "Screen is " + Settings.screenWidth + "x" + Settings.screenHeight,
         "Screen bpp is " + Settings.screenBPP,
         "timeZone is " + Settings.timeZone,
         "dateFormat is " + Settings.dateFormat,
         "dateSeparator is " + Settings.dateSeparator,
         "decimalSeparator is " + Settings.decimalSeparator,
         "thousandsSeparator is " + Settings.thousandsSeparator,
         "timeSeparator is " + Settings.timeSeparator,
         "daylightSavings is " + Settings.daylightSavings,
         "is24Hour is " + Settings.is24Hour,
         "weekStart is " + Settings.weekStart,
         "Battery is at " + Vm.getRemainingBattery() + "%",
         "Free memory is at " + Vm.getFreeMemory(),
         "Rom serial number is " + Settings.romSerialNumber,
         "Rom version is " + Settings.romVersion,
         "Device id is " + Settings.deviceId,
         "App path is " + Settings.appPath,
         "Version is " + Settings.versionStr,
         "Platform is " + Settings.platform,
         "User is " + Settings.userName
      };
      lb = new ListBox(items);
      sc.add(lb);
      lb.enableHorizontalScroll();
      lb.setRect(AFTER, TOP, SAME,SAME);
   }

   private void addToolTip(Control c, String text)
   {
      ToolTip t = new ToolTip(c,text);
      t.millisDelay = 500;
      t.millisDisplay = 5000;
      t.borderColor = Color.BLACK;
      t.setBackColor(0xF0F000);
   }

   private void testSpinToolColor()
   {
      final TimerEvent pbte;
      Button btnChooseColor;
      MultiEdit me;
      final Control c;
      final ProgressBar pbh = new ProgressBar();
      pbh.max = 50;
      pbh.highlight = true;
      pbh.suffix = " of "+pbh.max;
      pbh.textColor = 0xAAAA;
      add(pbh,LEFT+2,TOP+2,FILL-2,PREFERRED);
      // endless ProgressBar
      final ProgressBar pbe = new ProgressBar();
      pbe.max = width/4; // max-min = width of the bar
      pbe.setEndless();
      pbe.setBackColor(Color.YELLOW);
      pbe.setForeColor(Color.ORANGE);
      pbe.prefix = "Loading, please wait...";
      add(pbe,LEFT+2,AFTER+2,FILL-2,PREFERRED);
      final ProgressBar pbzh = new ProgressBar();
      pbzh.max = 50;
      pbzh.drawText = false;
      pbzh.setBackForeColors(Color.DARK,Color.RED);
      add(pbzh,LEFT+2,AFTER+2,FILL-2,fmH/2);
      
      add(btnChooseColor = new Button("Choose new background color"),LEFT,AFTER+2);
      addToolTip(btnChooseColor, ToolTip.split("Click this button to open a ColorChooserBox where you can choose a new back color",fm));
      add(c = new SpinList(new String[]{"Today","Day [1,31]"}),LEFT,AFTER+10);
      addToolTip(c, "This is a SpinList");

      final Label l;
      add(l = new Label("Click and hold in controls for a tooltip"),CENTER,BOTTOM);
      
      final ProgressBar pbv = new ProgressBar();
      pbv.vertical = true;
      pbv.max = 50;
      pbv.suffix = "";
      pbv.textColor = Color.BLUE;
      pbv.setBackColor(Color.CYAN);
      pbv.setForeColor(Color.GREEN);
      
      final ProgressBar pbzv = new ProgressBar();
      pbzv.vertical = true;
      pbzv.max = 50;
      pbzv.drawText = false;
      pbzv.setBackForeColors(Color.RED,Color.DARK);
      
      add(me = new MultiEdit(), LEFT,SAME,FILL-pbv.getPreferredWidth()-fmH/2-4,FIT, c);
      me.setText("ToolTip is not supported in the MultiEdit control.");

      add(pbv,RIGHT,SAME,PREFERRED,SAME);
      add(pbzv,BEFORE-2,SAME,fmH/2,SAME);
      
      btnChooseColor.addPressListener(new PressListener()
      {
         public void controlPressed(ControlEvent e)
         {
            ColorChooserBox ccb = new ColorChooserBox(getBackColor());
            ccb.popup();
            if (ccb.choosenColor != -1)
            {
               setBackColor(ccb.choosenColor);
               l.setBackColor(ccb.choosenColor);
               c.setBackColor(ccb.choosenColor);
               repaint();
            }
         }
      });
      pbte = pbh.addTimer(100);
      pbh.addTimerListener(new TimerListener()
      {
         int orig = pbh.getForeColor();
         public void timerTriggered(TimerEvent e)
         {
            Window w = getParentWindow();
            if (w == null) // sample removed? stop timer
               removeTimer(pbte);
            else
            if (w.isTopMost()) // update only if our window is the one being shown
            {
               int v = pbh.getValue();
               v = (v+1) % (pbh.max+1);
               Window.enableUpdateScreen = false; // since each setValue below updates the screen, we disable it to let it paint all at once at the end
               pbh.setValue(v);
               pbv.setValue(v);
               pbe.setValue(5); // increment value
               pbzh.setValue(v);
               pbzv.setValue(v);
               // change the color at each step
               if (Settings.uiStyle != Settings.Android)
               {
                  int nc = Color.brighter(pbh.getForeColor(),5);
                  if (v == 0)
                     nc = orig;
                  pbh.setForeColor(nc);
               }
               Window.enableUpdateScreen = true;
               repaintNow();
            }
         }
      });
   }

   private void testTabbedContainerWithImages()
   {
      try
      {
         Image[] images =
         {
            new Image("tab0.gif"),
            new Image("tab1.gif"),
            new Image("tab2.gif"),
            new Image("tab3.gif"),
            new Image("tab4.gif"),
         };
         final Check ch;
         final TabbedContainer tp;
         add(ch = new Check("Disable tabs 1 and 3"), LEFT,TOP+2);
         tp = new TabbedContainer(images,0);
         if (uiAndroid)
            tp.setBackColor(Color.darker(backColor,32));
         tp.activeTabBackColor = Color.ORANGE;
         add(tp, LEFT+5,AFTER+5,FILL-5,FILL-5);
         tp.getContainer(3).setBackColor(Color.CYAN);
         tp.useOnTabTheContainerColor = true;
         ch.addPressListener(new PressListener()
         {
            public void controlPressed(ControlEvent e)
            {
               tp.setEnabled(1,!ch.isChecked());
               tp.setEnabled(3,!ch.isChecked());
            }
         });
      }
      catch (Exception e)
      {
         MessageBox.showException(e, false);
      }
   }

   private void testFileChooser()
   {
      final Button btn,btn2;
      final Label l;
      final Check ch;
      add(btn = new Button("Choose file"),CENTER,CENTER);
      add(ch = new Check("Multiple selection"),CENTER,BEFORE-20);
      add(btn2 = new Button("Delete file"),CENTER,AFTER+20,btn);
      btn2.setEnabled(false);
      add(l = new Label(),LEFT,BOTTOM);
      btn.addPressListener(
         new PressListener()
         {
            public void controlPressed(ControlEvent e)
            {
               try
               {
                  FileChooserBox fcb = new FileChooserBox(null);
                  fcb.multipleSelection = ch.isChecked(); // guich@tc115_4
                  fcb.mountTree("device/",1);
                  fcb.popup();
                  String s = fcb.getAnswer();
                  if (s == null)
                     l.setText("Cancelled");
                  else
                  if (fm.stringWidth(s) > getWidth())
                     l.setMarqueeText(s, 100, 1, -8);
                  else
                     l.setText(s);
                  btn2.setEnabled(s != null);
               }
               catch (Exception ee)
               {
                  MessageBox.showException(ee,true);
               }
            }
         });
      btn2.addPressListener(new PressListener()
      {
         public void controlPressed(ControlEvent e)
         {
            try
            {
               String s = l.getText();
               new File(s,File.DONT_OPEN,1).delete();
            }
            catch (Exception ee)
            {
               MessageBox.showException(ee,false);
            }
         }
      });
   }

   private void testScrollContainer()
   {
      Button b;
      int hh = height/3-20-5-5;
      ScrollContainer sc;
      // a ScrollContainer with both ScrollBars
      add(sc = new ScrollContainer());
      sc.setBorderStyle(BORDER_SIMPLE);
      sc.setRect(LEFT+10,TOP+10,FILL-20,hh);
      int xx = new Label("Name99").getPreferredWidth()+2; // edit's alignment
      for (int i =0; i < 50; i++)
      {
         sc.add(new Label("Name"+i),LEFT,AFTER);
         sc.add(new Edit("@@@@@@@@@@@@@@"),xx,SAME);
         if (i % 3 == 0) sc.add(new Button("Go"), AFTER+2,SAME,PREFERRED,SAME);
      }
      // a ScrollContainer with horizontal ScrollBar disabled
      add(sc = new ScrollContainer(false,true));
      sc.setBorderStyle(BORDER_LOWERED);
      sc.setRect(SAME, AFTER+5, SAME, SAME);
      for (int i =0; i < 50; i++)
      {
         sc.add(new Label("Name"+i),LEFT,AFTER);
         sc.add(b = new Button("Go"), RIGHT,SAME,PREFERRED,SAME);
         sc.add(new Edit(""),xx,SAME,FIT-2,PREFERRED,b); // fit
      }
      // a ScrollContainer with vertical ScrollBar disabled
      add(sc = new ScrollContainer(true,false));
      sc.setBorderStyle(BORDER_RAISED);
      sc.setRect(SAME, AFTER+5, SAME, SAME);
      int n = hh / (Edit.prefH+fmH) - 1;
      for (int i =0; i < n; i++)
      {
         sc.add(new Label("Name"+i),LEFT,AFTER);
         sc.add(new Edit("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@"),xx,SAME); // fit
         sc.add(new Button("Go"), AFTER,SAME,PREFERRED,SAME);
      }
   }

   private void testImageButtonResolutionsScale()
   {
      String[] imageNames = {"clear.gif", "go.gif"};
      int imgRes = 320;
      int targetRes[] = {320, 240, 176, 160};
      int backColor = getBackColor();
      int coordX = LEFT, k=0;
      final Button []btns = new Button[2*4];

      try
      {
         for (int i = imageNames.length - 1; i >= 0; i--)
         {
            Image img = new Image(imageNames[i]);
            int imgWidth = img.getWidth();
            int coordY = TOP+5;

            for (int j = 0; j < targetRes.length; j++)
            {
               double factor = (double) targetRes[j] / (double) imgRes;
               Image img2 = img.smoothScaledBy(factor, factor, backColor);
               Button btn = btns[k++] = new Button(img2);
               btn.setBorder(Button.BORDER_NONE);
               add(btn, j == 0 ? coordX+5 : CENTER_OF, coordY);
               coordY += img2.getHeight() + 5;
            }
            coordX += imgWidth + 5;
         }
         final Check c = new Check("Enabled");
         add(c, RIGHT,BOTTOM);
         c.setChecked(true);
         c.addPressListener(new PressListener()
         {
            public void controlPressed(ControlEvent e)
            {
               for (int i =0; i < btns.length; i++)
                  btns[i].setEnabled(c.isChecked());
            }
         });
      }
      catch (Exception e)
      {
         MessageBox.showException(e, true);
      }
   }

   private void testMultiEdit()
   {
      add(new Label("Non-editable MultiEdit:"),LEFT,TOP+2);
      String s = "James Robert Baker (1946-1997) was an American author of sharply satirical, " +
            "predominantly gay-themed transgressional fiction. A native Californian, his work " +
            "is set almost entirely in Southern California. After graduating from UCLA, he began " +
            "his career as a screenwriter, but became disillusioned and started writing novels " +
            "instead. Though he garnered fame for his books Fuel-Injected Dreams and Boy Wonder," +
            "after the controversy surrounding publication of his novel, Tim And Pete, he faced " +
            "increasing difficulty having his work published. According to his life partner, this " +
            "was a contributing factor in his suicide. Baker's work has achieved cult status in the " +
            "years since his death, and two additional novels have been posthumously published. " +
            "First-edition copies of his earlier works have become collector's items. One of his " +
            "novels was filmed (though it was not a financial success) and two others have been " +
            "optioned for the movies, though they have not been produced.";
      MultiEdit mEdit;
      mEdit = new MultiEdit("",6,1);
      mEdit.drawDots = (false);
      mEdit.justify = true;
      mEdit.setEditable(false);
      mEdit.hasCursorWhenNotEditable = false;
      add(mEdit,LEFT,AFTER+2);
      mEdit.setText(s); //eventually
      mEdit.requestFocus();

      Label l;
      add(new Label("Label:"),LEFT,AFTER+4);
      add(l = new Label(Convert.insertLineBreak(Settings.screenWidth-20, fm, s)));
      l.align = FILL;
      l.setRect(LEFT,AFTER+2,FILL-20,FILL-2);
      l.backgroundType = Label.VERTICAL_GRADIENT_BACKGROUND;
      l.firstGradientColor = Color.YELLOW;
      l.secondGradientColor = Color.RED;
   }

   void addbtn(int color, int xpos, int ypos) throws Exception
   {
      Button btn = new Button("Rect", new Image("buttontemplate.png"), CENTER, 6);
      btn.setBackColor(backColor);
      btn.borderColor3DG = color;
      btn.setTextShadowColor(Color.BLACK);
      btn.setForeColor(color);
      btn.setFont(font.asBold());
      btn.setBorder(Button.BORDER_GRAY_IMAGE);
      add(btn,xpos,ypos);
   }
   
   private void testImageAndTextButton()
   {
      Button btn;
      try
      {
         Image img = new Image("imgbut.png").smoothScaledFromResolution(320, backColor);
         Font f = Font.getFont(true, Font.NORMAL_SIZE+2);

         Button.commonGap = 2;
         btn = new Button("Search", img, TOP, 8);
         btn.setFont(f);
         add(btn,LEFT+5,TOP+5);

         btn = new Button("Search", img, BOTTOM, 8);
         btn.setFont(f);
         add(btn,LEFT+5,AFTER+5);

         btn = new Button("Search", img, LEFT, 8);
         btn.setFont(f);
         add(btn,LEFT+5,AFTER+15);

         btn = new Button("Search", img, RIGHT, 8);
         btn.setFont(f);
         add(btn,LEFT+5,AFTER+5);
         Button.commonGap = 0;

         btn = new Button(" Horizontal Gradient ");
         btn.setForeColor(0xEEEEEE);
         btn.setTextShadowColor(Color.BLACK);
         btn.setBorder(Button.BORDER_3D_HORIZONTAL_GRADIENT);
         add(btn, RIGHT-2,TOP+5,PREFERRED,PREFERRED+10);
         
         btn = new Button(" Vertical Gradient ");
         btn.setForeColor(0xEEEEEE);
         btn.setTextShadowColor(Color.BLACK);
         btn.setBorder(Button.BORDER_3D_VERTICAL_GRADIENT);
         add(btn, RIGHT_OF,AFTER+5,PREFERRED,PREFERRED+10);

         addbtn(0xFF0000,RIGHT-5,AFTER+5);
         addbtn(0x00FF00,BEFORE-5,SAME);
         addbtn(0x0000FF,BEFORE-5,SAME);
         addbtn(0xFFFF00,RIGHT-5,AFTER+5);
         addbtn(0x00FFFF,BEFORE-5,SAME);
         addbtn(0xFF00FF,BEFORE-5,SAME);
         
         btn = new Button("This is a multi-lined\ntext button");
         add(btn, RIGHT,AFTER+5);

         final Check c = new Check("Enabled");
         add(c, RIGHT-5,BOTTOM-3);
         c.setChecked(true);
         c.addPressListener(new PressListener()
         {
            public void controlPressed(ControlEvent e)
            {
               Control []btns = getChildren();
               for (int i = btns.length; --i >= 0;)
                  if (btns[i] instanceof Button)
                     btns[i].setEnabled(c.isChecked());
            }
         });
         
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   private void testMaskedEdit()
   {
      Edit ed;
      add(new Label("Currency masked edit:"),LEFT,TOP);
      ed = new Edit("999.999.999,99");
      ed.setMode(Edit.CURRENCY,true);
      add(ed,RIGHT_OF,AFTER+2);
      ed.setKeyboard(Edit.KBD_NONE);

      add(new Label("Date masked edit:"),LEFT,AFTER+10);
      ed = new Edit();
      ed.setMode(Edit.DATE,true);
      add(ed,LEFT,AFTER+2);
      ed.setKeyboard(Edit.KBD_NONE);

      add(new Label("Normal masked edit:\n(Brazilian's 8-digit postal code)"),LEFT,AFTER+10);
      ed = new Edit("99.999-999");
      ed.setMode(Edit.NORMAL,true);
      add(ed,LEFT,AFTER+2);
      ed.setKeyboard(Edit.KBD_NONE);

      final PushButtonGroup pbg;
      add(pbg = new PushButtonGroup(new String[]{"1","2","3","4","5","6","7","8","9",null,"0",null}, 2, 4), RIGHT, BOTTOM_OF, PREFERRED+8,PREFERRED);
      pbg.setFocusLess(true);
      final KeyEvent ke = new KeyEvent();
      ke.type = KeyEvent.KEY_PRESS;
      pbg.addPressListener(new PressListener()
      {
         public void controlPressed(ControlEvent e)
         {
            Control focus = getFocus();
            if (focus instanceof Edit && pbg.getSelectedIndex() >= 0)
            {
               ke.touch();
               ke.target = focus;
               ke.key = pbg.getSelectedItem().charAt(0);
               focus.onEvent(ke);
            }
         }
      });
   }
   
   private void testLabelContainer()
   {
      String[] labels =
      {
         "Name",
         "Born date",
         "Telephone",
         "Address",
         "City",
         "Country",
         "",
      };
      AlignedLabelsContainer c = new AlignedLabelsContainer(labels,4);
      c.setBorderStyle(BORDER_LOWERED);
      c.labelAlign = RIGHT;
      c.foreColors = new int[]{Color.RED,Color.BLACK,Color.BLACK,Color.BLACK,Color.BLACK,Color.BLACK,Color.BLACK,};
      c.setInsets(2,2,2,2);
      add(c,LEFT+2,TOP+2,FILL-2,PREFERRED+4);
      int i;
      for (i =0; i < labels.length-2; i++)
         c.add(new Edit(),LEFT+2,c.getLineY(i));
      c.add(new ComboBox(new String[]{"Brazil","USA"}),LEFT+2,c.getLineY(i));
      c.add(new Button("Insert data"),RIGHT,SAME);
      c.add(new Button("Clear data"),RIGHT,AFTER+4,SAME,PREFERRED);
   }
   
   // attention: this is the old ListContainer style. See the ListContainerTest for the new style
   class LCItem extends ScrollContainer
   {
      Label lDate,lPrice,lDesc;
      Check chPaid;
      
      public LCItem()
      {
         super(false);
      }
      
      public void initUI()
      {
         add(chPaid = new Check("Paid"),LEFT,TOP);
         add(lDate = new Label("99/99/9999"),RIGHT,TOP); 
         add(new Label("US$"),LEFT,AFTER);
         add(lPrice = new Label("999.999.99"),AFTER,SAME);
         add(lDesc = new Label("",RIGHT),AFTER+10,SAME);
         lDesc.setText("description");
      }
   }
   
   private void testListContainer()
   {
      ListContainer lc;
      add(lc = new ListContainer(),LEFT,TOP,FILL,FILL);
      for (int i =0; i < 10; i++)
         lc.addContainer(new LCItem());
   }
}
