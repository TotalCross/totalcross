package totalcross.telephony;

public class SmsManager {

  private static SmsManager instance;

  private SmsManager() {}

  /** Get the default SmsManager. */
  public static SmsManager getDefault() {
    if (instance == null) {
      instance = new SmsManager();
    }
    return instance;
  }

  SmsReceiver receiver;

  /**
   * Registers a receiver to listen for incoming sms messages
   *
   * @param receiver the receiver that will handle incoming sms messages or null to stop listening
   *     incoming messages
   */
  public void registerSmsReceiver(SmsReceiver receiver) {
    this.receiver = receiver;
  }

  public native void registerSmsReceiver4D(SmsReceiver receiver);

  /**
   * Send a text based SMS.
   *
   * @param destinationAddress the address to send the message to
   * @param scAddress the service center address or null to use the current default SMSC
   * @param text the body of the message to send
   */
  public void sendTextMessage(String destinationAddress, String scAddress, String text) {
    if (destinationAddress.length() == 0) {
      throw new IllegalArgumentException("Argument destinationAddress cannot be empty");
    }
    if (text.length() == 0) {
      throw new IllegalArgumentException("Argument text cannot be empty");
    }
    System.out.println("To: " + destinationAddress + ", From: " + scAddress + ", Text: " + text);
  }

  public native void sendTextMessage4D(String destinationAddress, String scAddress, String text);
}
