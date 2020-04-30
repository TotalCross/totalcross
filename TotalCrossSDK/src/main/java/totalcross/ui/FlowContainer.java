// Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>
// Copyright (C) 2000-2012 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui;

import totalcross.sys.Settings;

/** This class is a Container that will place controls one after the other and, once the 
 * width has been reached, it wraps to next line.
 * 
 * Example:
 * <pre>
 * Settings.uiAdjustmentsBasedOnFontHeight = true;
 * 
 * Label l = new Label("Do you agree that TotalCross is a great development platform?");
 * l.autoSplit = true;
 * add(l, LEFT,AFTER,PARENTSIZE+100,PREFERRED);
 * 
 * FlowContainer fc = new FlowContainer(50,25);
 * fc.add(new Radio("Probably Yes"));
 * fc.add(new Radio("Probably No"));
 * fc.add(new Radio("Maybe"));
 * add(fc, LEFT,AFTER,PARENTSIZE+100,PREFERRED);
 * </pre>
 * 
 * All controls must be added before calling setRect.
 * 
 * When calling setRect for this control, the height must be PREFERRED (with adjustments, if needed).
 * 
 * Also, if initUI is overriden, be sure to call <code>super.initUI()</code>.
 * 
 * <b>IT IS VERY IMPORTANT THAT YOU USE PARENTSIZE+100 INSTEAD OF FILL IN THE WIDTH PARAMETER!</b>
 * 
 * @since TotalCross 1.39
 */
public class FlowContainer extends Container {
  protected int lines = 1;
  private int hgap, vgap;
  private int lastASW;

  /** Constructs a FlowContainer with the given horizontal and vertical gaps.
   * You <b>must</b> add all children controls before calling setRect for this container.
   */
  public FlowContainer(int hgap, int vgap) {
    this.hgap = hgap;
    this.vgap = vgap;
  }

  /** Places the controls on screen. */
  @Override
  public void initUI() {
    lines = 1;
    // position first control
    Control c = children;
    if (c == null) {
      return;
    }
    c.setRect(LEFT, TOP, PREFERRED, PREFERRED);
    int g = Settings.uiAdjustmentsBasedOnFontHeight && uiAdjustmentsBasedOnFontHeightIsSupported ? hgap * fmH / 100
        : hgap;
    // position next controls
    while (c != null) {
      int x2 = c.getX2() + g;
      c = c.next;
      if (c == null) {
        break;
      }
      c.resetSetPositions();
      x2 += c.getPreferredWidth();
      if (x2 <= width) {
        c.setRect(AFTER + hgap, SAME, PREFERRED, PREFERRED);
      } else // wrap to new line
      {
        c.setRect(LEFT, AFTER + vgap, PREFERRED, PREFERRED);
        lines++;
      }
    }
  }

  @Override
  protected void onBoundsChanged(boolean screenChanged) {
    if (setW == FILL) {
      throw new RuntimeException("For FlowContainer subclasses, please use PARENTSIZE+100 instead of FILL");
    }
    if (this.width > 0 && this.width != lastASW && ((PREFERRED - RANGE) <= setH && setH <= (PREFERRED + RANGE))) {
      lastASW = this.width;
      initUI();
      setRect(KEEP, KEEP, KEEP, getPreferredHeight() + setH - PREFERRED);
    }
  }

  @Override
  public int getPreferredHeight() {
    int lines = 1;
    // position first control
    Control c = children;
    int g = Settings.uiAdjustmentsBasedOnFontHeight && uiAdjustmentsBasedOnFontHeightIsSupported ? hgap * fmH / 100
        : hgap;
    // position next controls
    int x2 = 0;
    int tw = tempW != -1 ? tempW : width;
    while (c != null) {
      int w = c.getPreferredWidth() + g;
      x2 += w;
      c = c.next;
      if (x2 > tw) // still fits in the same line?
      {
        lines++;
        x2 = w;
      }
      if (c == null) {
        break;
      }
    }
    return lines * (fmH + Edit.prefH) + getGap(vgap) * (lines - 1);
  }
}
