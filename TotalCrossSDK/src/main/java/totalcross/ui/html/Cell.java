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
import totalcross.ui.ScrollContainer;
import totalcross.ui.html.Document.SizeDelimiter;

// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
// !!!!  REMEMBER THAT ANY CHANGE YOU MAKE IN THIS CODE MUST BE SENT BACK TO SUPERWABA COMPANY     !!!!
// !!!!  LEMBRE-SE QUE QUALQUER ALTERACAO QUE SEJA FEITO NESSE CODIGO DEVERÁ SER ENVIADA PARA NOS  !!!!
// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

/**
 * <code>Cell</code> is the cell of a Table.
 * Implementation notes:
 * In order to layout a cell, we first have to get its minimum width, then layout it.
 */

class Cell extends ScrollContainer implements SizeDelimiter {
  Style style;
  int colspan, rowspan;

  Cell(Style style) {
    super(false);
    this.style = style;
    appObj = new ControlProperties(null, null, style);
    if (style.backColor != -1) {
      setBackColor(style.backColor);
    }
    colspan = style.atts.getAttributeValueAsInt("colspan", 0);
    rowspan = style.atts.getAttributeValueAsInt("rowspan", 0);
  }

  private void layoutControls() {
    if (this.width != 0) {
      return;
    }
    this.width = this.height = 4096; // temporary
    int minWidth = Math.max(parent.getWidth(), getMinWidth(this, 0));
    LayoutContext lc2 = new LayoutContext(minWidth, this);
    Control[] children = bag.getChildren();
    if (children.length == 0) {
      resize(1, 1); // make sure size is never 0
      setBackColor(style.backColor);
    } else {
      for (int i = children.length; --i >= 0;) {
        Document.layout(children[i], lc2);
      }
      resize();
    }
  }

  @Override
  public int getPreferredWidth() {
    return super.getPreferredWidth() + insets.left + insets.right;
  }

  @Override
  public int getPreferredHeight() {
    return super.getPreferredHeight() + insets.top + insets.bottom;
  }

  private int getMinWidth(Control control, int curWidth) {
    if (control != this && control instanceof SizeDelimiter) {
      int w = ((SizeDelimiter) control).getMaxWidth(); // this is actually the minimum width
      if (w > curWidth) {
        curWidth = w;
      }
    }

    if (control instanceof Container) {
      Control[] children = ((Container) control).getChildren();
      for (int i = children.length; --i >= 0;) {
        curWidth = getMinWidth(children[i], curWidth);
      }
    }
    return curWidth;
  }

  @Override
  public int getMaxWidth() {
    layoutControls();
    return getPreferredWidth();
  }
}
