package totalcross;

import totalcross.io.*;
import totalcross.sys.*;
import totalcross.ui.dialog.*;
import totalcross.util.*;

public abstract class Service implements MainClass
{
   protected int loopDelay = 30000;
   private String serviceName, fileName;
   private static final String regkey = "\\Services\\TotalCrossSrv";
   private static boolean ANDROID = Settings.platform.equals(Settings.ANDROID);
   private String serviceApplicationId;
   
   public Service(String serviceApplicationId)
   {
      this();
      this.serviceApplicationId = serviceApplicationId;
   }
   
   public Service()
   {
      serviceName = getClass().getName().replace('.','/');
      serviceName = serviceName.substring(serviceName.lastIndexOf('/')+1);
      serviceName = serviceName.substring(serviceName.lastIndexOf('$')+1);
      fileName = ANDROID ? "/sdcard/" : "/";
      fileName += serviceName+".service.control";
   }

   protected abstract void onStart();
   protected abstract void onService();
   protected abstract void onStop();

   final public void appStarting(int timeAvail)
   {
      totalcross.ui.MainWindow.minimize(); // run on background
      if (!registerService()) // run the service loop only if it was previously registered
         serviceLoop();
   }

   private void serviceLoop()
   {
      onStart();
      try
      {
         new File(fileName,File.CREATE).close(); // start
         while (isRunning())
         {
            onService();
            Vm.sleep(loopDelay);
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      finally
      {
         onStop();
      }
   }

   public void start()
   {
      if (ANDROID)
      {
         String path = serviceApplicationId != null ? "totalcross.app"+serviceApplicationId.toLowerCase() : Settings.appPath.substring(Settings.appPath.lastIndexOf('/')+1); // /data/data/totalcross.appkfcb
         Vm.exec(path,"TCService",0,true); // "totalcross.appkfcb"
      }
      else
      {
         String path = "\\"+serviceName+"\\"+serviceName+".exe";
         Vm.exec(path,null,0,false);
      }
   }
   
   public void stop() throws Exception
   {
      try
      {
         new File(fileName).delete();
      } catch (FileNotFoundException fnfe) {}
   }
   
   public boolean isRunning() throws Exception
   {
      return new File(fileName).exists();
   }
   
   public boolean registerService()
   {
      if (ANDROID)
         return false;
      try
      {
         Registry.getInt(Registry.HKEY_LOCAL_MACHINE, regkey,"Index");
      }
      catch (ElementNotFoundException enfe)
      {
         try
         {
            Registry.set(Registry.HKEY_LOCAL_MACHINE, regkey,"Dll","\\"+serviceName+"\\tcvm.dll");
            Registry.set(Registry.HKEY_LOCAL_MACHINE, regkey,"Context",1);
            Registry.set(Registry.HKEY_LOCAL_MACHINE, regkey,"FriendlyName","TotalCrossSrv");
            Registry.set(Registry.HKEY_LOCAL_MACHINE, regkey,"Index",0);
            Registry.set(Registry.HKEY_LOCAL_MACHINE, regkey,"Description","TotalCross Service");
            Registry.set(Registry.HKEY_LOCAL_MACHINE, regkey,"Order",8);
            Registry.set(Registry.HKEY_LOCAL_MACHINE, regkey,"Flags",0);
            Registry.set(Registry.HKEY_LOCAL_MACHINE, regkey,"Keep",1);
            Registry.set(Registry.HKEY_LOCAL_MACHINE, regkey,"Prefix","TSV");
            Registry.set(Registry.HKEY_LOCAL_MACHINE, regkey,"TCZ",serviceName+".tcz");
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
      return ret == 1;
   }
   
   public void unregisterService()
   {
      if (!ANDROID)
      {
         Vm.exec("unregister service",null,0,true);
         Vm.sleep(500);
      }
   }

   public void _postEvent(int type, int key, int x, int y, int modifiers, int timeStamp) {}
   public void appEnding() {}
   public void _onTimerTick(boolean canUpdate) {}
}
