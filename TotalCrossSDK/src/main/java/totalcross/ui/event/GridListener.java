// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.event;

/** Interface used to listen to Grid events. */

public interface GridListener extends EventHandler {
  /** A GRID_SELECTED_EVENT event was dispatched.
   * @see GridEvent 
   */
  public void gridSelected(GridEvent e);

  /** A CHECK_CHANGED_EVENT event was dispatched.
   * @see GridEvent 
   */
  public void gridCheckChanged(GridEvent e);

  /** A TEXT_CHANGED_EVENT event was dispatched.
   * @see GridEvent 
   */
  public void gridTextChanged(GridEvent e);
}
