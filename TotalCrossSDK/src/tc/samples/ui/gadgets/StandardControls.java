package tc.samples.ui.gadgets;

import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;
import totalcross.unit.*;
import totalcross.util.*;

public class StandardControls extends Container
{
   Label lStatus;
   private Button btnClock;
   private Button btnInput;
   private Button btnMessage1,btnMessage2;
   private Check ch;
   private Radio rdEnab,rdDisab;
   private Edit ed;
   private ComboBox cb1,cb2;
   private TabbedContainer tp,tp2;
   private Button btnBench;
   private Slider sb1;
   private ScrollBar sb2;
   private MultiListBox lbox;
   private ComboBoxEditable cbe;
   private boolean initialized;
   
   public void initUI()
   {
      try
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
         add(cb1 = new ComboBox(new MultiListBox(items)),SAME,AFTER+cbe.getHeight()/2,ed);
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
            
         if (Settings.uiStyle == Settings.Vista) // guich@tc126_25
         {
            setTextShadowColor(Color.BLACK);
            btnInput.setTextShadowColor(BRIGHTER_BACKGROUND);
            tp.setTextShadowColor(DARKER_BACKGROUND);
            lbox.setTextShadowColor(DARKER_BACKGROUND);
            ed.setTextShadowColor(DARKER_BACKGROUND);
            cbe.setTextShadowColor(Color.WHITE);
            cb1.setTextShadowColor(BRIGHTER_BACKGROUND);
            cb2.setTextShadowColor(BRIGHTER_BACKGROUND);
         }
         
         if (!initialized)
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
      catch (Exception ee)
      {
         MessageBox.showException(ee,true);
      }
   }

   public void onEvent(Event event)
   {
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
            initUI();
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
            Window.setDeviceTitle(s);
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
}
