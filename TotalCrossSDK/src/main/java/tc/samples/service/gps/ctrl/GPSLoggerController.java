package tc.samples.service.gps.ctrl;

import totalcross.sys.Settings;
import totalcross.ui.Button;
import totalcross.ui.Label;
import totalcross.ui.MainWindow;
import totalcross.ui.dialog.MessageBox;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.Event;
import totalcross.ui.event.TimerEvent;

public class GPSLoggerController extends MainWindow
{
  static
  {
    Settings.useNewFont = true;
    Settings.fingerTouch= true;
    Settings.uiAdjustmentsBasedOnFontHeight = true;
  }

  Button b,b1,b2;
  Label l;
  TimerEvent te;

  public GPSLoggerController()
  {
    setUIStyle(Settings.Android);
  }

  @Override
  public void initUI()
  {
    add(b = new Button(" X "),RIGHT,TOP);
    add(b1 = new Button("Stop GPSLogger Service"),CENTER,CENTER,PARENTSIZE+80,PREFERRED+200);
    add(b2 = new Button("Start GPSLogger Service"),CENTER,BEFORE-100,SAME,SAME);
    add(l = new Label(""),LEFT,BOTTOM,FILL,PREFERRED);
    te = addTimer(1000);
  }

  class GPSLogger extends totalcross.Service
  {
    GPSLogger() {super("TCgl");}
    @Override
    protected void onStart() {}
    @Override
    protected void onService() {}
    @Override
    protected void onStop() {}
  }

  GPSLogger service = new GPSLogger();

  @Override
  public void onEvent(Event e)
  {
    try
    {
      if (e.type == TimerEvent.TRIGGERED && te.triggered) {
        l.setText(service.isRunning() ? "GPSLogger is running" : "GPSLogger is stopped");
      } else
        if (e.type == ControlEvent.PRESSED)
        {
          if (e.target == b) {
            exit(0);
          } else
            if (e.target == b2) {
              service.start();
            } else
              if (e.target == b1) {
                service.stop();
              }
        }
    }
    catch (Exception ee)
    {
      MessageBox.showException(ee,true);
    }
  }
}


