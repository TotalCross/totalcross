package totalcross.phone;

import totalcross.io.*;
import totalcross.sys.*;

import java.util.*;

public class PushNotification
{
   public static String readToken() 
   {
      String ret = null;
      String name = Settings.vmPath+"/push_token."+Settings.pushTokenAndroid;
      try
      {
         // get an exclusive write to the file
         byte[] b = new File(name, File.READ_WRITE).readAndClose();
         ByteArrayStream bas = new ByteArrayStream(b);
         DataStream ds = new DataStream(bas);
         ret = new String(ds.readChars());
      }
      catch (FileNotFoundException fnfe)
      {
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      return ret;
   }
   
   public static String[] readMessages() 
   {
      ArrayList<String> ret = new ArrayList<String>(5);
      String name = Settings.vmPath+"/push_messages."+Settings.pushTokenAndroid;
      try
      {
         // get an exclusive write to the file
         byte[] b = new File(name, File.READ_WRITE).readAndDelete();
         ByteArrayStream bas = new ByteArrayStream(b);
         DataStream ds = new DataStream(bas);
         while (bas.available() > 0)
            ret.add(new String(ds.readChars()));
      }
      catch (FileNotFoundException fnfe)
      {
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      return ret.toArray(new String[0]);
   }
}
