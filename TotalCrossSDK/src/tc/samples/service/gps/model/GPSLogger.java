package tc.samples.service.gps.model;

import totalcross.io.*;
import totalcross.io.device.gps.*;
import totalcross.sys.*;
import totalcross.util.*;

public class GPSLogger extends totalcross.Service
{
   static
   {
      Settings.applicationId = "TCgl";
   }
   
   public GPSLogger()
   {
      loopDelay = 5*60*1000; // 5 minutes
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
         GPS gps = new GPS();
         boolean ok = false;
         int end = Vm.getTimeStamp() + loopDelay*2/3; // 2/3 of 5 minutes
         while (Vm.getTimeStamp() < end && !(ok=gps.retrieveGPSData()) && isRunning())
            Vm.sleep(250);
         Vm.debug("GPSLOGGER GPS: "+ok);
         if (ok)
            try
            {
               File file = new File(!Settings.platform.equals(Settings.ANDROID) ? "/gps.log" : "/sdcard/gps.log", File.CREATE);
               file.setPos(file.getSize());
               file.writeBytes(gps.location[0]+","+gps.location[1]+","+gps.direction+","+gps.velocity+","+gps.satellites+","+gps.pdop+","+new Date(gps.lastFix)+","+gps.lastFix+"\n");
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
      Vm.debug("GPSLOGGER onStop");
   }
}
