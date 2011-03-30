/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000 Tom Cuthill                                               *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
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

// $Id: ScanEvent.java,v 1.10 2011-01-04 13:19:26 guich Exp $

package totalcross.io.device.scanner;

import totalcross.ui.event.*;

/**
 * ScanEvent is an event thrown by the barcode scanner.
 * Used in the Scanner class.
 * @author Tom Cuthill (Palm Solutions) 16/3/2000
 */
public class ScanEvent extends Event
{
   /** The event type for a scanner scanning event. Note that since they are set at runtime, you cannot use them in a switch statement.
    * Caution: do not call popup when handling this event, or the application will be blocked; use popupNonBlocking instead. */
   public static final int SCANNED = 1101;
   /** The event type for a scanner low battery event. Note that since they are set at runtime, you cannot use them in a switch statement.
    * Caution: do not call popup when handling this event, or the application will be blocked; use popupNonBlocking instead. */
   public static final int BATTERY_ERROR = 1102;
   /** The event type for a scanner triggered: A scan attempt was initiated - hard or soft trigger. Note that since they are set at runtime, you cannot use them in a switch statement.
    * Caution: do not call popup when handling this event, or the application will be blocked; use popupNonBlocking instead. */
   public static final int TRIGGERED = 1103;

   /**
   * The data resulting from the scan when type is <code>SCANNED</code>.
   */
   public String data;

   /**
   * Updates a scan event with the given type.
   * target is always null.
   */
   public void update(int type)
   {
      touch();
      this.type = type;
      data = type == SCANNED ? Scanner.getData() : null;
   }
}

