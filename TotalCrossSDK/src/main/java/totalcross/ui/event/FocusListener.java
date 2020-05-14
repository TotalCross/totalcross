// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.event;

/** Interface used to listen to Focus events. */

public interface FocusListener extends EventHandler {
  /** A FOCUS_IN event was dispatched.
   * @see ControlEvent 
   */
  public void focusIn(ControlEvent e);

  /** A FOCUS_OUT event was dispatched. 
   * @see ControlEvent 
   */
  public void focusOut(ControlEvent e);
}
