package totalcross.android.firebase;

/**
 
 This class is called when a message is received.
 One of the parameters that is sent from the server is the classname, so that the correct application can be called.
 The message is written to the Message queue file, and a broadcast is sent, because if the application is
 running, it can get the message.
 The message is also sent as notification so, if the app is not running, it can be opened.
 
 */

import java.util.Map;

import android.app.Notification; 
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import totalcross.Launcher4A;
import totalcross.AndroidUtils;

/**
 * Mudanças baseadas em https://developers.google.com/cloud-messaging/android/android-migrate-fcm
 */
public class FirebaseMessageReceiver extends FirebaseMessagingService {
	/**
	 * Called when message is received. ex: 
	 * time: 15:10 
	 * score: 5x1 
	 * collapse_key: do_not_collapse 
	 * From: 954430333411
	 */
	public void onMessageReceived(RemoteMessage message) {
		String from = message.getFrom();
		Map<String, String> data = message.getData();
		if (data != null) {
			try {
				AndroidUtils.debug("data received\n" + data);
				// get the parameters
				String title = data.get("title");
				String text = data.get("text");
				String info = data.get("info");
				String ticker = data.get("ticker");
				if (ticker == null) {
					ticker = title;
				}
				String appdata = data.get("appdata");
				if (appdata == null)  {
					appdata = title;
				}

				String classname = getClass().getName(); // totalcross.apptapi.firebase.FirebaseMessageReceiver
				classname = classname.replace("firebase.FirebaseMessageReceiver", "Loader");

				if (appdata == null) {
					throw new Exception("You must suply at least 'appdata' or 'title' parameter with the message; data received\n" + data);
				}

				// write to the program
				FirebaseUtils.writeMessage(this, classname, appdata);

				// send a broadcast if the vm is running
				FirebaseUtils.sendBroadcast(this, Launcher4A.MESSAGE_RECEIVED);

				// prepare the notification
				if (title != null) {
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
					if (text != null) {
						builder.setContentText(text);   // 2nd line small
					}
					if (info != null) {
						builder.setContentInfo(info);   // at right, smaller
					}
					builder.setContentIntent(PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));
					builder.setSmallIcon(totalcross.android.R.drawable.icon);
					builder.setTicker(ticker);
					notificationManager.notify(13121971, builder.getNotification());
				}
			} catch (Throwable t) {
				AndroidUtils.handleException(t, false);
			}
		}
	}
}
