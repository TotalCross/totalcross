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
         return true;
   }
   
   public void unregisterService()
   {
      if (Settings.isWindowsDevice())
      {
         Vm.exec("unregister service",null,0,true);
         Vm.sleep(500);
      }
   }

   public void _postEvent(int type, int key, int x, int y, int modifiers, int timeStamp) {}
   public void appEnding() {}
   public void _onTimerTick(boolean canUpdate) {}
}
