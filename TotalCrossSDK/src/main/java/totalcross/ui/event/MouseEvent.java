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
 * MouseEvent is a mouse move, in a control or out a control events.
 * Only occurs on devices with a mouse, like Windows 32 and Linux desktop.
 * 
 * @since TotalCross 1.27
 */

public class MouseEvent extends PenEvent {
  /** @see totalcross.ui.event.DragEvent 
   */
  public int wheelDirection;

  /** The event type for a mouse moving over a control.
   * This is a hardware event. 
   */
  public static final int MOUSE_MOVE = EventType.MOUSE_MOVE;

  /** The event type for a mouse moving into a control.
   * This is a software event (computed internally). 
   */
  public static final int MOUSE_IN = EventType.MOUSE_IN;

  /** The event type for a mouse moving outside a control. 
   * This is a software event (computed internally). 
   */
  public static final int MOUSE_OUT = EventType.MOUSE_OUT;

  /** The event type for a mouse wheel.
   * This is a hardware event. 
   */
  public static final int MOUSE_WHEEL = EventType.MOUSE_WHEEL;
}
