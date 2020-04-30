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

import totalcross.ui.Control;

/**
 * MultiTouchEvent works on devices that support more than one finger at a time.
 * 
 * It can be emulated in JavaSE by using the right mouse button:
 * <ol>
 * <li> Right-click and go up: the scale is increased. release the button.
 * <li> Right-click and go down: the scale is decreased. release the button.
 * </ol>
 */

public class MultiTouchEvent extends Event<MultiTouchListener> {
  /** The event type for a pen or mouse down. */
  public static final int SCALE = EventType.SCALE;

  protected static final String[] EVENT_NAME = { "SCALE" };

  /** The current scale value. */
  public double scale;

  /** Updates this event setting also the timestamp, consumed and target.
   * @since TotalCross 1.0
   */
  public MultiTouchEvent update(Control c, double scale) {
    this.type = SCALE;
    timeStamp = totalcross.sys.Vm.getTimeStamp(); // guich@200b4: removed this from the other subclasses and putted here.
    consumed = false;
    target = c;
    this.scale = scale;
    return this;
  }

  /** Returns the event name. Used to debugging. */
  public static String getEventName(int type) {
    return SCALE <= type && type <= SCALE ? EVENT_NAME[type - 250] : "Not a MultiTouchEvent";
  }

  @Override
  public String toString() {
    return EVENT_NAME[type - 250] + " scale: " + scale + " " + super.toString();
  }

  @Override
  public void dispatch(MultiTouchListener listener) {
    listener.scale(this);
  }
}
