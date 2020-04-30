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

/** An event generated when the user clicks on a grid or checks it.
 * @since SuperWaba 5.54
 * */

public class GridEvent extends Event<GridListener> {
  /** Event generated when a new row was selected. In penless devices, the user must press 0-9 to dispatch the event. */
  public static final int SELECTED_EVENT = EventType.GRID_SELECTED_EVENT;
  /** Event generated when a grid row was checked or unchecked.
   * Verify the checked member to determine the current state. */
  public static final int CHECK_CHANGED_EVENT = EventType.GRID_CHECK_CHANGED_EVENT;
  /** Generated when an editable column had its text changed. */
  public static final int TEXT_CHANGED_EVENT = EventType.GRID_TEXT_CHANGED_EVENT;

  /** True if the column is checked. On grid that has no check column, this member is useless. */
  public boolean checked;

  /** Stores the target row of this grid event. If the user checked all checks (by clicking in the header check),
   * it is Grid.ALL_CHECKED; otherwise, if the user unchecked all lines, it is Grid.ALL_UNCHECKED. */
  public int row;
  /** Stores the target column of this grid event. On grid that has a check column, the columns text starts from 1,
   * otherwise, if no check, it starts from 0.
   */
  public int col;

  @Override
  public String toString() {
    String s = "";
    switch (type) {
    case SELECTED_EVENT:
      s = "GRID_SELECTED_EVENT";
      break;
    case CHECK_CHANGED_EVENT:
      s = "CHECK_CHANGED_EVENT";
      break;
    case TEXT_CHANGED_EVENT:
      s = "TEXT_CHANGED_EVENT";
      break;
    }
    return s + " row:" + row + ", col: " + col + ", checked: " + checked + " " + super.toString();
  }

  @Override
  public void dispatch(GridListener listener) {
    switch (this.type) {
    case SELECTED_EVENT:
      listener.gridSelected(this);
      break;
    case CHECK_CHANGED_EVENT:
      listener.gridCheckChanged(this);
      break;
    case TEXT_CHANGED_EVENT:
      listener.gridTextChanged(this);
      break;
    }
  }
}
