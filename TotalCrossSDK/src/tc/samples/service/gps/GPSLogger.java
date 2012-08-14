package tc.samples.service.gps;

import totalcross.io.*;
import totalcross.io.device.gps.*;
import totalcross.sys.*;
import totalcross.util.*;

public class GPSLogger extends totalcross.Service
{

   public GPSLogger()
   {
      loopDelay = 30*1000; // 30 seconds
   }
   
   protected void onStart()
   {
      try 
      {
         Vm.debug(Vm.ALTERNATIVE_DEBUG);
         Vm.debug("GPSLOGGER onStart");
      } catch (Exception e) {e.printStackTrace();}
   }

   protected void onService()
   {
      try
      {
         Vm.debug("GPSLOGGER onService");
         GPS gps = new GPS();
         boolean ok = false;
         for (int i = 0; i < 20 && !(ok=gps.retrieveGPSData()); i++) // wait 10 seconds
            Vm.sleep(500);
         Vm.debug("GPSLOGGER GPS: "+ok);
         if (ok)
            try
            {
               File file = new File(!Settings.platform.equals(Settings.ANDROID) ? "/gps.log" : "/sdcard/gps.log", File.CREATE);
               file.setPos(file.getSize());
               file.writeBytes(gps.location[0]+","+gps.location[1]+","+gps.direction+","+gps.velocity+","+gps.satellites+","+gps.pdop+","+new Date(gps.lastFix)+","+gps.lastFix);
               file.close();
            }
            catch (Exception e)
            {
               e.printStackTrace();
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
   }
}
