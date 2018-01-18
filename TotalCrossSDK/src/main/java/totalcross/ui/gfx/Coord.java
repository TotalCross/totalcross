/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  This file is covered by the GNU LESSER GENERAL PUBLIC LICENSE VERSION 3.0    *
 *  A copy of this license is located in file license.txt at the root of this    *
 *  SDK or can be downloaded here:                                               *
 *  http://www.gnu.org/licenses/lgpl-3.0.txt                                     *
 *                                                                               *
 *********************************************************************************/

package totalcross.ui.gfx;

/**
 * Coord is a coordinate with x and y values.
 */

public class Coord {
  /** x position */
  public int x;
  /** y position */
  public int y;

  /** Constructs a coordinate with x = y = 0. */
  public Coord() {
    // they are already 0 as default.
  }

  /** Constructs a coordinate with the given x, y. */
  public Coord(int x, int y) {
    this.x = x;
    this.y = y;
  }

  /** Translates the current coordinate making x += dx and y += dy */
  public void translate(int dx, int dy) {
    x += dx;
    y += dy;
  }

  /** return's x. just to make sense in getSize like methods */
  public int width() {
    return x;
  }

  /** return's y. just to make sense in getSize like methods */
  public int height() {
    return y;
  }

  @Override
  public String toString() {
    return x + "," + y;
  }

  /** Returns true if the coordinates of this Coord and the given one are the same */
  @Override
  public boolean equals(Object other) // guich@240_2
  {
    if (other instanceof Coord) {
      Coord c = (Coord) other;
      return c.x == this.x && c.y == this.y;
    }
    return false;
  }

  /** Returns the hashcode: x<<16 | y */
  @Override
  public int hashCode() {
    return (x << 16) | y;
  }
}