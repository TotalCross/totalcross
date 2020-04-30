// Copyright (C) 2019-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.ui.icon;

import totalcross.ui.Control;
import totalcross.ui.font.Font;
import totalcross.ui.gfx.Color;
import totalcross.ui.gfx.Graphics;

public class Icon extends Control {

  private IconType glyph;
  private int premultipliedForeColor;
  private int alpha;

  public Icon(IconType glyph) {
    this.glyph = glyph;
    this.font = Font.getFont(glyph.fontName(), false, 24);
    this.setAlpha(255);
  }
  
  public void setGlyph(IconType glyph) {
    this.glyph = glyph;
  }

  @Override
  public int getPreferredWidth() {
    return this.font.fm.charWidth((char) glyph.codepoint()) + 3;
  }

  @Override
  public int getPreferredHeight() {
    return this.font.fm.height;
  }
  
  /** Gets the alpha applied to the icon color of this object */
  public int getAlpha() {
     return alpha;
  }
  /** Set the alpha value to be used with the icon of this object (0 to 255) */
  public void setAlpha(int alpha) {
     if (alpha != this.alpha) {
        this.alpha = alpha;
        onColorsChanged(true);
     }
  }

  @Override
  protected void onColorsChanged(boolean colorsChanged) {
    premultipliedForeColor = Color.interpolateA(foreColor, backColor, alpha); // sholtzer@450_21: added support for setEnabled(false)
  }

  @Override
  public void onPaint(Graphics g) {
    super.onPaint(g);

    g.backColor = getBackColor();
    g.foreColor = premultipliedForeColor;

    if (!transparentBackground) {
      g.fillRect(0, 0, width, height);
    }

    g.drawText(glyph.toString(), 3, 0);
  }
}
