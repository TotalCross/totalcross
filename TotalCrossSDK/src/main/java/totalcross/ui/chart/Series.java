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

import totalcross.ui.Control;
import totalcross.ui.image.Image;

/** The series of data that will be shown in the charts. */

public class Series {
  /** This series' name */
  public String name;

  /** This series' values for the X axis */
  public double[] xValues;

  /** This series' values for the Y axis */
  public double[] yValues;

  /** The color to be used when drawing this series on a chart */
  public int color;

  /** An image that will be used in some chars, to be used instead of a circle.
   * @see #dotVAlign 
   */
  public Image dot;
  Image legendDot;

  /** The alignment of the dot image. Use CENTER (default), TOP or BOTTOM. */
  public int dotVAlign = Control.CENTER;

  /**
   * Creates a new category series
   * @param name the series' name
   * @param yValues the series' values for the Y axis (one for each category)
   * @param color the color to be used when drawing the series on a chart
   */
  public Series(String name, double[] yValues, int color) {
    this(name, null, yValues, color);
  }

  /**
   * Creates a new series
   * @param name the series' name
   * @param xValues the series' values for the X axis
   * @param yValues the series' values for the Y axis
   * @param color the color to be used when drawing the series on a chart
   */
  public Series(String name, double[] xValues, double[] yValues, int color) {
    this.name = name;
    this.xValues = xValues;
    this.yValues = yValues;
    this.color = color;
  }

  /**
   * Creates a new series
   * @param name the series' name
   * @param xValues the series' values for the X axis
   * @param yValues the series' values for the Y axis
   * @param dot The image to be used instead of a circle.
   */
  public Series(String name, double[] xValues, double[] yValues, Image dot) {
    this.name = name;
    this.xValues = xValues;
    this.yValues = yValues;
    this.dot = dot;
    ;
  }
}
