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

import totalcross.sys.Convert;
import totalcross.sys.Settings;
import totalcross.ui.Window;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.Event;
import totalcross.ui.event.KeyEvent;
import totalcross.ui.event.PenEvent;
import totalcross.ui.gfx.Color;
import totalcross.ui.gfx.Graphics;
import totalcross.ui.gfx.Rect;
import totalcross.util.Vector;

/** A vertical column chart. */

public class ColumnChart extends Chart {
  /** The current selected column */
  private int selection = -1;

  /** Vector of columns rectangles */
  protected Vector columns = new Vector();

  /** Used to draw the column perspective when chart type is 3D */
  private int[] xPoints = new int[4];

  /** Used to draw the column perspective when chart type is 3D */
  private int[] yPoints = new int[4];

  /** Perspective horizontal distance. */
  public int perspectiveH = 5;

  /** Perspective vertical distance. */
  public int perspectiveV = 3;

  /**
   * Creates a new 2D column chart
   * @param categories The categories that will be used
   */
  public ColumnChart(String[] categories) {
    this(0, categories);
  }

  public ColumnChart(String[] categories, Boolean showTextBox) {
	this(0, categories);
	this.showTextBox = showTextBox;
  }
  
  /**
   * Creates a new column chart
   * @param type the chart's type (2D or 3D)
   * @param categories The categories that will be used
   * @see #type
   */
  public ColumnChart(int type, String[] categories) {
    this.type = type;
    setXAxis(categories);
  }

  @Override
  public void onPaint(Graphics g) {
    if (columns.size() != series.size()) {
      columns.removeAllElements();
    }

    if (!draw(g)) {
      return;
    }

    int sCount = series.size();
    int cCount = xAxisSteps;

    int numCols = sCount * cCount;
    for (int i = columns.size(); i < numCols; i++) {
      columns.addElement(new Rect());
    }

    int catW = getXValuePos(1.0) - getXValuePos(0.0);
    int colW = catW / (sCount + 1);
    int colS = (type & IS_3D) != 0 ? (colW - 5) / 2 : colW / 2;

    // Draw series
    boolean forward = perspectiveH >= 0;

    int col = 0;
    for (int i = 0; i < cCount; i++) // for each category
    {
      int x = getXValuePos(i) + colS + (forward ? 0 : colW * (sCount - 1) - perspectiveH);
      for (int j = forward ? 0 : sCount - 1; forward ? (j < sCount)
          : (j >= 0); j += (forward ? 1 : -1), x += (forward ? colW : -colW), col++) // for each series
      {
        Series s = (Series) series.items[j];
        int y = getYValuePos(s.yValues[i]);

        Rect r = (Rect) columns.items[col]; // update column rect
        r.x = x;
        r.y = y;
        r.width = colW + 1;
        r.height = yAxisY1 - y + 1;

        int c = s.color;
        if (selection == col) {
          c = Color.darker(c);
        }

        if ((type & (GRADIENT_HORIZONTAL | GRADIENT_VERTICAL)) != 0) {
          int fade = (type & GRADIENT_DARK) != 0 ? Color.darker(c, 128) : Color.brighter(c, 128);
          boolean invertGradient = (type & GRADIENT_INVERT) != 0;
          boolean vertical = (type & GRADIENT_VERTICAL) != 0;
          g.fillShadedRect(x, y, colW, yAxisY1 - y, !invertGradient, !vertical, c, fade, 100); // note: original method was drawRoundGradient, which draws from c2 to c1, that's why we use !invert here
        } else {
          g.backColor = c;
          g.fillRect(x, y, colW + 1, yAxisY1 - y + 1);
        }
        g.foreColor = Color.BLACK;
        g.drawRect(x, y, colW + 1, yAxisY1 - y + 1);

        if ((type & IS_3D) != 0) // draw perspective
        {
          // Include the perspective into rect
          r.y -= perspectiveV;
          r.height += perspectiveV;
          r.width += perspectiveH;

          // top
          g.backColor = Color.darker(c);
          xPoints[0] = x;
          yPoints[0] = y;
          xPoints[1] = x + perspectiveH;
          yPoints[1] = y - perspectiveV;
          xPoints[2] = x + colW + perspectiveH;
          yPoints[2] = y - perspectiveV;
          xPoints[3] = x + colW;
          yPoints[3] = y;

          g.fillPolygon(xPoints, yPoints, 4);
          g.drawPolygon(xPoints, yPoints, 4);

          // side
          if (perspectiveH >= 0) {
            xPoints[0] = x + colW;
            yPoints[0] = yAxisY1;
            xPoints[1] = x + colW + perspectiveH;
            yPoints[1] = yAxisY1;
          } else {
            xPoints[0] = x;
            yPoints[0] = yAxisY1;
            xPoints[1] = x + perspectiveH;
            yPoints[1] = yAxisY1;
            xPoints[2] = x + perspectiveH;
            yPoints[2] = y - perspectiveV;
            xPoints[3] = x;
            yPoints[3] = y;
          }

          g.fillPolygon(xPoints, yPoints, 4);
          g.drawPolygon(xPoints, yPoints, 4);
        }
      }
    }

    if (selection >= 0) // there is a selection
    {
      Rect r = (Rect) columns.items[selection];
      String text = Convert.toCurrencyString(((Series) series.items[selection % sCount]).yValues[selection / sCount],
          xDecimalPlaces);
      drawTextBox(g, r.x, r.y, text);
    } else if(showTextBox) {
      int j = -1;
      int z = -1;
      for(int i = 0; i < numCols; i++) {
        if (i % sCount == 0){
    	  j++;
          z=0;
        }
        Rect r = (Rect) columns.items[i];
        String text = Convert.toCurrencyString(((Series) series.items[z]).yValues[j], xDecimalPlaces);
        drawTextBox(g, r.x, r.y, text);
        z++;
      }
    }
  }

