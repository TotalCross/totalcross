package tc.samples.like.fb;

import totalcross.io.*;
import totalcross.sys.*;

public class FBUtils
{
   public static void logException(Throwable t)
   {
      t.printStackTrace();
      if (!Settings.onJavaSE)
         try
         {
            File f = new File(Settings.platform.equals(Settings.ANDROID) ? "/sdcard/fbtc_error.log" : "device/fbtc_error.log",File.CREATE);
            f.setPos(f.getSize());
            String m = t.getMessage();
            String c = t.getClass().getName();
            String s = Vm.getStackTrace(t);
            f.writeBytes(new Time().getSQLString());
            f.writeBytes("Class: "+c);
            if (m != null) f.writeBytes("Message: "+m);
            f.writeBytes("Stack trace: \n");
            f.writeBytes(s);
            f.close();
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
   }
}
