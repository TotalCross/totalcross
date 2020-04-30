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

package totalcross.ui.chart;

import totalcross.ui.Container;
import totalcross.ui.gfx.Graphics;
import totalcross.ui.image.Image;

/** This class represents a velocimeter gauge. The background and pointer can be customized.
 * The text, max and min values can be drawn or not. The pointer's color can be changed.
 */
public class Velocimeter extends Container {
  /** The current value */
  public int value;
  /** The maximum value; defaults to 100 */
  public int max = 100;
  /** The minimum value; defaults to 0 */
  public int min;
  /** The pointer's color */
  public int pointerColor = 0xAAAAAA;
  /** The value's color */
  public int valueColor = 0xFFFFFF;
  /** Set to false to don't draw the min value's text */
  public boolean drawMin = true;
  /** Set to false to don't draw the max value's text */
  public boolean drawMax = true;
  /** Set to false to don't draw the value's text */
  public boolean drawValue = true;
  /** The maximum angle value; defaults to 270 for the default gauge */
  public int maxAngle = 270;

  private int lastValue;
  private Image gauge, pointer, lastPointer;
  private String gaugeStr, pointerStr;

  /** Constructs a velocimeter using the default gauge and pointer images */
  public Velocimeter() {
    this("totalcross/res/gauge.png", "totalcross/res/pointer.png");
  }

  /** Constructs a velocimeter using the given images. Note that the pointer
   * should be all-white so it can be correctly colorized, and it must also be 
   * pointing to the gauge's position 0. Both images must be squared.
   */
  public Velocimeter(String gaugeImagePath, String pointerImagePath) {
    this.gaugeStr = gaugeImagePath;
    this.pointerStr = pointerImagePath;
  }

  @Override
  public void initUI() {
    try {
      int s = height < width ? height : width;
      gauge = new Image(gaugeStr).smoothScaledFixedAspectRatio(s, true);
      pointer = new Image(pointerStr).smoothScaledFixedAspectRatio(s, true);
      pointer.applyColor2(pointerColor);
    } catch (Exception ee) {
      throw new RuntimeException(ee);
    }
  }

  @Override
  public void onPaint(Graphics g) {
    super.onPaint(g);
    try {
      // draw background
      g.drawImage(gauge, 0, 0);
      // draw pointer
      int v = value > max ? max : value < min ? min : value;
      if (lastValue != v || lastPointer == null) {
        lastValue = v;
        int angle = maxAngle * (v - min) / (max - min);
        lastPointer = pointer.getRotatedScaledInstance(100, -angle, backColor);
      }
      int p = (gauge.getWidth() - pointer.getWidth()) / 2;
      int h = gauge.getHeight();
      g.drawImage(lastPointer, p, p);
      // draw texts
      g.foreColor = foreColor;
      if (drawMin) {
        g.drawText(String.valueOf(min), 0, h - fmH);
      }
      if (drawMax) {
        String s = String.valueOf(max);
        g.drawText(s, width - fm.stringWidth(s), h - fmH);
      }
      if (drawValue) {
        String s = String.valueOf(value > max ? max : value < min ? min : value);
        g.foreColor = valueColor;
        g.drawText(s, (width - fm.stringWidth(s)) / 2, h - fmH * 2);
      }
    } catch (Exception ee) {
      ee.printStackTrace();
    }
  }
}
