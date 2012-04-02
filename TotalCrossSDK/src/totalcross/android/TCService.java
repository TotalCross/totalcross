package totalcross.android;

import android.app.*;
import android.content.*;
import android.os.*;

public class TCService extends Service
{
   public IBinder onBind(Intent intent)
   {
      return null;
   }
 
   public void onCreate() 
   {
   }
 
   public int onStartCommand(Intent intent, int flags, int startId) 
   {
      // We want this service to continue running until it is explicitly
      // stopped, so return sticky.
      return START_STICKY;
   }
 
   public void onDestroy() 
   {
   }
}
