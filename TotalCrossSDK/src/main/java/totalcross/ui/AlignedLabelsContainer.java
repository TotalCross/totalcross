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

import totalcross.ui.font.Font;
import totalcross.ui.gfx.Graphics;

/** 
 * <code>AlignedLabelsContainer</code> is a <code>Container</code> used to align all controls to the maximum width of a set of labels. You can 
 * define the label alignment. The controls that you add to this container are placed at the right of the labels. Here's a sample of how to use it:
 *
 * <pre>
 * String[] labels =
 * {
 *    "Name",
 *    "Born date",
 *    "Telephone",
 *    "Address",
 *    "City",
 *    "Country",
 *    "",
 * };
 * AlignedLabelsContainer c = new AlignedLabelsContainer(labels);
 * c.setBorderStyle(BORDER_LOWERED);
 * c.labelAlign = RIGHT;
 * c.foreColors = new int[]{Color.RED,Color.BLACK,Color.BLACK,Color.BLACK,Color.BLACK,Color.BLACK,Color.BLACK,};
 * c.setInsets(2,2,2,2);
 * c.setFont(font.asBold()); // labels are bold
 * c.childrenFont = font;    // but controls are normal
 * add(c,LEFT+2,TOP+2,FILL-2,PREFERRED+4);
 * for (int i =0; i < labels.length-2; i++)
 *    c.add(new Edit(),LEFT+2,AFTER+(i==0?2:0));
 * c.add(new ComboBox(new String[]{"Brazil","France"}),LEFT+2,AFTER);
 * c.add(new Button("Insert data"),RIGHT,SAME);
 * c.add(new Button("Clear data"),RIGHT,AFTER,SAME,PREFERRED);
 * </pre>
 * @since TotalCross 1.01
 */
public class AlignedLabelsContainer extends Container // guich@tc110_86
{
  /** 
   * The label captions. 
   */
  protected String[] labels;

  /** 
   * The computed widths for the labels. 
   */
  protected int[] widths;

  /** 
   * Sets this member to the font you want to set to the controls that are added to this container.
   * 
   * @since TotalCross 1.25
   */
  public Font childrenFont;

  /** 
   * Sets an array with the same number of labels and the colors you want to show for each label. If the number of labels differs, you will get an 
   * exception. It can be used to hide a label: just set its foreground color to the background color.
   * 
   * @since TotalCross 1.2
   */
  public int[] foreColors; // guich@tc120_53

  private int maxw, vgap, ileft;

  /** 
   * The alignment of the labels. Defaults to <code>LEFT</code>.
   * 
   * @see #LEFT
   * @see #CENTER
   * @see #RIGHT
   */
  public int labelAlign = LEFT;

  /** 
   * Creates a new <code>AlignedLabelsContainer</code> without labels. You may call <code>setLabels()</code> to set the labels.
   * 
   * @see #AlignedLabelsContainer(String[])
   * @see #AlignedLabelsContainer(String[], int)
   * @see #setLabels(String[], int)
   */
  public AlignedLabelsContainer() {
    setLabels(null, 0);
  }

  /** 
   * Creates a new <code>AlignedLabelsContainer</code> with the given labels.
   * 
   * @param labels The strings that represents the labels. You may pass <code>null</code>, but be sure to call <code>setLabels()</code> to set the 
   * labels.
   *
   * @see #AlignedLabelsContainer(String[], int)
   * @see #setLabels(String[], int)
   */
  public AlignedLabelsContainer(String[] labels) {
    setLabels(labels, 0);
  }

  /** 
   * Creates a new <code>AlignedLabelsContainer</code> with the given labels.
   * 
   * @param labels The strings that represents the labels. You may pass <code>null</code>, but be sure to call <code>setLabels()</code> to set the 
   * labels.
   * @param vgap The extra gap between rows. May be negative.
   * 
   * @see #AlignedLabelsContainer(String[])
   * @see #setLabels(String[], int)
   */
  public AlignedLabelsContainer(String[] labels, int vgap) {
    setLabels(labels, vgap);
  }

