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

/**
 * TimerEvent represents a control's timer. Timers are created and destroyed using
 * the addTimer() and removeTimer() methods present in the Control class.
 */

public class TimerEvent extends Event<TimerListener> {
  /** The event type for a triggered timer */
  public static final int TRIGGERED = EventType.TRIGGERED;
  /** The timer interval in milliseconds. */
  public int millis;
  /** The timestamp of the last tick. */
  public int lastTick;
  /** The next timer in the linked list. */
  public TimerEvent next;
  /** a flag set when the timer event is being posted. With it, you can test from various timers to the same target control when one of them is dispatched. */
  public boolean triggered; // guich@220_39

  /** Constructs a new TimerEvent, setting the type to TRIGGERED. */
  public TimerEvent() {
    type = TRIGGERED;
  }

  @Override
  public String toString() {
    return "TRIGGERED millis: " + millis + " " + super.toString();
  }

  public void postpone() {
    lastTick = totalcross.sys.Vm.getTimeStamp();
  }

  @Override
  public void dispatch(TimerListener listener) {
    listener.timerTriggered(this);
  }
}
