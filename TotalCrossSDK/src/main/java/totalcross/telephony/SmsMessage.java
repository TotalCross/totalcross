/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2017-2020 TotalCross Global Mobile Platform Ltda.   
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  This file is covered by the GNU LESSER GENERAL PUBLIC LICENSE VERSION 3.0    *
 *  A copy of this license is located in file license.txt at the root of this    *
 *  SDK or can be downloaded here:                                               *
 *  http://www.gnu.org/licenses/lgpl-3.0.txt                                     *
 *                                                                               *
 *********************************************************************************/

package totalcross.telephony;

/** A Short Message Service message. */
public class SmsMessage {

  String displayOriginatingAddress;
  String displayMessageBody;
  byte[] userData;

  private SmsMessage() {
  }

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

  public byte[] getUserData() {
    return userData;
  }
}
