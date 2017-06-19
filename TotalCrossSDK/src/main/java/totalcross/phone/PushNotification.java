package totalcross.phone;

import totalcross.io.*;
import totalcross.sys.*;

import java.util.*;

@Deprecated
public class PushNotification
{
   @Deprecated
   public static String readToken() 
   {
      String ret = null;
      String name = Settings.vmPath+"/push_token.dat";
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
   
   @Deprecated
   public static String[] readMessages() 
   {
      ArrayList<String> ret = new ArrayList<String>(5);
      String name = Settings.vmPath+"/push_messages.dat";
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
