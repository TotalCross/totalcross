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

/** Interface used to listen to mouse move events.
 * @since TotalCross 1.27 
 */

public interface MouseListener extends EventHandler {
  /** A MOUSE_MOVE event was dispatched.
   * @see PenEvent 
   */
  public void mouseMove(MouseEvent e);

  /** A MOUSE_IN event was dispatched when the mouse was going into a control.
   * @see PenEvent 
   */
  public void mouseIn(MouseEvent e);

  /** A MOUSE_OUT event was dispatched when the mouse was going out of a control.
   * @see PenEvent 
   */
  public void mouseOut(MouseEvent e);

  /** The event type for a mouse wheel moving down.
   * This is a hardware event. 
   */
  public void mouseWheel(MouseEvent e);

}
