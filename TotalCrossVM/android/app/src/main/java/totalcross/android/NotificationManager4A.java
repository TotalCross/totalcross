package totalcross.android;

import android.graphics.Color;
import android.os.Build;
import android.app.NotificationManager;
import android.app.NotificationChannel;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.app.NotificationManagerCompat;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import totalcross.Launcher4A;
import android.support.annotation.RequiresApi;

public class NotificationManager4A {

  private static final String NOTIFICATION_ID_EXTRA = "notificationId";
  private static final String IMAGE_URL_EXTRA = "imageUrl";
  private static final String ADMIN_CHANNEL_ID ="admin_channel";

  public static void notify(String title, String text) {
    final Context context = Launcher4A.instance.getContext();
    // Create an explicit intent for an Activity in your app
    Intent intent = context
            .getPackageManager()
            .getLaunchIntentForPackage(context.getPackageName());
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    PendingIntent pendingIntent =
        PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

    NotificationCompat.Builder builder =
        new NotificationCompat.Builder(context, ADMIN_CHANNEL_ID)
            .setSmallIcon(totalcross.android.R.drawable.ic_stat_icon_no_bg)
            .setContentTitle(title)
            .setContentText(text)
            // Set the intent that will fire when the user taps the notification
            .setContentIntent(pendingIntent)
            .setAutoCancel(true);

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        setupChannels(context, notificationManager);
    } else {
      builder.setPriority(NotificationCompat.PRIORITY_HIGH)
              .setColor(0xffc3352b)
              //.setColor(ContextCompat.getColor(context, R.color.transparent))
              .setVibrate(new long[]{100, 250})
              .setLights(Color.YELLOW, 500, 5000)
              //.setAutoCancel(true)
              ;
    }

    notificationManager.notify(13121971, builder.build());
  }
  
  private static String getApplicationName(Context context) {
      return context.getApplicationInfo().loadLabel(context.getPackageManager()).toString();
  }
  
  @RequiresApi(api = Build.VERSION_CODES.O)
  private static void setupChannels(Context context, NotificationManager notificationManager){
      CharSequence adminChannelName = getApplicationName(context); //"TotalCross channel"; //getString(R.string.notifications_admin_channel_name);
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
