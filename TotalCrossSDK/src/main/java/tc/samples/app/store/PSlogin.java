package tc.samples.app.store;

import totalcross.sys.SpecialKeys;
import totalcross.ui.Button;
import totalcross.ui.Container;
import totalcross.ui.Edit;
import totalcross.ui.ImageControl;
import totalcross.ui.MainWindow;
import totalcross.ui.Toast;
import totalcross.ui.Window;
import totalcross.ui.dialog.MessageBox;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.Event;
import totalcross.ui.event.KeyEvent;
import totalcross.ui.event.KeyListener;
import totalcross.ui.gfx.Color;
import totalcross.ui.image.Image;

/** Login page */

public class PSlogin extends Container
{
  private Button btEnter;
  private Edit edPass, edLogin;

  @Override
  public void initUI()
  {
    try
    {
      // add the logo at top
      ImageControl ic = new ImageControl(new Image("img/logo.png"));
      ic.scaleToFit = ic.centerImage = true;
      add(ic,LEFT,PARENTSIZE+12,FILL,PARENTSIZE+20);

      Edit ed;

      // add edits before button

      edLogin = ed = new Edit();
      ed.caption = "Login";
      ed.captionIcon = new Image("img/login.png").smoothScaledFixedAspectRatio(fmH*2,true);
      ed.setBackForeColors(Color.WHITE,Color.WHITE);
      add(ed,CENTER,PARENTSIZE+37,PARENTSIZE+90,PARENTSIZE+15);

      edPass = ed = new Edit();
      ed.caption = "Password";
      ed.captionIcon = new Image("img/pass.png").smoothScaledFixedAspectRatio(fmH*2,true);
      ed.setBackForeColors(Color.WHITE,Color.WHITE);
      add(ed,CENTER,PARENTSIZE+62,PARENTSIZE+90,PARENTSIZE+15);

      // add button at bottom
      Button b = btEnter = new Button("ENTER");
      b.setBorder(Button.BORDER_ROUND);
      b.setBackForeColors(Color.WHITE,Color.BLACK);
      b.roundBorderFactor = 3;
      add(b,CENTER,PARENTSIZE+87,PARENTSIZE+80,PARENTSIZEMIN+20);

      Window.keyHook = new KeyListener() 
      {
        @Override
        public void keyPressed(KeyEvent e) {}
        @Override
        public void actionkeyPressed(KeyEvent e) {}
        @Override
        public void specialkeyPressed(KeyEvent e)
        {
          if (e.key == SpecialKeys.ESCAPE)
          {
            e.consumed = true;
            MainWindow.exit(0);
          }
        }
      };
    }
    catch (Exception ee)
    {
      MessageBox.showException(ee,true);
    }
  }

  @Override
  public void onEvent(Event e)
  {
    switch (e.type)
    {
    case ControlEvent.PRESSED:
      if (e.target == btEnter)
      {
        // check if the edits are filled
        if (edPass.getLength() == 0 || edLogin.getLength() == 0) {
          Toast.show("Please type the login and password!",2000);
        } else
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
