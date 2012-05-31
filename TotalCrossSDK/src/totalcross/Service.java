package totalcross;

import totalcross.sys.*;
import totalcross.ui.dialog.*;
import totalcross.util.*;

public abstract class Service implements MainClass
{
   protected int loopDelay = 30000;
   private String serviceName;
   private static final String regkey = "\\Services\\TotalCrossSrv";
   
   public Service()
   {
      serviceName = getClass().getName().replace('.','/');
      serviceName = serviceName.substring(serviceName.lastIndexOf('/')+1);
      serviceName = serviceName.substring(serviceName.lastIndexOf('$')+1);
      Vm.alert("service name: "+serviceName);
   }

   protected abstract void onStart();
   protected abstract void onService();
   protected abstract void onStop();

   
   final public void appStarting(int timeAvail)
   {
      if (!registerService()) // run the service loop only if it was previously registered
         serviceLoop();
   }

   private void serviceLoop()
   {
      onStart();
      try
      {
         start();
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

   public void launchService()
   {
      String path = "\\"+serviceName+"\\"+serviceName+".exe";
      Vm.alert(path);
      Vm.exec(path,null,0,false);
   }
   
   public void start() throws Exception
   {
      if (Settings.isWindowsDevice())
         Registry.set(Registry.HKEY_LOCAL_MACHINE, regkey, "running", 1);
   }
   
   public void stop() throws Exception
   {
      if (Settings.isWindowsDevice())
         Registry.set(Registry.HKEY_LOCAL_MACHINE, regkey, "running", 0);
   }
   
   public boolean isRunning() throws Exception
   {
      if (Settings.isWindowsDevice())
         return Registry.getInt(Registry.HKEY_LOCAL_MACHINE, regkey, "running") == 1;
      return false;
   }
   
   public boolean registerService()
   {
      if (Settings.isWindowsDevice())
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
      Vm.exec("unregister service",null,0,true);
      Vm.sleep(500);
   }

   public void _postEvent(int type, int key, int x, int y, int modifiers, int timeStamp) {}
   public void appEnding() {}
   public void _onTimerTick(boolean canUpdate) {}
}
