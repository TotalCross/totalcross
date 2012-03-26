package tc.samples.service.im;

import totalcross.io.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.util.*;

public class MailService extends MainWindow implements Runnable
{
   static
   {
      Settings.closeButtonType = Settings.MINIMIZE_BUTTON;
   }
   
   public void run()
   {
      String inFolder = !Settings.platform.equals(Settings.ANDROID) ? "/msg" : "/sdcard/msg";
      try 
      {
         if (!new File(inFolder).exists())
            new File(inFolder).createDir();
      } catch (Exception e) {e.printStackTrace();}

      log("Write a txt file at folder "+inFolder+" to show it");
      byte []buf = new byte[1024];
      while (true)
      {
         try
         {
            String[] files = new File(inFolder).listFiles();
            if (files != null)
               for (int i = 0; i < files.length; i++)
                  if (files[i].endsWith(".txt"))
                  {
                     String fileName = files[i];
                     log("reading file "+fileName);
                     // load file
                     File f = new File(Convert.appendPath(inFolder,fileName),File.READ_WRITE);
                     int len = f.getSize();
                     if (buf.length < len) buf = new byte[len];
                     f.readBytes(buf,0,len);
                     // show file
                     restore();
                     boolean stop = fileName.toLowerCase().indexOf("stop.txt") >= 0;
                     if (!stop)
                        new MessageBox("Message Received",new String(buf,0,len)).popup();
                     minimize();
                     f.delete();
                     if (stop)
                     {
                        new MessageBox("message","stopping service...").popupNonBlocking();
                        Vm.exec("unregister service",null,0,true);
                        Vm.sleep(500);
                        exit(0);
                        return;
                     }

                     log("file deleted");
                  }
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
         log("waiting 30 seconds");
         Vm.sleep(30*1000); // wait 30 seconds
      }
   }
   
   ListBox lb;
   
   public MailService()
   {
      setUIStyle(Settings.Android);
      prepareService();
   }
   
   public void initUI()
   {
      MessageBox mb = new MessageBox("Attention","Service being started. Write a txt file at \\msg to open the service's window",null);
      mb.popupNonBlocking();
      Vm.sleep(2000);
      mb.unpop();
      minimize();
      add(lb = new ListBox(), LEFT,TOP,FILL,FILL);
      new Thread(this).start();
   }
   
   public void log(String s)
   {
      lb.addWrapping(s);
      lb.selectLast();
   }
   
   private void prepareService()
   {
      try
      {
         Registry.getInt(Registry.HKEY_LOCAL_MACHINE, "\\Services\\TotalCrossSrv","Index");
      }
      catch (ElementNotFoundException enfe)
      {
         try
         {
            Registry.set(Registry.HKEY_LOCAL_MACHINE, "\\Services\\TotalCrossSrv","Dll","\\MailService\\tcvm.dll");
            Registry.set(Registry.HKEY_LOCAL_MACHINE, "\\Services\\TotalCrossSrv","Context",1);
            Registry.set(Registry.HKEY_LOCAL_MACHINE, "\\Services\\TotalCrossSrv","FriendlyName","TotalCrossSrv");
            Registry.set(Registry.HKEY_LOCAL_MACHINE, "\\Services\\TotalCrossSrv","Index",0);
            Registry.set(Registry.HKEY_LOCAL_MACHINE, "\\Services\\TotalCrossSrv","Description","TotalCross Service");
            Registry.set(Registry.HKEY_LOCAL_MACHINE, "\\Services\\TotalCrossSrv","Order",8);
            Registry.set(Registry.HKEY_LOCAL_MACHINE, "\\Services\\TotalCrossSrv","Flags",0);
            Registry.set(Registry.HKEY_LOCAL_MACHINE, "\\Services\\TotalCrossSrv","Keep",1);
            Registry.set(Registry.HKEY_LOCAL_MACHINE, "\\Services\\TotalCrossSrv","Prefix","TSV");
         }
         catch (Exception ee)
         {
            MessageBox.showException(ee,true);
         }
      }
      catch (Exception ee)
      {
         MessageBox.showException(ee,true);
      }
      int ret = Vm.exec("register service",null,0,true); // register the service
      if (ret == 1) // registered, exit
         exit(0);
   }
}
