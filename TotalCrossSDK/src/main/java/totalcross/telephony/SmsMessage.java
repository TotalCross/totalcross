package totalcross.telephony;

/** A Short Message Service message. */
public class SmsMessage {

  String displayOriginatingAddress;
  String displayMessageBody;

  private SmsMessage() {}

  /**
   * Returns the originating address, or email from address if this message was from an email
   * gateway. Returns null if originating address unavailable.
   */
  public String getDisplayOriginatingAddress() {
    return displayOriginatingAddress;
  }

  /**
   * Returns the message body, or email message body if this message was from an email gateway.
   * Returns null if message body unavailable.
   */
  public String getDisplayMessageBody() {
    return displayMessageBody;
  }
}
