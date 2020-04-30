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
import totalcross.ui.Control;
import totalcross.ui.Window;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.Event;
import totalcross.ui.event.KeyEvent;
import totalcross.ui.event.PenEvent;
import totalcross.ui.gfx.Color;
import totalcross.ui.gfx.Coord;
import totalcross.ui.gfx.Graphics;
import totalcross.ui.gfx.Rect;
import totalcross.util.Vector;

/** Abstract class used by points and line charts.
 * @see LineChart
 * @see XYChart 
 */
public abstract class PointLineChart extends Chart {
  /** Contains all points that are currently painted on this chart */
  protected Vector points = new Vector();

  /** Flag to indicate whether the lines connecting points must be painted */
  public boolean showLines;

  /** Flag to indicate whether the points must be painted */
  public boolean showPoints;

  /** Flag to indicate whether is to show the selected value */
  public boolean showValue = true;

  /** The current selected series */
  private int selectedSeries = -1;

  /** The current selected value from <code>selectedSeries</code> */
  private int selectedValue = -1;

  /** The radious of each point (in pixels) */
  public int pointR = 3;

  /** The line thickness. */
  public int lineThickness = 1;

  /** Flag to indicate whether this chart is focused */
  private boolean hasFocus;

  /** The axis that was selected. */
  public int selectedAxis = -1;

  /** Flag indicating if its to post an event when the user selects an axis. */
  public boolean postEventOnAxisSelection;

  @Override
  public void onPaint(Graphics g) {
    // Draw lines
    if (showLines) {
      int thick = lineThickness;
      for (int i = series.size() - 1; i >= 0; i--) // for each series
      {
        Series s = (Series) series.items[i];
        g.foreColor = s.color;
        Vector v = (Vector) points.items[i]; // the series' points
        if (v != null) {
          for (int j = 0, n = v.size() - 1; j < n; j++) {
            if (s.yValues[j] != UNSET && s.yValues[j + 1] != UNSET) {
              Coord c1 = (Coord) v.items[j];
              Coord c2 = (Coord) v.items[j + 1];
              g.drawThickLine(c1.x, c1.y, c2.x, c2.y, thick);
            }
          }
        }
      }
    }

    // Draw points
    if (!showLines || showPoints || hasFocus || Settings.fingerTouch) {
      for (int i = series.size() - 1; i >= 0; i--) // for each series
      {
        Series s = (Series) series.items[i]; // the series
        Vector v = (Vector) points.items[i]; // the series' points
        if (v != null) {
          for (int j = v.size() - 1; j >= 0; j--) // for each series point
          {
            Coord c1 = (Coord) v.items[j];

            int c = s.color;
            if (showValue && selectedSeries == i && selectedValue == j) {
              c = Color.darker(c);
            }

            if (s.dot == null) {
              g.backColor = c;
              g.fillCircle(c1.x, c1.y, pointR);
            } else {
              int dy;
              int h = s.dot.getHeight();
              switch (s.dotVAlign) {
              case Control.TOP:
                dy = c1.y - h;
                break;
              case Control.BOTTOM:
                dy = c1.y;
                break;
              default:
                dy = c1.y - h / 2;
              }
              g.drawImage(s.dot, c1.x - s.dot.getWidth() / 2, dy);
            }
          }
        }
      }
      
      int sCount = ((Series) series.items[0]).yValues.length;
      int numCols = sCount * series.size();

      // Draw selection (text box)
      if (selectedSeries != -1 && showValue) {
        Series s = (Series) series.items[selectedSeries];

        String text = Convert.toCurrencyString(s.yValues[selectedValue], yDecimalPlaces);
        if (s.xValues != null) {
          text = "(" + Convert.toCurrencyString(s.xValues[selectedValue], xDecimalPlaces) + "," + text + ")";
        }

        Coord c = (Coord) ((Vector) points.items[selectedSeries]).items[selectedValue];
        drawTextBox(g, c.x, c.y, text);
      } else if (showTextBox) {
    	int j = -1;
      	int z = -1
      				;
      	for(int i = 0; i < numCols; i++) { 
  		  if (i % sCount == 0){
 		    j++;
 			z=0;
  		  }
  		  
  		  Series s = (Series) series.items[j];
  		  String text = Convert.toCurrencyString(s.yValues[z], yDecimalPlaces);
	  	  if (s.xValues != null) {
	  	    text = "(" + Convert.toCurrencyString(s.xValues[z], xDecimalPlaces) + "," + text + ")";
	  	  }
	
	  	  Coord c = (Coord) ((Vector) points.items[j]).items[z];
	  	  drawTextBox(g, c.x, c.y, text);
	  	  z++;
  	    }
      }
    }
  }

