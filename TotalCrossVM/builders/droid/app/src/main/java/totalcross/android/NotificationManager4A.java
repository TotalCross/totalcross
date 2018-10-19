package totalcross.android;

import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.app.NotificationManagerCompat;
import android.app.PendingIntent;
import android.content.Intent;
import totalcross.Launcher4A;

public class NotificationManager4A {

  private static String CHANNEL_ID = "totalcross";

  public static void notify(String title, String text) {
    // Create an explicit intent for an Activity in your app
    Intent intent =
        Launcher4A.instance
            .getContext()
            .getPackageManager()
            .getLaunchIntentForPackage(Launcher4A.instance.getContext().getPackageName());
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    PendingIntent pendingIntent =
        PendingIntent.getActivity(
            Launcher4A.instance.getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

    NotificationCompat.Builder mBuilder =
        new NotificationCompat.Builder(
                Launcher4A.instance.getContext()
                //        , CHANNEL_ID
                )
            .setSmallIcon(totalcross.android.R.drawable.ic_stat_icon_no_bg)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setColor(0xffc3352b)
            // Set the intent that will fire when the user taps the notification
            .setContentIntent(pendingIntent)
            .setAutoCancel(true);

    NotificationManagerCompat notificationManager =
        NotificationManagerCompat.from(Launcher4A.instance.getContext());
    // notificationId is a unique int for each notification that you must define
    notificationManager.notify(1, mBuilder.build());
  }
}
