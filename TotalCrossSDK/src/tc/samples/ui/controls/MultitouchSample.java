package tc.samples.ui.controls;

import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.image.*;

public class MultitouchSample extends BaseContainer
{
   ImageControl ic;
   public static Image screenShot;
   private static Image lata;

   public void initUI()
   {
      super.initUI();
      isSingleCall = true;
      
      if (!Settings.isOpenGL && !Settings.onJavaSE)
         add(new Label("This sample works only on iOS, Android and Windows Phone."),CENTER,CENTER);
      else
      try
      {
         super.initUI();
         setTitle("Multitouch");
         if (lata == null)
            lata = new Image("tc/samples/ui/controls/lata.jpg");
         ic = new ImageControl(screenShot != null ? screenShot : lata);
         screenShot = null;
         ic.allowBeyondLimits = false;
         ic.setEventsEnabled(true);
         updateStatus();
         add(ic,LEFT+gap,TOP+gap,FILL-gap,FILL-gap);
         ic.addMultiTouchListener(new MultiTouchListener()
         {
            public void scale(MultiTouchEvent e)
            {
               updateStatus();
            }
         });
      }
      catch (Exception e)
      {
         MessageBox.showException(e,false);
      }
   }
   
   private void updateStatus()
   {
      setInfo(ic.getImageWidth()+"x"+ic.getImageHeight());
   }
}