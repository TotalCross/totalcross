package totalcross.android.gcm;

import totalcross.*;

import java.io.*;
import java.nio.channels.*;
import java.util.*;

public class GCM2TC
{
   public static final int TOKEN_RECEIVED = 360;
   public static final int MESSAGE_RECEIVED = 361;
   
   public static class Event
   {
      public int type;
      public String msg;
      
      public Event(DataInputStream ds) throws IOException
      {
         type = ds.readByte();
         msg = ds.readUTF();
      }
      public Event(int type, String msg)
      {
         this.type = type;
         this.msg = msg;
      }
      public void saveTo(DataOutputStream ds) throws IOException
      {
         ds.writeByte(type);
         ds.writeUTF(msg);
      }
   }
   
   public static void writeEvent(int type, String msg)
   {
      String name = Launcher4A.appPath+"/push.events"; // /data/data/totalcross.app.totalcrossapi/push.events
      AndroidUtils.debug("writeEvent "+type+" to "+name);
      
      try
      {
         // get an exclusive write to the file
         FileOutputStream fis = new FileOutputStream(name);
         DataOutputStream dis = new DataOutputStream(fis);
         //FileLock fl = lock(fis.getChannel());
         new Event(type, msg).saveTo(dis);
         fis.close();
         //fl.release();
      }
      catch (Exception e)
      {
         AndroidUtils.handleException(e, false);
      }
   }
   
   private static FileLock lock(FileChannel channel)
   {
      FileLock fl;
      while (true)
         try 
         {
            Thread.sleep(100); 
            if ((fl = channel.tryLock(0L, Long.MAX_VALUE, true)) != null) 
               break; 
         } catch (Exception e) {}
      return fl;
   }

   public static ArrayList<Event> readEvents(int type, String msg)
   {
      ArrayList<Event> ret = new ArrayList<Event>(5);
      String name = Launcher4A.appPath+"/push.events";
      AndroidUtils.debug("readEvents "+type+" from "+name);
      
      try
      {
         // get an exclusive write to the file
         File f = new File(name);
         FileInputStream fis = new FileInputStream(f);
         DataInputStream dis = new DataInputStream(fis);
         FileLock fl = lock(fis.getChannel());
         while (fis.available() > 0)
            ret.add(new Event(dis));
         f.delete();
         fis.close();
         fl.release();
      }
      catch (Exception e)
      {
         AndroidUtils.handleException(e, false);
      }
      return ret;
   }
}
