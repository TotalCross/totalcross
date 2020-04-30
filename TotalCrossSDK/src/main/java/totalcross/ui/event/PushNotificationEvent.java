// Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>   
// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.event;

import totalcross.sys.Vm;
import totalcross.ui.MainWindow;

/**
 * TimerEvent represents a control's timer. Timers are created and destroyed using
 * the addTimer() and removeTimer() methods present in the Control class.
 */
@Deprecated
public class PushNotificationEvent extends Event<PushNotificationListener> {
  /** The event type for a token for this device being received by server */
  public static final int TOKEN_RECEIVED = EventType.TOKEN_RECEIVED;
  /** The event type for a message being received by server */
  public static final int MESSAGE_RECEIVED = EventType.MESSAGE_RECEIVED;

  /** The message or the token, depending on the event type */
  public String message;

  @Deprecated
  public PushNotificationEvent(int type, String msg) {
    super(type, MainWindow.getMainWindow(), Vm.getTimeStamp());
    message = msg;
  }

  @Override
  public String toString() {
    return (type == TOKEN_RECEIVED ? "TOKEN_RECEIVED" : "MESSAGE_RECEIVED") + ": " + message;
  }

  @Override
  public void dispatch(PushNotificationListener listener) {
    // TODO Auto-generated method stub
    
  }
}
