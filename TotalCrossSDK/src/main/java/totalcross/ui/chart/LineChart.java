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

import totalcross.ui.Insets;
import totalcross.ui.gfx.Coord;
import totalcross.ui.gfx.Graphics;
import totalcross.util.Vector;

/** This class represents a line chart. */

public class LineChart extends PointLineChart {
  /**
   * Creates a new line chart
   * @param categories The categories that will be used.
   */
  public LineChart(String[] categories) {
    setXAxis(categories);
    border = new Insets(5, 5, 5, 5);
    showLines = true;
  }
  
  public LineChart(String[] categories, Boolean showTextBox) {
    setXAxis(categories);
	border = new Insets(5, 5, 5, 5);
	showLines = true;
	this.showTextBox = showTextBox;
  }

  @Override
  public void onPaint(Graphics g) {
    if (!draw(g)) {
      return;
    }

    // Update points
    int sCount = series.size();
    int off = (getXValuePos(1.0) - getXValuePos(0.0)) / 2;
    int psize = points.size();
    for (int i = 0; i < sCount; i++) // for each series
    {
      if (i >= psize) {
        points.addElement(new Vector());
      }

      Vector v = (Vector) points.items[i];
      Series s = (Series) series.items[i];
      if (v.size() != sCount) {
        v.removeAllElements();
      }
      double[] yValues = s.yValues;

      for (int j = 0; j < xAxisSteps; j++) // for each category
      {
        if (j >= v.size()) {
          v.addElement(new Coord());
        }
        Coord c = (Coord) v.items[j];

        c.x = getXValuePos(j) + off;
        c.y = getYValuePos(yValues[j]);
      }
    }

    super.onPaint(g);
  }
}
