package tc.samples.service.ctrl;

import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.event.*;

public class MailController extends MainWindow
{
   static
   {
      Settings.useNewFont = true;
      Settings.uiAdjustmentsBasedOnFontHeight = true;
   }
   
   public MailController()
   {
      super("MailService Controller",RECT_BORDER);
   }
   
   public void initUI()
   {
      Button b;
      add(b = new Button("Register service"),CENTER,TOP,PARENTSIZE+50,PREFERRED+50);          b.appId = 1;
      add(b = new Button("Start service"), CENTER,AFTER+100,PARENTSIZE+50,PREFERRED+50);      b.appId = 2;
      add(b = new Button("Stop service"), CENTER,AFTER+100,PARENTSIZE+50,PREFERRED+50);       b.appId = 3;
      add(b = new Button("Unregister service"), CENTER,AFTER+100,PARENTSIZE+50,PREFERRED+50); b.appId = 4;
      add(b = new Button("Exit"), CENTER,BOTTOM,PARENTSIZE+50,PREFERRED+50);                  b.appId = 5;
   }
   
   public void onEvent(Event e)
   {
      if (e.type == ControlEvent.PRESSED)
         switch (((Control)e.target).appId)
         {
            case 0:
               break;
            case 1:
               break;
            case 2:
               break;
            case 3:
               break;
            case 4:
               exit(0);
               break;
         }
   }
}
