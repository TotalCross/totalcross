package tc.samples.ui.androidui;

import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;

public class PopupMenuSamples extends BaseContainer
{
   private int lastSel;
   
   public PopupMenuSamples()
   {
      helpMessage = "These are PopupMenu samples in the Android user interface style. Press back to go to the main menu.";
   }
   
   public void initUI()
   {
      try
      {
         super.initUI();
         
         final Button btn1 = new Button(" Popup menu ",new Image("totalcross/res/android/comboArrow.png"), LEFT, fmH/2);
         add(btn1,CENTER,CENTER);
         btn1.addPressListener(new PressListener()
         {
            public void controlPressed(ControlEvent e)
            {
               try
               {
                  String[] items =
                  {
                     "Always",
                     "Never",
                     "Only in Silent mode",
                     "Only when not in Silent mode",
                     "None the answers above",
                     "All the answers above"
                  };
                  PopupMenu pm = new PopupMenu("Vibrate",items);
                  pm.setBackColor(Color.BRIGHT);
                  pm.setCursorColor(Color.CYAN);
                  pm.setSelectedIndex(lastSel);
                  pm.popup();
                  lastSel = pm.getSelectedIndex();
                  setInfo(lastSel == -1 ? "Cancelled" : "Selected "+lastSel);
               }
               catch (Exception ee)
               {
                  MessageBox.showException(ee,true);
               }
            }
         });

         setInfo("Click Info button for help.");
      }
      catch (Exception ee)
      {
         MessageBox.showException(ee,true);
      }
   }
}