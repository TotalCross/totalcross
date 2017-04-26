package totalcross.android;

import totalcross.*;

import android.content.*;
import android.os.*;

// http://marakana.com/forums/android/examples/108.html

public class TCService extends android.app.Service
{
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
   }
 
   public int onStartCommand(Intent intent, int flags, int startId) 
   {
      return START_STICKY;
   }
 
   public void onDestroy() 
   {
      super.onDestroy();
   }
}