  @Override
  public void onEvent(Event e) {
    switch (e.type) {
    case PenEvent.PEN_UP: {
      PenEvent pe = (PenEvent) e;
      if (!hadParentScrolled() && xAxisX1 <= pe.x && pe.x <= xAxisX2) {
        int d = columnW / 4;
        for (int i = 0, xx = xAxisX1; xx <= xAxisX2; xx += columnW, i++) {
          if ((xx - d) <= pe.x && pe.x <= (xx + d)) {
            selectedAxis = i;
            postPressedEvent();
            break;
          }
        }
      }
      break;
    }
    case PenEvent.PEN_DOWN: {
      PenEvent pe = (PenEvent) e;
      int xx = pe.x;
      int yy = pe.y;

      if (xx < (xAxisX1 - pointR) || xx > (xAxisX2 + pointR) || yy > (yAxisY1 + pointR) || yy < (yAxisY2 - pointR)) {
        hasFocus = false;
        selectedSeries = -1;
        selectedValue = -1;
        Window.needsPaint = true;
      } else {
        hasFocus = true;
        selectedSeries = -1; // clear selection
        selectedValue = -1;

        Rect r = new Rect();
        int dim = fmH;
        r.width = r.height = fmH * 2;

        for (int i = series.size() - 1; i >= 0; i--) // for each series
        {
          Vector v = (Vector) points.items[i]; // the series' points
          if (v != null) {
            for (int j = v.size() - 1; j >= 0; j--) // for each series point
            {
              Coord c = (Coord) v.items[j];
              r.x = c.x - dim;
              r.y = c.y - dim;

              if (r.contains(xx, yy)) {
                selectedSeries = i;
                selectedValue = j;

                i = 0; // force outter for to exit
                break;
              }
            }
          }
        }

        Window.needsPaint = true;
      }
      break;
    }
    case KeyEvent.SPECIAL_KEY_PRESS: {
      KeyEvent ke = (KeyEvent) e;
      if (ke.isActionKey()) // release focus
      {
        isHighlighting = true;
        parent.requestFocus();
      } else if (ke.isNextKey()) // next point
      {
        if (selectedValue < ((Series) series.items[selectedSeries]).yValues.length - 1) {
          selectedValue++;
          Window.needsPaint = true;
        } else if (selectedSeries < series.size() - 1) {
          selectedSeries++;
          selectedValue = 0;
          Window.needsPaint = true;
        }
      } else if (ke.isPrevKey()) // previous point
      {
        if (selectedValue > 0) {
          selectedValue--;
          Window.needsPaint = true;
        } else if (selectedSeries > 0) {
          selectedSeries--;
          selectedValue = ((Series) series.items[selectedSeries]).yValues.length - 1;
          Window.needsPaint = true;
        }
      }
      break;
    }
    case ControlEvent.FOCUS_IN: {
      hasFocus = true;
      if (series.size() > 0 && ((Series) series.items[0]).yValues.length > 0) {
        selectedSeries = 0;
        selectedValue = 0;
        Window.needsPaint = true;
      }
      break;
    }
    case ControlEvent.FOCUS_OUT: {
      hasFocus = false;
      selectedSeries = -1;
      selectedValue = -1;
      Window.needsPaint = true;
      break;
    }
    }
  }
}
