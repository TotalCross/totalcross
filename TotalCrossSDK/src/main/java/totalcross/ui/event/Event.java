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

import com.totalcross.annotations.ReplacedByNativeOnDeploy;
import totalcross.ui.Window;

/**
 * Event is the base class for all events.
 */

public abstract class Event<H extends EventHandler> {
  
  public static class Type<H> {
    
  }
  
  /** The type of event. */
  public int type;

  /**
   * The target of the event. For user-interface events, this is the
   * control the event is associated with.
   */
  public Object target;

  /**
   * The event's timestamp.
   * @see totalcross.sys.Vm#getTimeStamp()
   */
  public int timeStamp;

  /**
   * Set to true to break the event propagation up to the parents
   */
  public boolean consumed;

  /** Constructs a new event based in the given parameters. */
  public Event(int type, Object target, int timeStamp) {
    this.type = type;
    this.target = target;
    this.timeStamp = timeStamp;
  }

  /** Constructs a new event with the current time stamp */
  public Event() {
    timeStamp = totalcross.sys.Vm.getTimeStamp(); // guich@200b4: removed this from the other subclasses and putted here.
  }

  /** Updates the timeStamp parameter with the current time */
  public void touch() {
    timeStamp = totalcross.sys.Vm.getTimeStamp(); // guich@200b4: removed this from the other subclasses and putted here.
  }

  /** The next available event number */
  private static int nextAvailableEventId = 10000;

  /** Register a new event with the system. Use it to avoid
   * conflict of events created by multiple Applications. */
  public static int getNextAvailableEventId() {
    return nextAvailableEventId++;
  }

  /** Returns true if there is an event available in the VM event queue. */
  @ReplacedByNativeOnDeploy
  public static boolean isAvailable() {
    return totalcross.Launcher.instance.eventIsAvailable();
  }

  @Override
  public String toString() {
    return "target: " + target + " (" + super.toString() + ")";
  }

  /** Clears the event queue. 
   * @param type The Event type to be ignored, or 0 to ignore all types.
   * 
   * @since TotalCross 1.2
   */
  public static void clearQueue(int type) // guich@tc120_44
  {
    Window.ignoreEventOfType = type;
    while (isAvailable()) {
      Window.pumpEvents();
    }
    Window.ignoreEventOfType = -1;
  }
 
  abstract public void dispatch(H listener);
  
//  abstract public Type<H> getAssociatedType();
}
