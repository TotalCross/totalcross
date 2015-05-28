package tc.samples.like.fb;

import totalcross.io.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.font.*;
import totalcross.ui.image.*;

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
         catch (Exception e)
         {
            e.printStackTrace();
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
      catch (Exception e)
      {
         logException(e);
         return null;
      }
   }

   public static Button noborder(Image img)
   {
      Button b = new Button(img);
      b.setBorder(Button.BORDER_NONE);
      return b;
   }
}
