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

/** Implements a ListBox where colors can be choosen from.
 * The only functional methods are setColors, getSelectedItem and getSelectedColor.
 * Next an example of how to use this class as a combobox color chooser:
 * <pre>
 * add(foreCombo = new ComboBox(new ColorList()), CENTER, BOTTOM);
 * </pre>
 * Consider using ColorChooserBox instead.
 * @see totalcross.ui#ColorChooserBox
 */

public class ColorList extends ListBox {
  private int w;

  /** Item added to the ListBox, containing the color as String and as int. */
  public static class Item {
    public int value;
    private String s;

    public Item(int value) {
      set(value);
    }

    public void set(int value) {
      this.value = value;
      s = Color.toString(value);
    }

    @Override
    public boolean equals(Object other) {
      if (other instanceof Item) {
        return ((Item) other).value == value;
      }
      if (other instanceof String) {
        return Color.getRGB((String) other) == value;
      }
      return false;
    }

    @Override
    public String toString() {
      return s;
    }
  }

  private static Item blackItem = new Item(0);

  private static Item[] convert(int[] c) {
    Item[] ci = new Item[c.length];
    for (int i = c.length - 1; i >= 0; i--) {
      ci[i] = new Item(c[i]);
    }
    return ci;
  }

  private static Item[] defaultColors;
  static {
    defaultColors = convert(Graphics.getPalette());
  }
  private Item[] colors;

  public ColorList() {
    super(defaultColors);
    colors = defaultColors;
    w = fm.stringWidth("CCCCCC") + 8;
  }

  /** Sets the colors that will be displayed. */
  public void setColors(int[] newColors) // guich@582_4
  {
    colors = convert(newColors);
    removeAll();
    add(colors);
  }

  @Override
  protected void drawItem(Graphics g, int index, int dx, int dy) {
    if (0 <= index && index <= colors.length) {
      int hh = getItemHeight(index);
      if (uiAndroid) {
        hh--;
      }
      int xx = btnX == 0 ? width : btnX;
      g.backColor = colors[index].value;
      if (uiVista) {
        g.fillVistaRect(dx - 1, dy, xx, hh, g.backColor, false, false);
      } else {
        g.fillRect(dx - 1, dy, xx, hh);
      }
      g.foreColor = Color.getBrightness(colors[index].value) >= 127 ? Color.BLACK : Color.WHITE;
      g.drawText(colors[index].toString(), dx + 4, dy + (hh - fmH) / 2, textShadowColor != -1, textShadowColor);
    }
  }

  @Override
  protected void drawSelectedItem(Graphics g, int index, int dx, int dy, int w) {
    drawItem(g, index, dx, dy); // we will draw using the selected color
  }

  @Override
  protected void drawCursor(Graphics g, int sel, boolean on) {
    if (offset <= sel && sel < visibleItems + offset && sel < itemCount) {
      int dx = 3;
      int dy = 4;
      if (uiFlat) {
        dy--;
      }
      if (simpleBorder) {
        dx--;
        dy--;
      }
      int ih = getItemHeight(sel);
      dy += (sel - offset) * ih;
      int yy = dy - 2 - (ih - fmH) / 2;
      g.setClip(dx - 1, yy, btnX - dx + 1, Math.min(ih * visibleItems, this.height - dy)); // guich@200b4_83: fixed selection overflowing paint area
      int c = colors[sel].value;
      int a = Color.getBrightness(c);
      g.foreColor = a >= 127 ? Color.BLACK : Color.WHITE;
      g.drawRect(dx - 1, yy, btnX - 1, ih); // only select the Object - guich@200b4_130
    }
  }

  @Override
  public int getPreferredWidth() {
    return w + (simpleBorder ? 4 : 6) + sbar.getPreferredWidth() + insets.left + insets.right;
  }

  @Override
  protected int getItemWidth(int index) {
    return w;
  }

  /** Returns the selected Item object, or a black Item representing the black color if none was selected.
   * From the Item you can retrieve the color as int. Use the getSelectedColor method to retrieve
   * the color directly. */
  @Override
  public Object getSelectedItem() {
    return selectedIndex != -1 ? colors[selectedIndex] : blackItem;
  }

  /** Returns the selected color or black if no one is selected. */
  public int getSelectedColor() {
    return selectedIndex != -1 ? colors[selectedIndex].value : 0;
  }
}