package totalcross.android.gcm;

import totalcross.*;

import android.app.*;
import android.content.*;
import android.os.*;
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
            String title = data.getString("title");
            String text = data.getString("text");
            String info = data.getString("info");
            String ticker = data.getString("ticker"); if (ticker == null) ticker = title;
            String appdata = data.getString("appdata");
            if (appdata == null) 
               appdata = title;
            
            String classname = getClass().getName(); // totalcross.apptapi.gcm.GCMMessageReceiver
            classname = classname.replace("gcm.GCMMessageReceiver", "Loader");
            
            if (appdata == null) throw new Exception("You must suply at least 'appdata' or 'title' parameter with the message");
            
            // write to the program
            GCMUtils.writeMessage(this, classname, appdata);
            
            // send a broadcast if the vm is running
            GCMUtils.sendBroadcast(this, Launcher4A.MESSAGE_RECEIVED);
            
            // prepare the notification
            if (title != null)
            {
               NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
               int dot = classname.lastIndexOf('.');
               String pack = classname.substring(0,dot); //"totalcross.app.totalcrossapi";
               String cls  = classname.substring(dot+1); //"TotalCrossAPI";
               
               Intent intent = new Intent("android.intent.action.MAIN");
               intent.setClassName(pack,pack+"."+cls);
               intent.putExtra("cmdline","/pushnotification");
         
               Notification.Builder builder = new Notification.Builder(this);
               builder.setAutoCancel(true);
               builder.setContentTitle(title); // 1st line big
               if (text != null) builder.setContentText(text);   // 2nd line small
               if (info != null) builder.setContentInfo(info);   // at right, smaller
               builder.setContentIntent(PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));
               builder.setSmallIcon(totalcross.android.R.drawable.icon);
               builder.setTicker(ticker);
               notificationManager.notify(13121971, builder.getNotification());
            }               
         }
         catch (Throwable t)
         {
            AndroidUtils.handleException(t, false);
         }
   }
}
