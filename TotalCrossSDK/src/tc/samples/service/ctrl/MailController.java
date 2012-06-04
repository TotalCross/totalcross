package tc.samples.service.ctrl;

import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;

public class MailController extends MainWindow
{
   static
   {
      Settings.useNewFont = true;
      Settings.uiAdjustmentsBasedOnFontHeight = true;
   }
   
   private ListBox lb;
   
   public MailController()
   {
      super("MailService Controller",RECT_BORDER);
   }
   
   public void initUI()
   {
      Button b;
      add(b = new Button("Register & start service"),CENTER,TOP,PREFERRED+100,PREFERRED+50);  b.appId = 1;
      add(b = new Button("Stop service"), CENTER,AFTER+100,SAME,PREFERRED+50);       b.appId = 2;
      add(b = new Button("Unregister service"), CENTER,AFTER+100,SAME,PREFERRED+50); b.appId = 3;
      add(b = new Button("Exit"), CENTER,AFTER+100,SAME,PREFERRED+50);                  b.appId = 4;
      add(lb = new ListBox(),LEFT,AFTER+50,FILL,FILL);
   }
   
   private void log(String s)
   {
      lb.addWrapping(s);
      lb.selectLast();
   }
   
   class MailService extends totalcross.Service // must have the same name of the real service
   {
      protected void onStart() {}
      protected void onService() {}
      protected void onStop() {}
   }
   
   MailService stub;
   
   public void onEvent(Event e)
   {
      try
      {
         if (e.type == ControlEvent.PRESSED)
         {
            int id = ((Control)e.target).appId;
            if (id == 4)
               exit(3);
            else
            {
               MailService ms = new MailService();
               switch (id)
               {
                  case 1:
                     if (ms.isRunning())
                        log("service is already running!");
                     else
                     {
                        log("lauching service");
                        ms.launchService();
                        if (waitService(ms, true))
                           log("service started!");
                     }
                     break;
                  case 2:
                     if (!ms.isRunning())
                        log("service is not running");
                     else
                     {
                        log("stopping service");
                        ms.stop();
                        if (waitService(ms, false))
                           log("service stopped!");
                     }
                     break;
                  case 3:
                     ms.unregisterService();
                     log("service unregistered!");
                     break;
               }
            }
         }
      }
      catch (Exception ee)
      {
         MessageBox.showException(ee,true);
      }
   }
   
   private boolean waitService(MailService ms, boolean status) throws Exception
   {
      boolean ok = false;
      for (int i = 30; i-- > 0 && !(ok=(ms.isRunning() == status));)
      {
         log("waiting service... "+i);
         Vm.sleep(1000);
      }
      if (!ok)
         log("failed due to timeout!");
      return ok;
         
   }
}
