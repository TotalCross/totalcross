package tc.samples.service.message.model;

import totalcross.io.*;
import totalcross.sys.*;

public class MailService extends totalcross.Service
{
   static
   {
      Settings.applicationId = "TCms";
   }
   
   private String inFolder;

   public MailService()
   {
      loopDelay = 15000;
   }
   
   protected void onStart()
   {
      inFolder = !Settings.platform.equals(Settings.ANDROID) ? "/msg" : "/sdcard/msg";
      try 
      {
         if (!new File(inFolder).exists())
            new File(inFolder).createDir();
      } catch (Exception e) {e.printStackTrace();}
   }

   protected void onService()
   {
      try
      {
         String[] files = new File(inFolder).listFiles();
         if (files != null)
            for (int i = 0; i < files.length; i++)
               if (files[i].endsWith(".txt"))
               {
                  // here we just launch the viewer to show the file
                  Vm.exec("/totalcross/mailviewer/MailViewer.exe",null,0,true);
               }
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