  @Override
  public void onEvent(Event e) {
    switch (e.type) {
    case PenEvent.PEN_UP:
      if (!hadParentScrolled()) {
        PenEvent pe = (PenEvent) e;
        pe.consumed = true;

        if (pe.x >= xAxisX1 && pe.x <= xAxisX2 && pe.y >= yAxisY2 && pe.y <= yAxisY1) {
          selection = -1;
          int len = columns.size();
          int i;

          for (i = 0; i < len; i++) {
            Rect r = (Rect) columns.items[i];
            if (r.contains(pe.x, pe.y)) {
              break;
            }
          }

          if (i < len) {
            if ((type & IS_3D) != 0 && i < len - 1) // check overlaping with next column if 3D
            {
              Rect r = (Rect) columns.items[i + 1];
              if (r.contains(pe.x, pe.y)) {
                i++;
              }
            }

            selection = i;
          }
          Window.needsPaint = true;
        } else if (selection >= 0) // there is a column selected, invalidate
        {
          selection = -1;
          Window.needsPaint = true;
        }
      }
      break;
    case KeyEvent.SPECIAL_KEY_PRESS:
      if (Settings.keyboardFocusTraversable) {
        KeyEvent ke = (KeyEvent) e;

        if (ke.isActionKey()) {
          isHighlighting = true;
          parent.requestFocus();
        } else {
          int len = columns.size();
          if (len > 0) {
            if (ke.isNextKey()) // next column
            {
              if (selection < len - 1) {
                selection++;
                Window.needsPaint = true;
              }
            } else if (ke.isPrevKey()) // previous column
            {
              if (selection > 0) {
                selection--;
                Window.needsPaint = true;
              }
            }
          }
        }
      }
      break;
    case ControlEvent.FOCUS_IN:
      if (Settings.keyboardFocusTraversable && columns.size() > 0) {
        selection = 0;
        Window.needsPaint = true;
      }
      break;
    case ControlEvent.FOCUS_OUT:
      if (selection >= 0) {
        selection = -1;
        Window.needsPaint = true;
      }
      break;
    }
  }
}
