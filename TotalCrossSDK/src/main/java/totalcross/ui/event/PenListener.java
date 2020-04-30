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

/** Interface used to listen to Pen events. */

public interface PenListener extends EventHandler {
  /** A PEN_DOWN event was dispatched.
   * @see PenEvent 
   */
  public void penDown(PenEvent e);

  /** A PEN_UP event was dispatched.
   * @see PenEvent 
   */
  public void penUp(PenEvent e);

  /** A PEN_DRAG event was dispatched.
   * @see PenEvent 
   */
  public void penDrag(DragEvent e); // guich@tc122_11: now a DragEvent

  /** A PEN_DRAG_START event was dispatched.
   * @see PenEvent 
   */
  public void penDragStart(DragEvent e); // guich@tc122_11: now a DragEvent

  /** A PEN_DRAG_END event was dispatched.
   * @see PenEvent 
   */
  public void penDragEnd(DragEvent e); // guich@tc122_11: now a DragEvent
}
