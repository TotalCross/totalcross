package totalcross.android;

import android.app.*;
import android.content.*;
import android.os.*;

// http://marakana.com/forums/android/examples/108.html

public class TCService extends Service
{
   /*
public class BootReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {
    context.startService( new Intent(context, UpdaterService.class) );
    Log.d("BootReceiver", "onReceive'd");
  }

}
    */
   static final int DELAY = 30000; // 1/2 a min
   VmRunnable vmRunnable;
   Handler handler;
   
   public IBinder onBind(Intent intent)
   {
      return null;
   }
 
   public void onStart(Intent intent, int startId) 
   {
      super.onStart(intent, startId);
   }

   public void onCreate() 
   {
      super.onCreate();
      handler = new Handler();
      vmRunnable = new VmRunnable();
      handler.post(vmRunnable);
   }
 
   public int onStartCommand(Intent intent, int flags, int startId) 
   {
      return START_STICKY;
   }
 
   public void onDestroy() 
   {
      super.onDestroy();
      handler.removeCallbacks(vmRunnable);
      vmRunnable = null;
      handler = null;
   }

   class VmRunnable implements Runnable 
   {
      public void run() 
      {
         new Thread() 
         {
            public void run() 
            {
               // run vm.
              
               // Do this again
               handler.postDelayed(vmRunnable, DELAY);
            }
         }.start();
      }
   }
}
