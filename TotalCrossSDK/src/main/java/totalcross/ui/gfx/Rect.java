// Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>   
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

package totalcross.ui.gfx;

import totalcross.sys.Convert;

/**
 * Rect represents a rectangle.
 */

public class Rect {
  /** x position */
  public int x;
  /** y position */
  public int y;
  /** rectangle width */
  public int width;
  /** rectangle height */
  public int height;

  /** Constructs a rectangle with x = y = width = height = 0. */
  public Rect() {
    // they are already 0 as default.
  }

  /** Constructs a rectangle with the given x, y, width and height. */
  public Rect(int x, int y, int width, int height) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
  }

  /** Constructs a rectangle with the given rectangle coordinates. */
  public Rect(Rect r) {
    this.x = r.x;
    this.y = r.y;
    this.width = r.width;
    this.height = r.height;
  }

  /** Constructs a rectangle with the given coords */
  public Rect(Coord topleft, Coord bottomright) {
    set(topleft.x, topleft.y, bottomright.x - topleft.x + 1, bottomright.y - topleft.y + 1);
  }

  /** Sets the properties of this rect. */
  public void set(int x, int y, int width, int height) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
  }

  /** Copies the properties of this rect from the given rect. */
  public void set(Rect r) // guich@210_4
  {
    this.x = r.x;
    this.y = r.y;
    this.width = r.width;
    this.height = r.height;
  }

  /** Returns true if the point xx,yy is inside this rect. */
  public boolean contains(int xx, int yy) {
    return x <= xx && xx < x + width && y <= yy && yy < y + height;
  }

  @Override
  public String toString() {
    return Convert.toString(x) + ',' + y + ',' + width + ',' + height;
  }

  /** Translates this rect. The new positions will be this.x+deltaX, this.y+deltaY. */
  public void translate(int deltaX, int deltaY) {
    this.x += deltaX;
    this.y += deltaY;
  }

  /** Returns a new rect modified by the specified parameters. */
  public Rect modifiedBy(int deltaX, int deltaY, int deltaW, int deltaH) {
    return new Rect(x + deltaX, y + deltaY, width + deltaW, height + deltaH);
  }

  /** Modifies this rect by the specified parameters. */
  public void modify(int deltaX, int deltaY, int deltaW, int deltaH) {
    x += deltaX;
    y += deltaY;
    width += deltaW;
    height += deltaH;
  }

  /** Returns x+width-1 */
  public int x2() {
    return x + width - 1;
  }

  /** Returns y+height-1 */
  public int y2() {
    return y + height - 1;
  }

  /** Returns true if this rectangle intersects with the given one */
  public boolean intersects(Rect r) {
    return !((r.x + r.width <= x) || (r.y + r.height <= y) || (r.x >= x + width) || (r.y >= y + height));
  }

  /** Modify this Rect by doing the intersection with the given rect. Returns <code>this</code> rect. */
  public Rect intersectWith(Rect r) {
    int x1 = Math.max(x, r.x);
    int x2 = Math.min(x + width, r.x + r.width);
    int y1 = Math.max(y, r.y);
    int y2 = Math.min(y + height, r.y + r.height);
    this.x = x1;
    this.y = y1;
    this.width = x2 - x1;
    this.height = y2 - y1;
    return this;
  }

  /** Modify this Rect by doing an union with the given rect. Returns <code>this</code> rect. */
  public Rect unionWith(Rect r) {
    int x1 = Math.min(x, r.x);
    int x2 = Math.max(x + width, r.x + r.width);
    int y1 = Math.min(y, r.y);
    int y2 = Math.max(y + height, r.y + r.height);
    this.x = x1;
    this.y = y1;
    this.width = x2 - x1;
    this.height = y2 - y1;
    return this;
  }

  /** Returns true if the bounds of this Rect and the given one are the same */
  @Override
  public boolean equals(Object other) // guich@240_2
  {
    if (other instanceof Rect) {
      Rect r = (Rect) other;
      return r.x == this.x && r.y == this.y && r.width == this.width && r.height == this.height;
    }
    return false;
  }

  /** Returns the hashcode for this rect, ie, an integer valued 0xXXYYWWHH */
  @Override
  public int hashCode() // guich@566_4
  {
    return (x << 24) | (y << 16) | (width << 8) | height;
  }
}