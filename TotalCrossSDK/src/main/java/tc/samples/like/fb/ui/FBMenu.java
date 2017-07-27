package tc.samples.like.fb.ui;

import tc.samples.like.fb.FBUtils;
import tc.samples.like.fb.db.FBDB;
import totalcross.ui.Button;
import totalcross.ui.MainWindow;
import totalcross.ui.dialog.MessageBox;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.Event;

public class FBMenu extends FBContainer
{
  Button btExit, btDrop;
  @Override
  public void initUI()
  {
    btExit = new Button("Exit");
    btExit.setBackColor(MENUBACK);
    add(btExit, LEFT+50,AFTER+50,FILL-50,PREFERRED+100);

    btDrop = new Button("Drop database");
    btDrop.setBackColor(MENUBACK);
    add(btDrop, LEFT+50,AFTER+50,FILL-50,PREFERRED+100);
  }

  @Override
  public void onEvent(Event e)
  {
    try
    {
      switch (e.type)
      {
      case ControlEvent.PRESSED:
        if (e.target == btExit) {
          MainWindow.exit(0);
        } else
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
