package tc.samples.service.gps;

import totalcross.io.*;
import totalcross.io.device.gps.*;
import totalcross.sys.*;
import totalcross.util.*;

public class GPSLogger extends totalcross.Service
{
   private File file;

   public GPSLogger()
   {
      loopDelay = 30*1000; // 30 seconds
   }
   
   protected void onStart()
   {
      try 
      {
         file = new File(!Settings.platform.equals(Settings.ANDROID) ? "/gps.log" : "/sdcard/gps.log", File.CREATE);
      } catch (Exception e) {e.printStackTrace();}
   }

   protected void onService()
   {
      try
      {
         GPS gps = new GPS();
         boolean ok = false;
         for (int i = 0; i < 20 && !(ok=gps.retrieveGPSData()); i++) // wait 10 seconds
            Vm.sleep(500);
         if (ok)
         {
            file.writeBytes(gps.location[0]+","+gps.location[1]+","+gps.direction+","+gps.velocity+","+gps.satellites+","+gps.pdop+","+new Date(gps.lastFix)+","+gps.lastFix);
            file.flush();
         }
         gps.stop();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   protected void onStop()
   {
      try
      {
         file.close();
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }
}
