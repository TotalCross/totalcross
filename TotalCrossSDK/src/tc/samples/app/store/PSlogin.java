package tc.samples.app.store;

import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;

public class PSlogin extends Container
{
   private Button btEnter;
   
   public void initUI()
   {
      try
      {
         add(new ImageControl(new Image("img/logo.png").smoothScaledFixedAspectRatio(width*2/3,false)),CENTER,TOP+300);
         
         Button b = btEnter = new Button("ENTER");
         b.setBorder(Button.BORDER_ROUND);
         b.setBackForeColors(Color.WHITE,Color.BLACK);
         b.roundBorderFactor = 3;
         add(b,CENTER,BOTTOM-100,PARENTSIZE+80,fmH*3);
         
         Edit ed;
         
         ed = new Edit();
         ed.caption = "Password";
         ed.captionIcon = new Image("img/pass.png").smoothScaledFixedAspectRatio(fmH*2,true);
         ed.setBackForeColors(Color.WHITE,Color.WHITE);
         add(ed,CENTER,BEFORE-100,PARENTSIZE+90,fmH*3);
   
         ed = new Edit();
         ed.caption = "Login";
         ed.captionIcon = new Image("img/login.png").smoothScaledFixedAspectRatio(fmH*2,true);
         ed.setBackForeColors(Color.WHITE,Color.WHITE);
         add(ed,CENTER,BEFORE-100,PARENTSIZE+90,fmH*3);
      }
      catch (Exception ee)
      {
         MessageBox.showException(ee,true);
      }
   }
   
   public void onEvent(Event e)
   {
      switch (e.type)
      {
         case ControlEvent.PRESSED:
            if (e.target == btEnter)
               new PSProductList().swapToTopmostWindow();
            break;
      }
   }
}
