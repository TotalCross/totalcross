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

import totalcross.ui.Control;
import totalcross.ui.gfx.Graphics;

/**
 * <code>Hr</code> is the Tile associated to the &lt;HR&gt; tag.
 */
class Hr extends Control implements Document.CustomLayout {
  /**
   * Constructor
   *
   * @param doc containing document
   * @param atts tag attributes
   * @param style associated style
   */
  Hr() {
    focusTraversable = false;
  }

  @Override
  public void layout(LayoutContext lc) {
    lc.disjoin();
    parent.add(this, lc.nextX, lc.nextY, 10, PREFERRED); // the width will be recomputed later
    lc.disjoin();
  }

  @Override
  public int getPreferredHeight() {
    return fmH * 2;
  }

  @Override
  public void onPaint(Graphics g) {
    if (parent.getWidth() != width) {
      setRect(this.x, this.y, parent.getWidth(), this.height);
      repaintNow();
    }
    g.drawLine(0, fmH, width, fmH);
  }
}
