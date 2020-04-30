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
