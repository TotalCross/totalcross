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

/**
 * TimerEvent represents a control's timer. Timers are created and destroyed using
 * the addTimer() and removeTimer() methods present in the Control class.
 */

public class TimerEvent extends Event
{
   /** The event type for a triggered timer */
   public static final int TRIGGERED = 350;
   /** The timer interval in milliseconds. */
   public int millis;
   /** The timestamp of the last tick. */
   public int lastTick;
   /** The next timer in the linked list. */
   public TimerEvent next;
   /** a flag set when the timer event is being posted. With it, you can test from various timers to the same target control when one of them is dispatched. */
   public boolean triggered; // guich@220_39

   /** Constructs a new TimerEvent, setting the type to TRIGGERED. */
   public TimerEvent()
   {
      type = TRIGGERED;
   }
   
   public String toString()
   {
      return "TRIGGERED millis: "+millis+" "+super.toString();
   }
}
