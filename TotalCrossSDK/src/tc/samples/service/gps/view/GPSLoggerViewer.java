package tc.samples.service.gps.view;


import totalcross.io.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.event.*;

public class GPSLoggerViewer extends MainWindow
{
   static
   {
      Settings.useNewFont = true;
      Settings.fingerTouch= true;
      Settings.uiAdjustmentsBasedOnFontHeight = true;
   }
   
   public GPSLoggerViewer()
   {
      setUIStyle(Settings.Android);
   }

   ListBox lb;
   Button b;
   TimerEvent te;

   public void initUI()
   {
      add(b = new Button("  X  "),RIGHT,TOP);
      add(lb = new ListBox());
      lb.setRect(LEFT,AFTER+50,FILL,FILL);
      te = addTimer(5 * 60 * 1000);
      loadCoords();
   }
   
   public void onEvent(Event e)
   {
      if (e.type == TimerEvent.TRIGGERED && te.triggered)
         loadCoords();
      else
      if (e.type == ControlEvent.PRESSED && e.target == b)
         exit(0);
   }
   
   private void loadCoords() 
   {
      try
      {
         File f = new File(!Settings.platform.equals(Settings.ANDROID) ? "/gps.log" : "/sdcard/gps.log", File.READ_ONLY);
         LineReader lr = new LineReader(f);
         String s;
         lb.removeAll();
         while ((s = lr.readLine()) != null)
            status(s);
         f.close();
      }
      catch (FileNotFoundException fnfe)
      {
         status("No coordinates found.");
      }
      catch (Exception ee)
      {
         status(ee.getClass().getName()+" "+ee.getMessage());
         ee.printStackTrace();
      }
   }

   private void status(String s)
   {
      if (s != null)
      {
         lb.add(s);
         lb.selectLast();
      }
   }
}


