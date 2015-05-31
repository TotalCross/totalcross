package tc.samples.like.fb.ui;

import tc.samples.like.fb.*;
import tc.samples.like.fb.db.*;

import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;

public class FBMenu extends Container
{
   Button btExit, btDrop;
   public void initUI()
   {
      add(btExit = new Button("Exit"), LEFT+50,AFTER+50,FILL-50,PREFERRED+100);
      add(btDrop = new Button("Drop database"), LEFT+50,AFTER+50,FILL-50,PREFERRED+100);
   }
   
   public void onEvent(Event e)
   {
      try
      {
         switch (e.type)
         {
            case ControlEvent.PRESSED:
               if (e.target == btExit)
                  MainWindow.exit(0);
               else
               if (e.target == btDrop)
               {
                  FBDB.db.dropTables();
                  new MessageBox("Message","Press ok to exit the application").popup();
                  MainWindow.exit(0);
               }
               break;
         }
      }
      catch (Throwable t)
      {
         FBUtils.logException(t);
      }
   }
}
