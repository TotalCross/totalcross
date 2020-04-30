// Copyright (C) 2017-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.telephony;

import com.totalcross.annotations.ReplacedByNativeOnDeploy;

public class SmsManager {

  private static SmsManager instance;

  private SmsManager() {
  }

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
   * @param port the port to listen for incoming sms messages - required for data sms messages, pass -1 to listen on default port for text messages
   */
  @ReplacedByNativeOnDeploy
  public void registerSmsReceiver(SmsReceiver receiver, int port) {
    this.receiver = receiver;
  }

  /**
   * Registers a receiver to listen for incoming text sms messages
   *
   * @param receiver the receiver that will handle incoming text sms messages or null to stop listening
   *     incoming messages
   */
  public void registerSmsReceiver(SmsReceiver receiver) {
    this.registerSmsReceiver(receiver, -1);
  }

  /**
   * Send a text based SMS.
   *
   * @param destinationAddress the address to send the message to
   * @param scAddress the service center address or null to use the current default SMSC
   * @param text the body of the message to send
   */
  @ReplacedByNativeOnDeploy
  public void sendTextMessage(String destinationAddress, String scAddress, String text) {
    if (destinationAddress.length() == 0) {
      throw new IllegalArgumentException("Argument destinationAddress cannot be empty");
    }
    if (text.length() == 0) {
      throw new IllegalArgumentException("Argument text cannot be empty");
    }
    System.out.println("To: " + destinationAddress + ", From: " + scAddress + ", Text: " + text);
  }

  /**
   * Send a data based SMS to a specific application port.
   *
   * @param destinationAddress the address to send the message to
   * @param scAddress the service center address or null to use the current default SMSC
   * @param port the port to deliver the message to
   * @param data the body of the message to send
   */
  @ReplacedByNativeOnDeploy
  public void sendDataMessage(String destinationAddress, String scAddress, int port, byte[] data) {
    if (destinationAddress.length() == 0) {
      throw new IllegalArgumentException("Argument destinationAddress cannot be empty");
    }
    if (data.length == 0) {
      throw new IllegalArgumentException("Argument data cannot be empty");
    }
    System.out.println("To: " + destinationAddress + ", From: " + scAddress + ", Data with length: " + data.length);
  }
}
