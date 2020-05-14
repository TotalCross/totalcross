// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.event;

/** Interface used to listen to MultiTouch events. */

public interface MultiTouchListener extends EventHandler {
  /** A SCALE event was dispatched.
   * @see PenEvent 
   */
  public void scale(MultiTouchEvent e);
}
