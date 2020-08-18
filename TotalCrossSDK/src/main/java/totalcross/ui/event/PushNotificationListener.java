// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.event;

/** Interface used to listen to Push notification events. */

public interface PushNotificationListener extends EventHandler {
  public void tokenReceived(PushNotificationEvent e);

  public void messageReceived(PushNotificationEvent e);
}
