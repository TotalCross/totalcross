package tc.samples.ui.androidui;

import totalcross.res.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;

public class MessageBoxSamples extends BaseContainer
{
   public MessageBoxSamples()
   {
      helpMessage = "These are MessageBox samples in the Android user interface style. Press back to go to the main menu.";
   }
   
   public void initUI()
   {
      try
      {
         super.initUI();
         ScrollContainer sc = new ScrollContainer(false, true);
         sc.setInsets(gap,gap,gap,gap);
         add(sc,LEFT,TOP,FILL,FILL);
         
         Button btn;
         
         Button.commonGap = gap;
         add(btn = new Button("Title only"), CENTER, TOP+fmH);
         btn.addPressListener(new PressListener()
         {
            public void controlPressed(ControlEvent e)
            {
               MessageBox mb = new MessageBox("Message","This is a MessageBox with title, in the Android user interface style.",new String[]{"Close"});
               mb.popup();
            }
         });
         add(btn = new Button("No title"), CENTER, AFTER+fmH);
         btn.addPressListener(new PressListener()
         {
            public void controlPressed(ControlEvent e)
            {
               MessageBox mb = new MessageBox("","This is a MessageBox without title, in the Android user interface style.",new String[]{"Close"});
               mb.popup();
            }
         });
         add(btn = new Button("Title and Icon\nTop separator"), CENTER, AFTER+fmH);
         btn.addPressListener(new PressListener()
         {
            public void controlPressed(ControlEvent e)
            {
               MessageBox mb = new MessageBox("Message","This is a MessageBox with title and icon with top separator, in the Android user interface style.",new String[]{"Close"});
               mb.headerColor = UIColors.messageboxBack;
               mb.footerColor = 0xAAAAAA;
               try
               {
                  mb.setIcon(Resources.warning);
               }
               catch (Exception ee) {ee.printStackTrace();}
               mb.popup();
            }
         });
         add(btn = new Button("Title and Icon\nTop/bottom separators"), CENTER, AFTER+fmH);
         btn.addPressListener(new PressListener()
         {
            public void controlPressed(ControlEvent e)
            {
               MessageBox mb = new MessageBox("Message","This is a MessageBox with title and icon with top and bottom separators, in the Android user interface style.",new String[]{"Close"});
               mb.footerColor = mb.headerColor = UIColors.messageboxBack;
               try
               {
                  // paint a copy of the image with the yellow color
                  Image img = Resources.warning.getFrameInstance(0);
                  img.applyColor2(Color.YELLOW);
                  mb.setIcon(img);
               }
               catch (Exception ee) {ee.printStackTrace();}
               mb.popup();
            }
         });
         Button.commonGap = 0;

         setInfo("Click Info button for help.");
      }
      catch (Exception ee)
      {
         MessageBox.showException(ee,true);
      }
   }
}