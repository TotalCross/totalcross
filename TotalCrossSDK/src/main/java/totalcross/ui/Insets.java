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

/** Used to define the gaps between the container and its child controls. */

public class Insets {
  /** The inset from the top. */
  public int top;

  /** The inset from the left. */
  public int left;

  /** The inset from the bottom. */
  public int bottom;

  /** The inset from the right. */
  public int right;

  /**
   * Creates a new Insets object.
   */
  public Insets() {
    this(0, 0, 0, 0);
  }

  /**
   * Creates and initializes a new Insets object with the specified top, left,
   * bottom and right insets.
   * @param top The inset from the top.
   * @param left The inset from the left.
   * @param bottom The inset from the bottom.
   * @param right The inset from the right.
   */
  public Insets(int top, int left, int bottom, int right) {
    this.top = top;
    this.left = left;
    this.bottom = bottom;
    this.right = right;
  }

  /** Sets the fields with the given values.
   * @since TotalCross 1.3
   */
  public void set(int top, int left, int bottom, int right) {
    this.top = top;
    this.left = left;
    this.bottom = bottom;
    this.right = right;
  }

  /**
   * Copy the insets from another Insets object.
   * @param other The other Insets object.
   */
  public void copyFrom(Insets other) {
    top = other.top;
    left = other.left;
    bottom = other.bottom;
    right = other.right;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Insets)) {
      return false;
    }

    Insets other = (Insets) obj;
    return top == other.top && left == other.left && bottom == other.bottom && right == other.right;
  }

  @Override
  public int hashCode() {
    return ((top + 16) << 24) | ((bottom + 16) << 16) | ((right + 16) << 8) | (left + 16); // somewhat dificult that an inset is less than -16 or greater than 240
  }
}
