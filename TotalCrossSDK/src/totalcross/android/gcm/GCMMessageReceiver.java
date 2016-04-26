package totalcross.android.gcm;

import totalcross.*;

import android.app.*;
import android.content.*;
import android.os.*;
import android.support.v4.app.*;
import android.support.v4.content.*;
import com.google.android.gms.gcm.*;

/**
 
 This class is called when a message is received.
 One of the parameters that is sent from the server is the classname, so that the correct application can be called.
 The message is written to the Message queue file, and a broadcast is sent, because if the application is
 running, it can get the message.
 The message is also sent as notification so, if the app is not running, it can be opened.
 
 */
public class GCMMessageReceiver extends GcmListenerService
{
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
            // get the parameters
            String classname = data.getString("classname");
            if (classname == null)
               throw new Exception("You must suply with the message the 'classname' parameter for the target program to open when clicking in the notification");
            String title = data.getString("title");
            String text = data.getString("text");
            String info = data.getString("info");
            String ticker = data.getString("ticker"); if (ticker == null) ticker = title;
            String appdata = data.getString("appdata");
            if (appdata == null) 
               appdata = title;
            
            if (appdata == null) throw new Exception("You must suply at least 'appdata' or 'title' parameter with the message");
            
            AndroidUtils.debug("Message received: "+appdata);
            // write to the program
            GCM2TC.writeEvent(GCM2TC.MESSAGE_RECEIVED, appdata);
            
            // send a broadcast if the vm is running
            Intent in = new Intent("totalcross.MESSAGE_EVENT");
            // Put extras into the intent as usual
            in.putExtra("classname",classname);
            in.putExtra("resultCode", Activity.RESULT_OK);
            // Fire the broadcast with intent packaged
            LocalBroadcastManager.getInstance(this).sendBroadcast(in);
            
            // prepare the notification
            if (title != null)
            {
               NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
               int dot = classname.lastIndexOf('.');
               String pack = classname.substring(0,dot); //"totalcross.app.totalcrossapi";
               String cls  = classname.substring(dot+1); //"TotalCrossAPI";
               
               Intent intent = new Intent("android.intent.action.MAIN");
               intent.setClassName(pack,pack+"."+cls);
         
               NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
               builder.setAutoCancel(true);
               builder.setContentTitle(title); // 1st line big
               if (text != null) builder.setContentText(text);   // 2nd line small
               if (info != null) builder.setContentInfo(info);   // at right, smaller
               builder.setContentIntent(PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));
               builder.setSmallIcon(totalcross.android.R.drawable.icon);
               builder.setTicker(ticker);
               notificationManager.notify(19700325, builder.build());
               AndroidUtils.debug("Sent notification");
            }               
         }
         catch (Throwable t)
         {
            AndroidUtils.handleException(t, false);
         }
   }
}
