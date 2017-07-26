/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>                         *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
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



package totalcross.ui.event;

import totalcross.sys.*;
import totalcross.ui.*;

/**
 * TimerEvent represents a control's timer. Timers are created and destroyed using
 * the addTimer() and removeTimer() methods present in the Control class.
 */
@Deprecated
public class PushNotificationEvent extends Event
{
   /** The event type for a token for this device being received by server */
   public static final int TOKEN_RECEIVED = 360;
   /** The event type for a message being received by server */
   public static final int MESSAGE_RECEIVED = 361;
   
   /** The message or the token, depending on the event type */
   public String message;

   @Deprecated
   public PushNotificationEvent(int type, String msg)
   {
      super(type, MainWindow.getMainWindow(), Vm.getTimeStamp());
      message = msg;
   }
   
   public String toString()
   {
      return (type == TOKEN_RECEIVED ? "TOKEN_RECEIVED" : "MESSAGE_RECEIVED")+": "+message;
   }
}
