// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.event;

/** Interface used to listen to Window events. */

public interface WindowListener extends EventHandler {
  /** A WINDOW_CLOSED event was dispatched.
   * @see ControlEvent 
   */
  public void windowClosed(ControlEvent e);
}
