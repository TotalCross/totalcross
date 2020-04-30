// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui;

/** This class represents a menu item. It is used in conjunction with the MenuBar and MenuBarDropDown classes.
 * @since SuperWaba 5.8
 */

public class MenuItem {
  /** The menu item's caption. */
  public String caption;

  /** True if this item is checked. Note that you must use the constructor
   * that specifies the starting state of the check (if set or unset), otherwise
   * changing this will not work.
   */
  public boolean isChecked;

  /** True if this item is enabled, false otherwise. */
  public boolean isEnabled = true;

  boolean isCheckable;
  boolean isSeparator;

  /** Constructs a separator menu item. */
  public MenuItem() {
    isSeparator = true;
    caption = "";
  }

  /** Constructs a menu item with the given caption. This item CANNOT be checked (to allow that, 
   * use the other constructor with 2 parameters), but can be enabled/disabled.
   * @see #MenuItem(String, boolean) 
   */
  public MenuItem(String caption) {
    this.caption = caption;
  }

  /** Constructs a menu item with the given caption and the given default state for the check.
   * This item CAN be checked and can be enabled/disabled as well.
   */
  public MenuItem(String caption, boolean isChecked) {
    this.caption = caption;
    this.isCheckable = true;
    this.isChecked = isChecked;
  }

  /** Returns true if this menu item is checkable. */
  public boolean isCheckable() {
    return this.isCheckable;
  }
}
