package tc.samples.ui.gadgets;

import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;

public class SpinToolColor extends Container
{
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
      try
      {
         final TimerEvent pbte;
         Button btnChooseColor;
         MultiEdit me;
         Control c;
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
         add(c = new SpinList(new String[]{"Today","Day [1,31]"}, !Settings.fingerTouch),LEFT,AFTER+2,Settings.fingerTouch?FILL:PREFERRED,PREFERRED);
         ((SpinList)c).hAlign = CENTER;

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
         
         add(me = new MultiEdit(), LEFT,AFTER+2,FILL-pbv.getPreferredWidth()-fmH/2-4,FIT, c);
         me.setText("ToolTip is not supported in the MultiEdit control.");
   
         add(pbv,RIGHT,SAME,PREFERRED,SAME);
         add(pbzv,BEFORE-2,SAME,fmH/2,SAME);
         
         final Control fc = c;
         
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
                  fc.setBackColor(ccb.choosenColor);
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
      catch (Exception ee)
      {
         MessageBox.showException(ee,true);
      }
   }
}
