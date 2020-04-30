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
 * ControlEvent is an event posted by a control.
 */

public class ControlEvent extends Event<ControlListener> {
  /** The event type for a pressed event. */
  public static final int PRESSED = EventType.PRESSED;
  /** The event type for a focus in event. */
  public static final int FOCUS_IN = EventType.FOCUS_IN;
  /** The event type for a focus out event. */
  public static final int FOCUS_OUT = EventType.FOCUS_OUT;
  /** The event type for a closing window. */
  public static final int WINDOW_CLOSED = EventType.WINDOW_CLOSED;
  /** The event type for the control focus indicator changing to a new control. */
  public static final int HIGHLIGHT_IN = EventType.HIGHLIGHT_IN;
  /** The event type for the control focus indicator leaving a control. */
  public static final int HIGHLIGHT_OUT = EventType.HIGHLIGHT_OUT;
  /** The event type fot the SIP being closed by the system. Works on Android and iOS. 
   * The application cannot see this event since it is interpected by the topmost Window.
   * @since TotalCross 1.3
   */
  public static final int SIP_CLOSED = EventType.SIP_CLOSED;
  /** Event sent when user called Edit.setCursorPos
   * @since TotalCross 1.5 
   */
  public static final int CURSOR_CHANGED = EventType.CURSOR_CHANGED;

  /** Constructs an empty ControlEvent. */
  public ControlEvent() {
  }

  /**
   * Constructs a control event of the given type.
   * @param type the type of event
   * @param c the target control
   */
  public ControlEvent(int type, Control c) {
    this.type = type;
    target = c;
    timeStamp = totalcross.sys.Vm.getTimeStamp(); // guich@200b4: removed this from the other subclasses and putted here.
  }

  /** Updates the control event setting the timestamp, consumed and target.
   * @since TotalCross 1.0
   */
  public ControlEvent update(Control c) {
    timeStamp = totalcross.sys.Vm.getTimeStamp(); // guich@200b4: removed this from the other subclasses and putted here.
    consumed = false;
    target = c;
    return this;
  }

  @Override
  public String toString() {
    String s = "";
    switch (type) {
    case PRESSED:
      s = "PRESSED";
      break;
    case FOCUS_IN:
      s = "FOCUS_IN";
      break;
    case FOCUS_OUT:
      s = "FOCUS_OUT";
      break;
    case WINDOW_CLOSED:
      s = "WINDOW_CLOSED";
      break;
    case HIGHLIGHT_IN:
      s = "HIGHLIGHT_IN";
      break;
    case HIGHLIGHT_OUT:
      s = "HIGHLIGHT_OUT";
      break;
    }
    return s + " " + super.toString();
  }

  @Override
  public void dispatch(ControlListener listener) {
    // TODO Auto-generated method stub
    
  }

}
