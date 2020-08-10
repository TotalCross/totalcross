package totalcross.android;

import android.telephony.SmsManager;
import android.content.Intent;
import android.net.Uri;
import totalcross.Launcher4A;

public class SmsManager4A {

  public static void sendTextMessage(String destinationAddress, String scAddress, String text) {
    SmsManager smsManager = SmsManager.getDefault();
    // Requires SEND_SMS permission
     smsManager.sendTextMessage(destinationAddress, null, text, null, null);
  }

  public static void enableSmsReceiver(boolean enabled, int port) {
    Launcher4A.enableSmsReceiver(enabled, port);
  }
  
  public static void sendDataMessage(String destinationAddress, String scAddress, int port, byte[] data) {
    SmsManager smsManager = SmsManager.getDefault();
    smsManager.sendDataMessage(destinationAddress, null, (short) port, data, null, null);
  }
}
