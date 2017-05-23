package totalcross.android;

import android.telephony.SmsManager;
import android.content.Intent;
import android.net.Uri;
import totalcross.Launcher4A;

public class SmsManager4A {

  public static void sendTextMessage(String destinationAddress, String scAddress, String text) {
    SmsManager smsManager = SmsManager.getDefault();
    // Requires SEND_SMS permission
    // smsManager.sendTextMessage(destinationAddress, null, text, null, null);

    Intent sendIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + destinationAddress));
    sendIntent.putExtra("sms_body", text);
    Launcher4A.startActivity(sendIntent);
  }

  public static void enableSmsReceiver(boolean enabled) {
    Launcher4A.enableSmsReceiver(enabled);
  }
}
