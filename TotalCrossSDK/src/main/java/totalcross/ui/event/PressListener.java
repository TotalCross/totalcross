// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.event;

/** Interface used to listen to Press events. */

public interface PressListener extends EventHandler {
  /** A PRESSED event was dispatched.
   * @see ControlEvent 
   */
  public void controlPressed(ControlEvent e);
}
