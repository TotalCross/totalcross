// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.event;

/** Interface used to listen to Highlight events. */

public interface HighlightListener extends EventHandler {
  /** A HIGHLIGHT_IN event was dispatched.
   * @see ControlEvent 
   */
  public void highlightIn(ControlEvent e);

  /** A HIGHLIGHT_OUT event was dispatched.
   * @see ControlEvent 
   */
  public void highlightOut(ControlEvent e);
}
