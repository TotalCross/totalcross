package totalcross.android.firebase;

/**
 
 This class is called when a message is received.
 One of the parameters that is sent from the server is the classname, so that the correct application can be called.
 The message is written to the Message queue file, and a broadcast is sent, because if the application is
 running, it can get the message.
 The message is also sent as notification so, if the app is not running, it can be opened.
 
 */

import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.util.ArrayList;
import android.os.Bundle;
import android.os.Message;
import android.app.Notification; 
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import totalcross.Launcher4A;
import totalcross.AndroidUtils;
import totalcross.android.Loader;
import android.graphics.Color;
import android.os.Build;
import android.app.NotificationChannel;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;

/**
 * Mudanças baseadas em https://developers.google.com/cloud-messaging/android/android-migrate-fcm
 */
public class FirebaseMessageReceiver extends FirebaseMessagingService {

    private static final String NOTIFICATION_ID_EXTRA = "notificationId";
    private static final String IMAGE_URL_EXTRA = "imageUrl";
    private static final String ADMIN_CHANNEL_ID ="admin_channel";
    private NotificationManager notificationManager;
    
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
				//callback totalcross.firebase.FireBaseManager.onMessageReceived 
				Set entries = data.entrySet();
				ArrayList<String> keys = null; 
				ArrayList<String> values = null;
				
				if(entries.size() > 0) {
					keys = new ArrayList<String> (entries.size());
					values = new ArrayList<String> (entries.size());
					Iterator iter = entries.iterator();
					while (iter.hasNext()) {
						Map.Entry me = (Map.Entry) iter.next();
						keys.add((String) me.getKey());
						values.add((String) me.getValue());
					}
				}
				String messageId = message.getMessageId();
				String messageType = message.getMessageType();
				String collapseKey = message.getCollapseKey();

				Message msg = Launcher4A.viewhandler.obtainMessage();
			    Bundle b = new Bundle();
			    b.putInt("type", Launcher4A.FIREBASE_MSG_RCVD);
			    b.putByteArray("messageId", messageId == null ? null : messageId.getBytes());
			    b.putByteArray("messageType", messageType == null ? null : messageId.getBytes());
			    b.putStringArrayList("keys", keys);
			    b.putStringArrayList("values", values);
			    b.putByteArray("collapseKey", collapseKey == null ? null : collapseKey.getBytes());
			    b.putInt("ttl", message.getTtl());
			    msg.setData(b);
			    Launcher4A.viewhandler.sendMessage(msg);

//				AndroidUtils.debug("data received\n" + data);
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
					return;
					// throw new Exception("You must suply at least 'appdata' or 'title' parameter with the message; data received\n" + data);
				}

				// write to the program
				FirebaseUtils.writeMessage(this, classname, appdata);

				// send a broadcast if the vm is running
				FirebaseUtils.sendBroadcast(this, Launcher4A.MESSAGE_RECEIVED);

				// prepare the notification
				if (title != null) {
					notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
					
			        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
			            setupChannels();
			        }
					
					int dot = classname.lastIndexOf('.');
					String pack = classname.substring(0,dot); //"totalcross.app.totalcrossapi";
					String cls  = classname.substring(dot+1); //"TotalCrossAPI";

					Intent intent = new Intent("android.intent.action.MAIN");
					intent.setClassName(pack,pack+"."+cls);
					intent.putExtra("cmdline","/pushnotification");

					NotificationCompat.Builder builder = new NotificationCompat.Builder(this, ADMIN_CHANNEL_ID);
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
					notificationManager.notify(13121971, builder.build());
				}
			} catch (Exception t) {
				AndroidUtils.handleException(t, false);
			}
		}
	}
	
	private static String getApplicationName(Context context) {
	    return context.getApplicationInfo().loadLabel(context.getPackageManager()).toString();
	}
	
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupChannels(){
        CharSequence adminChannelName = getApplicationName(this); //"TotalCross channel"; //getString(R.string.notifications_admin_channel_name);
        String adminChannelDescription = adminChannelName + " notification channel"; //getString(R.string.notifications_admin_channel_description);

        NotificationChannel adminChannel;
        adminChannel = new NotificationChannel(ADMIN_CHANNEL_ID, adminChannelName, NotificationManager.IMPORTANCE_HIGH);
        adminChannel.setDescription(adminChannelDescription);
        adminChannel.enableLights(true);
        adminChannel.setLightColor(Color.RED);
        adminChannel.enableVibration(true);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(adminChannel);
        }
    }
}
