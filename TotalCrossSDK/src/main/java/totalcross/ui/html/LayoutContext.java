// Copyright (C) 2003-2004 Pierre G. Richard
// Copyright (C) 2004-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.html;

import totalcross.ui.Container;
import totalcross.ui.Control;
import totalcross.util.IntVector;

/**
 * <code>LayoutContext</code> holds the coordinates that layout computations
 * rely upon.
 */

final class LayoutContext {
  public int nextX, nextY;
  public Style lastStyle;
  public int x, maxWidth;
  public int incY;
  public Control lastControl;
  int gap;
  private IntVector idents = new IntVector(2);
  private boolean wasDisjoin;
  /**
   * Parent container with a graphical representation which this context is relative to. Controls should have their
   * bounds set relative to this container whenever a layout context is available. <br>
   * <br>
   * Created to avoid using the controls' parent, which is often a container with no graphical representation.
   */
  Container parentContainer;

  public LayoutContext(int maxWidth, Container parentContainer) {
    if (parentContainer == null) {
      throw new NullPointerException("Argument 'parentContainer' cannot be null");
    }
    this.maxWidth = maxWidth;
    this.parentContainer = parentContainer;
    disjoin();
  }

  public void setIdentation(int gap, boolean set) {
    if (set) {
      nextX -= this.gap;
      idents.push(this.gap);
      this.gap = gap;
      nextX += gap;
    } else {
      try {
        nextX -= this.gap;
        this.gap = idents.pop();
      } catch (Exception e) {
        gap = 0;
      }
    }
  }

  public boolean atStart() {
    return x == gap;
  }

  /** Verify if there's enough space to add this control and do a disjoin otherwise. */
  void verify(int width) {
    if (x + width >= maxWidth || incY > 0) {
      disjoin();
    }
  }

  /** Prepare the layout to add a new control right after the last one. */
  void update(int width) {
    x += width;
    if (x >= maxWidth) {
      disjoin();
      x = gap;
    } else {
      wasDisjoin = false;
      //System.err.println("update");
      nextX = Control.AFTER;
      nextY = Control.SAME;
      incY = 0;
    }
  }

  /** Prepare the layout context for entering into a new clear area. */
  void disjoin() {
    if (wasDisjoin) {
      return;
    }
    wasDisjoin = true;
    //System.err.println("disjoin "+incY);
    x = gap;
    nextX = Control.LEFT + gap;
    nextY = Control.AFTER + incY;
    incY = 0;
  }
}
