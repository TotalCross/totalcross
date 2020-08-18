// Copyright (C) 2017-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.telephony;

/** Listener for the sms messages received by the device */
public interface SmsReceiver {
  void onReceive(SmsMessage message);
}
