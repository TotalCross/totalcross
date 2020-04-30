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
 * PenEvent is a pen down, up, move or drag event.
 * <p>
 * A pen drag occurs when the pen moves while the screen is pressed.
 */

public class PenEvent extends Event<PenListener> implements Cloneable {
  /** The event type for a pen or mouse down. */
  public static final int PEN_DOWN = EventType.PEN_DOWN;
  /** The event type for a pen or mouse up. */
  public static final int PEN_UP = EventType.PEN_UP;
  /** The event type for a pen or mouse drag. */
  public static final int PEN_DRAG = EventType.PEN_DRAG;
  /** The event type for a pen or mouse drag start. */
  public static final int PEN_DRAG_START = EventType.PEN_DRAG_START; // kmeehl@tc100
  /** The event type for a pen or mouse drag end. */
  public static final int PEN_DRAG_END = EventType.PEN_DRAG_END; // kmeehl@tc100

  protected static final String[] EVENT_NAME = { "PEN_DOWN", "PEN_UP", "PEN_DRAG", "PEN_DRAG_START", "PEN_DRAG_END",
      "MOUSE_MOVE", "MOUSE_IN", "MOUSE_OUT", "MOUSE_WHEEL" };

  /** The x location of the event. */
  public int x;

  /** The y location of the event. */
  public int y;

  /** The absolute x location of the event. */
  public int absoluteX;

  /** The absolute y location of the event. */
  public int absoluteY;

  /**
   * The state of the modifier keys when the event occured. This is a
   * OR'ed combination of the modifiers present in the DeviceKeys interface.
   * @see totalcross.sys.SpecialKeys
   */
  public int modifiers;

  /** Updates this event setting also the timestamp, consumed and target.
   * @since TotalCross 1.0
   */
  public PenEvent update(Control c, int absoluteX, int x, int absoluteY, int y, int type, int modifiers) {
    this.absoluteX = absoluteX;
    this.x = x;
    this.absoluteY = absoluteY;
    this.y = y;
    this.type = type;
    timeStamp = totalcross.sys.Vm.getTimeStamp(); // guich@200b4: removed this from the other subclasses and putted here.
    consumed = false;
    target = c;
    this.modifiers = modifiers;
    return this;
  }

  /** Returns the event name. Used to debugging. */
  public static String getEventName(int type) {
    return PEN_DOWN <= type && type <= PEN_DRAG_END ? EVENT_NAME[type - 200] : "Not a PEN_EVENT";
  }

  @Override
  public String toString() {
    return EVENT_NAME[type - 200] + " pos: " + x + "," + y + " " + super.toString();
  }

  @Override
  public PenEvent clone() {
    PenEvent clone = new PenEvent();
    clone.x = x;
    clone.y = y;
    clone.type = type;
    clone.absoluteX = absoluteX;
    clone.absoluteY = absoluteY;
    clone.modifiers = modifiers;
    clone.target = target;
    clone.timeStamp = timeStamp;
    return clone;
  }

  @Override
  public void dispatch(PenListener listener) {
    switch (this.type) {
    case PEN_DOWN:
      listener.penDown(this);
      break;
    case PEN_UP:
      listener.penUp(this);
      break;
    case PEN_DRAG:
      listener.penDrag((DragEvent) this);
      break;
    case PEN_DRAG_START:
      listener.penDragStart((DragEvent) this);
      break;
    case PEN_DRAG_END:
      listener.penDragEnd((DragEvent) this);
      break;
    }
  }
}
