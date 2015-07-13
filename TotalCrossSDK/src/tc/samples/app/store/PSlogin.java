package tc.samples.app.store;

import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;

/** Login page */

public class PSlogin extends Container
{
   private Button btEnter;
   private Edit edPass, edLogin;
   
   public void initUI()
   {
      try
      {
         // add the logo at top
         add(new ImageControl(new Image("img/logo.png").smoothScaledFixedAspectRatio(width*2/3,false)),CENTER,TOP+300);
         
         // add button at bottom
         Button b = btEnter = new Button("ENTER");
         b.setBorder(Button.BORDER_ROUND);
         b.setBackForeColors(Color.WHITE,Color.BLACK);
         b.roundBorderFactor = 3;
         add(b,CENTER,BOTTOM-100,PARENTSIZE+80,fmH*3);
         
         Edit ed;
         
         // add edits before button
         edPass = ed = new Edit();
         ed.caption = "Password";
         ed.captionIcon = new Image("img/pass.png").smoothScaledFixedAspectRatio(fmH*2,true);
         ed.setBackForeColors(Color.WHITE,Color.WHITE);
         add(ed,CENTER,BEFORE-100,PARENTSIZE+90,fmH*3);
   
         edLogin = ed = new Edit();
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
            {
               // check if the edits are filled
               if (edPass.getLength() == 0 || edLogin.getLength() == 0)
                  Toast.show("Please type the login and password!",2000);
               else
               {
                  String p = edPass.getText(), l = edLogin.getText();
                  // TODO validate the login and password
                  
                  // here we just go to the products list screen
                  new PSProductList().swapToTopmostWindow();
               }
            }
            break;
      }
   }
}
