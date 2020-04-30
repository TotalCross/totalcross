// Copyright (C) 2001-2012 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui;

import totalcross.ui.gfx.Graphics;

/**
 * PagePosition implements the empty and filled balls that indicates the current 
 * page in a set of pages, very common on Android and iPhone.
 * 
 * It has three properties:
 * <ol>
 * <li> visible count: the number of visible balls
 * <li> count: the number of balls that will be displayed. Can be less, equal or greater than visible count
 * <li> position: the current position of the filled ball
 * </ol>
 * 
 * @since TotalCross 1.3
 */

public class PagePosition extends Control {
  protected int visibleCount, count, position = 1;

  /** Constructs a new PagePosition with the given number of visible balls.
   * This number cannot be changed later.
   */
  public PagePosition(int visibleCount) {
    this.count = this.visibleCount = visibleCount;
  }

  /** Sets the balls count. Invalid values are forced to a valid range. */
  public void setCount(int c) {
    if (c < 1) {
      c = 1;
    }
    count = c;
    Window.needsPaint = true;
  }

  /** Sets the current position (starting at 1). Invalid values are forced to a valid range. */
  public void setPosition(int p) {
    if (p < 1) {
      p = 1;
    } else if (p > count) {
      p = count;
    }
    position = p;
    Window.needsPaint = true;
  }

  /** Returns the visibleCount passed in the constructor. */
  public int getVisibleCount() {
    return visibleCount;
  }

  /** Returns the ball count. */
  public int getCount() {
    return count;
  }

  /** Returns the current position */
  public int getPosition() {
    return position;
  }

  /** Increments the current position. If wrap is true, when position gets above count, it wraps to 1. */
  public void inc(boolean wrap) {
    if (wrap && ++position > count) {
      position = 1;
    } else if (!wrap && position < count) {
      position++;
    }
    Window.needsPaint = true;
  }

  /** Decrements the current position. If wrap is true, when position gets below 1, it wraps to count. */
  public void dec(boolean wrap) {
    if (wrap && --position < 1) {
      position = count;
    } else if (!wrap && position > 1) {
      position--;
    }
    Window.needsPaint = true;
  }

  @Override
  public void onPaint(Graphics g) {
    if (!transparentBackground) {
      g.backColor = backColor;
      g.fillRect(0, 0, width, height);
    }

    int pos = position - 1;
    int k = Math.min(height, width / (visibleCount + 1));
    int r = k / 3;

    int n = count < visibleCount ? count : visibleCount;
    int mid = n / 2;

    int pageCount = count / visibleCount;
    boolean exactFraction = (count % visibleCount) == 0;
    if (exactFraction) {
      pageCount--;
    }
    int curPage = pos / visibleCount;
    boolean leftArrow = count > visibleCount && (n == 1 ? position > 1 : pos >= visibleCount);
    boolean rightArrow = count > visibleCount && (n == 1 ? position < count : curPage < pageCount);

    int x = 0;
    int y = (k - r * 2) / 2;
    int x2 = width - r - 1;
    // draw the arrows
    if (leftArrow) {
      g.drawArrow(x, y, r + 1, Graphics.ARROW_LEFT, false, foreColor);
    }
    if (rightArrow) {
      g.drawArrow(x2, y, r + 1, Graphics.ARROW_RIGHT, false, foreColor);
    }
    x += k - r;
    int x0 = x;
    if (count < visibleCount) {
      x0 = x = (width - n * k) / 2 + r;
    }
    g.backColor = g.foreColor = foreColor;
    // draw the empty circles
    for (int i = rightArrow ? n : exactFraction ? visibleCount : count % visibleCount; --i >= 0; x += k) {
      g.drawCircle(x + r, y + r, r);
    }
    // draw the current position
    int p = n == 1 ? mid : leftArrow && rightArrow ? pos % visibleCount : rightArrow ? pos : pos % visibleCount;
    x = x0 + (n == 1 ? 0 : p * k);
    g.fillCircle(x + r, y + r, r);
  }

  @Override
  public int getPreferredWidth() {
    return getPreferredHeight() * (visibleCount + 1) + 1;
  }

  @Override
  public int getPreferredHeight() {
    return fmH / 2;
  }
}