  /** 
   * Sets the labels and the extra gap between rows (which may be 0).
   * 
   * @param labels The strings that represents the labels. You may pass <code>null</code>, but be sure to call <code>setLabels()</code> again to 
   * set the labels with a valid value.
   * @param vgap The extra gap between rows. May be negative.
   * 
   * @since TotalCross 1.2
   * @see #AlignedLabelsContainer(String[])
   * @see #AlignedLabelsContainer(String[], int)
   */
  public void setLabels(String[] labels, int vgap) {
    if (labels == null) {
      labels = new String[0];
    }
    this.labels = labels;
    widths = new int[labels.length];
    onFontChanged();
    this.vgap = vgap;
  }

  /**
   * Called after a <code>setFont()</code>
   */
  @Override
  public void onFontChanged() {
    maxw = 0;
    for (int i = 0; i < labels.length; i++) {
      widths[i] = fm.stringWidth(labels[i]);
      if (widths[i] > maxw) {
        maxw = widths[i];
      }
    }
    super.setInsets(maxw + ileft, insets.right, insets.top, insets.bottom); // guich@tc114_3: set the insets again
  }

  /**
   * Since this is an <code>AlignedLabelsContainer</code>, use this to add a label. 
   */
  @Override
  public void add(Control c) {
    super.add(c);
    if (childrenFont != null) {
      c.setFont(childrenFont);
    }
  }

  /**
   * Sets the insets value to match the given ones.
   * 
   * @param left The left border value.
   * @param right The right border value.
   * @param top The top border value.
   * @param bottom The bottom border value.
   */
  @Override
  public void setInsets(int left, int right, int top, int bottom) {
    ileft = left;
    super.setInsets(maxw + left, right, top, bottom);
  }

  /**
   * Returns the preferred width of this control. 
   * 
   * @return The preferred width of this control.
   */
  @Override
  public int getPreferredWidth() {
    return maxw + insets.left + insets.right;
  }

  /**
   * Returns the preferred height of this control. 
   * 
   * @return The preferred height of this control.
   */
  @Override
  public int getPreferredHeight() {
    return getLineY(labels.length) + insets.bottom;
  }

  /**
   * Draws the border (if any). 
   * 
   * @param g The graphics object for drawing.
   */
  @Override
  public void onPaint(Graphics g) {
    super.onPaint(g);
    int left = insets.left - maxw;
    int yy = getLineY(0);
    int inc = getLineHeight() + getGap(vgap);
    yy += Edit.prefH / 2; // guich@tc114_5
    for (int i = 0; i < labels.length; i++, yy += inc) {
      g.foreColor = foreColor;
      if (foreColors != null) {
        g.foreColor = foreColors[i];
      }
      switch (labelAlign) {
      case LEFT:
        g.drawText(labels[i], left, yy, textShadowColor != -1, textShadowColor);
        break;
      case CENTER:
        g.drawText(labels[i], left + (maxw - widths[i]) / 2, yy, textShadowColor != -1, textShadowColor);
        break;
      case RIGHT:
        g.drawText(labels[i], left + maxw - widths[i], yy, textShadowColor != -1, textShadowColor);
        break;
      }
    }
  }

  /** 
   * Given a line (staring from 0), returns the y position. Can be used to easily align the controls. Note that the line number may be greater than 
   * the number of assigned Labels.
   * 
   * <pre>
   * add(chCasado = new Check("Married"),LEFT,getLineY(4)+2);
   * </pre>
   * 
   * @since TotalCross 1.14
   */
  public int getLineY(int line) // guich@tc114_4
  {
    int y0 = insets.top + Edit.prefH + getGap(vgap) - 1;
    int inc = getLineHeight() + getGap(vgap);
    return y0 + inc * line;
  }

  /** 
   * Returns the height of a line.
   * 
   * @return The height of a line.
   * 
   * @since TotalCross 1.14 
   */
  public int getLineHeight() // guich@tc114_4
  {
    return fmH + Edit.prefH;
  }

  /** 
   * Returns the left inset. To be able to center a control, you have to do like this:
   * 
   * <pre>
   * control.setRect(CENTER-getLeftInset()/2,...);
   * </pre>
   * 
   * @return The left inset.
   */
  public int getLeftInset() {
    return maxw;
  }
}
