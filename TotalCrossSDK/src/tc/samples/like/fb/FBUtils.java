package tc.samples.like.fb;

import totalcross.io.*;
import totalcross.io.device.gps.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.font.*;
import totalcross.ui.image.*;
import totalcross.ui.media.*;

public class FBUtils implements FBConstants
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
         catch (Throwable tt)
         {
            tt.printStackTrace();
         }
   }

   public static Ruler createRuler(int type)
   {
      Ruler r = new Ruler(type,false);
      r.setForeColor(BORDER);
      r.ignoreInsets = true;
      return r;
   }
   
   public static Button createButton(String s, Image i, int fmH)
   {
      Button b = new Button(s, i, Control.RIGHT, fmH);
      b.setFont(Font.getFont(true,fmH*8/10));
      b.setForeColor(0x9B9EA3);
      b.setBorder(Button.BORDER_NONE);
      return b;
   }
   
   public static byte[] jpegBytes(Image img)
   {
      try
      {
         ByteArrayStream bas = new ByteArrayStream(20*1024);
         img.createJpg(bas, 85);
         return bas.toByteArray();
      }
      catch (Throwable t)
      {
         logException(t);
         return null;
      }
   }

   public static Button noborder(Image img)
   {
      Button b = new Button(img);
      b.setBorder(Button.BORDER_NONE);
      return b;
   }

   public static Image takePhoto()
   {
      Image ret = null;
      try
      {
         Camera c = new Camera();
         c.cameraType = Camera.CAMERA_NATIVE_NOCOPY;
         c.resolutionWidth  = 640;
         c.resolutionHeight = 480;
         String name = c.click();
         if (name != null)
         {
            File f = new File(name,File.READ_WRITE);
            ret = new Image(f);
            ret = ret.smoothScaledFixedAspectRatio(480,true);
            if (!Settings.onJavaSE)
               f.delete();
         }
      }
      catch (Throwable t)
      {
         logException(t);
      }
      return ret;
   }
   
   public static String getCoords()
   {
      String ret = null;
      try 
      {
         GPS gps = new GPS();
         Toast.show("Gettings GPS coordinates...",Toast.INFINITE_NOANIM);
         int endTime = Vm.getTimeStamp() + 60000;
         do
         {
            if (gps.retrieveGPSData())
               ret = "Lat: "+getGMS(gps.getLatitude(),true)+"\nLon: "+getGMS(gps.getLongitude(),false);
            else
               Vm.sleep(50);
         }
         while (ret == null && Vm.getTimeStamp() < endTime);
         gps.stop();
      } 
      catch (Exception e) 
      {
      }
      Toast.show(null,0);
      return ret;
   }   

   public static String getGMS(double d, boolean isLat) // vem do gps assim
   {
      double absoluteLon = d < 0 ? -d : d;
      int gr = (int) absoluteLon;
      int mi = (int) ((absoluteLon - gr) * 60);
      int se = (int) ((((absoluteLon - gr) * 60) - mi) * 60 * 100);
      
      return gr+"\" "+mi+"' "+(se/100)+"."+(se%100)+" "+(d >= 0 ? isLat ? "E" : "N" : isLat ? "W" : "S");
   }
}
