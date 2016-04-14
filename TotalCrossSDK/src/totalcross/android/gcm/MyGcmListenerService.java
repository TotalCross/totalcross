package totalcross.android.gcm;

import android.app.*;
import android.content.*;
import android.os.*;
import android.support.v4.app.*;
import android.util.*;
import com.google.android.gms.gcm.*;

public class MyGcmListenerService extends GcmListenerService
{
   private static final String TAG = "TotalCross";

   /**
    * Called when message is received. ex: 
    * time: 15:10 
    * score: 5x1 
    * collapse_key: do_not_collapse 
    * From: 954430333411
    */
   public void onMessageReceived(String from, Bundle data)
   {
      if (data != null)
         try
         {
            String classname = data.getString("classname");
            if (classname == null)
               throw new Exception("You must suply with the message the 'classname' parameter for the target program to open when clicking in the notification");
            String title = data.getString("title");
            String text = data.getString("text");
            String info = data.getString("info");
            String ticker = data.getString("ticker"); if (ticker == null) ticker = title;
            String appdata = data.getString("appdata");
            if (appdata == null) appdata = title;
            
            if (title == null) throw new Exception("You must suply at least the 'title' parameter with the message");
            
            NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            int dot = classname.lastIndexOf('.');
            String pack = classname.substring(0,dot); //"totalcross.app.totalcrossapi";
            String cls  = classname.substring(dot+1); //"TotalCrossAPI";
            
            Intent intent = new Intent("android.intent.action.MAIN");
            intent.putExtra("cmdline", appdata);
            intent.setClassName(pack,pack+"."+cls);
      
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
            builder.setAutoCancel(true);
            builder.setContentTitle(title); // 1st line big
            if (text != null) builder.setContentText(text);   // 2nd line small
            if (info != null) builder.setContentInfo(info);   // at right, smaller
            builder.setContentIntent(PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));
            builder.setSmallIcon(totalcross.android.R.drawable.icon);
            builder.setTicker(ticker);
            Notification not = builder.build();
            notificationManager.notify(19700325, not);
         }
         catch (Throwable t)
         {
            Log.i(TAG, "onMessageReceived\n\n"+t.getMessage()+"\n\n"+Log.getStackTraceString(t));
         }
   }
}
