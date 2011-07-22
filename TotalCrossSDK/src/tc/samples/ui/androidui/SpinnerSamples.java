package tc.samples.ui.androidui;

import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.gfx.*;

public class SpinnerSamples extends BaseContainer
{
   private Spinner sp1,sp2;
   
   public SpinnerSamples()
   {
      helpMessage = "These are Spinner samples in the Android user interface style. Note that the Spinner control is not likely to be used by the programmer; you should use the ProgressBox instead. Press back to go to the main menu.";
   }
   
   public void initUI()
   {
      try
      {
         super.initUI();

         Spacer s;
         add(s = new Spacer(1,1),CENTER,CENTER);
         Spinner.spinnerType = Spinner.ANDROID;
         add(sp1 = new Spinner(),CENTER,BEFORE-gap,fmH*2,fmH*2, s);
         Spinner.spinnerType = Spinner.IPHONE;
         add(sp2 = new Spinner(),CENTER,AFTER+gap,fmH*2,fmH*2, s);

         setInfo("Click Info button for help.");
      }
      catch (Exception ee)
      {
         MessageBox.showException(ee,true);
      }
   }
   
   public void onPaint(Graphics g)
   {
      super.onPaint(g);
      // if we call start in initUI, the spinner will be shown before the rest of the container.
      // so, we start it here, after we're sure that it was already painted.
      if (!sp1.isRunning()) 
      {
         sp1.start();
         sp2.start();
      }
   }
   public void onRemove() // stop spinners at end
   {
      sp1.stop();
      sp2.stop();
   }
}