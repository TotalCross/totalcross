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

import totalcross.ui.gfx.Color;
import totalcross.ui.gfx.Graphics;
import totalcross.ui.image.Image;

/** Implements a ListBox where the items are images.
 * Next an example of how to use this class as a combobox color chooser:
 * <pre>
 * ImageList list = new ImageList();
 * list.add( ADD AT LEAST ONE IMAGE SO THE CORRECT SIZE CAN BE COMPUTED );
 * add(foreCombo = new ComboBox(list), CENTER, BOTTOM);
 * </pre>
 * If you don't add at least one image before calling add/setRect, you must compute
 * the preferred size yourself.
 */

public class ImageList extends ListBox // guich@tc126_2
{
  public ImageList() {
  }

  public ImageList(Object[] items) {
    super(items);
  }

  @Override
  protected void drawItem(Graphics g, int index, int dx, int dy) {
    Image img = (Image) items.items[index];
    g.drawImage(img, dx, dy);
  }

  @Override
  protected void drawSelectedItem(Graphics g, int index, int dx, int dy, int w) {
    drawItem(g, index, dx, dy); // we will draw using the selected color
  }

  @Override
  protected void drawCursor(Graphics g, int sel, boolean on) {
    if (offset <= sel && sel < visibleItems + offset && sel < itemCount) {
      int dx = 3; // guich@580_41: cursor must be drawn at 3 or will overwrite the border on a combobox with PalmOS style
      int dy = 3;
      if (uiFlat) {
        dy--;
      }
      if (simpleBorder) {
        dx--;
        dy--;
      }

      int ih = getItemHeight(sel);
      int iw = getItemWidth(sel);
      dx += xOffset; // guich@552_24: added this to make scroll apply to the item
      dy += (sel - offset) * ih;
      if (on) {
        g.foreColor = customCursorColor != -1 ? customCursorColor : Color.RED;
        g.drawRect(dx, dy, iw, ih); // only select the Object - guich@200b4_130
      } else {
        drawItem(g, sel, dx, dy);
      }
    }
  }

  @Override
  public int getPreferredWidth() {
    int max = itemCount > 0 ? ((Image) items.items[0]).getWidth() : 0;
    return max + (simpleBorder ? 4 : 6) + (sbar.visible ? sbar.getPreferredWidth() : 0) + insets.left + insets.right;
  }

  @Override
  public int getPreferredHeight() {
    if (itemCount == 0) {
      return fmH;
    }
    int ih = getItemHeight(0);
    return itemCount * ih + insets.top + insets.bottom + (simpleBorder ? 4 : 6);
  }

  @Override
  protected int getItemWidth(int index) {
    return itemCount == 0 ? fmH : ((Image) items.items[index]).getWidth();
  }

  int ih, lastIC = -1;

  @Override
  protected int getItemHeight(int index) {
    if (itemCount == 0) {
      return 0;
    }
    if (itemCount != lastIC) {
      ih = ((Image) items.items[0]).getHeight();
      if (itemCount > 1 && ih != ((Image) items.items[1]).getHeight()) {
        for (int i = itemCount; --i > 0;) {
          ih = Math.max(((Image) items.items[i]).getHeight(), ih);
        }
      }
      lastIC = itemCount;
    }
    return ih;
  }
}