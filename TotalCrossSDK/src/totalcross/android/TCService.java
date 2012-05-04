package totalcross.android;

import totalcross.*;

import android.app.*;
import android.content.*;
import android.os.*;

// http://marakana.com/forums/android/examples/108.html

public class TCService extends Service
{
/*   static final int DELAY = 30000; // 1/2 a min
   VmRunnable vmRunnable;
   Handler handler;
*/   
   public IBinder onBind(Intent intent)
   {
      AndroidUtils.debug("@@@@ Service onBind "+intent);
      return null;
   }
 
   public void onStart(Intent intent, int startId) 
   {
      AndroidUtils.debug("@@@@ Service onStart");
      super.onStart(intent, startId);
   }

   public void onCreate() 
   {
      super.onCreate();
      AndroidUtils.debug("@@@@ starting loader intent");
      Intent myStarterIntent = new Intent(getApplicationContext(), Loader.class);
      myStarterIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      getApplicationContext().startActivity(myStarterIntent);
      
/*      handler = new Handler();
      vmRunnable = new VmRunnable();
      handler.post(vmRunnable);
*/   }
 
   public int onStartCommand(Intent intent, int flags, int startId) 
   {
      return START_STICKY;
   }
 
   public void onDestroy() 
   {
      super.onDestroy();
/*      if (handler != null) {
      handler.removeCallbacks(vmRunnable);
      vmRunnable = null;
      handler = null;}
*/   }

/*   class VmRunnable implements Runnable 
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
*/}
