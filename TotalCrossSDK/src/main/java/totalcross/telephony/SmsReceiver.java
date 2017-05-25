package totalcross.telephony;

/** Listener for the sms messages received by the device */
public interface SmsReceiver {
  void onReceive(SmsMessage message);
}
