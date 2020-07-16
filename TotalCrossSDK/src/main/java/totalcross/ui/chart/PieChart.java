// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.chart;

import totalcross.sys.Convert;
import totalcross.sys.SpecialKeys;
import totalcross.ui.Control;
import totalcross.ui.Insets;
import totalcross.ui.ToolTip;
import totalcross.ui.Window;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.Event;
import totalcross.ui.event.KeyEvent;
import totalcross.ui.event.PenEvent;
import totalcross.ui.font.Font;
import totalcross.ui.gfx.Color;
import totalcross.ui.gfx.Coord;
import totalcross.ui.gfx.Graphics;
import totalcross.ui.gfx.Rect;

/** A simple pie chart.
 * <br><br>
 * The values do not have to be in percentage; the percentage is computed based on the series values.
 * If the user clicks on the slice, a popup shows the corresponding value.
 * By setting the legendValueSuffix to "%", the value displayed will be the percentage instead of the
 * serie's value.
 */

public class PieChart extends Chart {
  /** Specifies the distance that the selected pie will be placed from the rest of the pie. Defaults to fmH. */
  public int distanceOfSelectedPie = Font.NORMAL_SIZE;
  /** Specifies the selected pie. */
  public int selectedSeries = -1;
  /** The suffix used in the legend to display the values. E.G.: "%". Defaults to blank.
   */
  public String legendValueSuffix = "";
  /** Set to true to show the values in the legend. */
  public boolean showValuesOnLegend;

  /** Perspective horizontal distance. */
  public int perspectiveH = Font.NORMAL_SIZE / 2;

  /** Perspective vertical distance. */
  public int perspectiveV = Font.NORMAL_SIZE / 2;

  /** GAO: keeps track of the currently selected slice */
  public int selectedSlice = -1;

  /** GAO: if true, then offset selected pie slice, for visual indicator that its been selected */
  public boolean offsetSelectedSlice = true;

  private ToolTip tip;
  private int lastPenX, lastPenY;
  private static Coord c = new Coord();
  private Rect rect = new Rect();
  private double sum;
  private int currentSelection = -1;
  private int xx, yy, rr;

  /**
   * Creates a new Pie chart.
   * The yDecimalPlaces defines the number of decimal places used to display the value in the legend.
   */
  public PieChart() {
    drawAxis = false;
    setXAxis(0, 100, 1);
    setYAxis(0, 100, 1);
    tip = new ToolTip(this, "");
    tip.setBackColor(Color.WHITE);
    tip.millisDelay = 50;
    tip.borderColor = 0;
  }

  @Override
  protected void getCustomInsets(Insets r) {
    if ((type & IS_3D) != 0) {
      if (perspectiveV > 0) {
        r.bottom = perspectiveV;
      } else {
        r.top = -perspectiveV;
      }
      if (perspectiveH < 0) {
        r.left = -perspectiveH;
      } else {
        r.right = perspectiveH;
      }
    }
    if (currentSelection != -1) {
      r.bottom += distanceOfSelectedPie;
    }
  }

  @Override
  public void onPaint(Graphics g) {
    // compute sum and the values
    sum = 0;
    int sCount = series.size();
    if (showValuesOnLegend && (legendValues == null || legendValues.length != sCount)) {
      legendValues = new String[sCount];
    }
    for (int i = 0; i < sCount; i++) // for each series
    {
      double v = ((Series) series.items[i]).yValues[0];
      sum += v;
      if (showValuesOnLegend) {
        legendValues[i] = " " + Convert.toCurrencyString(v, yDecimalPlaces) + legendValueSuffix;
      }
    }

    if (!draw(g)) {
      return;
    }

    // Update points
    int xx = clientRect.x + clientRect.width / 2;
    int yy = clientRect.y + clientRect.height / 2;
    int rr = Math.min(clientRect.width, clientRect.height) / 2 - distanceOfSelectedPie;
    if (rr > 0) {
      this.rr = rr;
      if ((type & IS_3D) != 0) {
        drawPie(g, xx + perspectiveH, yy + perspectiveV, rr, true);
      }
      drawPie(g, xx, yy, rr, false);
    }
  }

  private void drawPie(Graphics g, int xx, int yy, int rr, boolean is3d) {
    if (sum == 0) {
      return;
    }

    g.foreColor = 0;
    int sCount = series.size();
    double last = 0, current;
    this.xx = xx;
    this.yy = yy;
    int space = 5;

    for (int i = 0; i < sCount; i++) // for each series
    {
      // juliana@268: it is necessary to save the old positions to correctly offset the selected pie.
      xx = this.xx;
      yy = this.yy;

      Series s = (Series) series.items[i];
      int color = i == currentSelection ? Color.darker(s.color, 32) : is3d ? Color.darker(s.color) : s.color;
      //if (is3d) color = Color.interpolate(backColor,color);

      double v = s.yValues[0];
      current = last + (v * 360 / sum);

      if (i == selectedSeries) {
        int half = (int) (last + (v / 2 * 360 / sum));
        g.getAnglePoint(xx, yy, distanceOfSelectedPie, distanceOfSelectedPie, half, c);
        xx = c.x;
        yy = c.y;
      }
      if (last == current) {
        ;
      } else if ((type & GRADIENT_VERTICAL) != 0) {
        int fade = (type & GRADIENT_DARK) != 0 ? Color.darker(color, 128) : Color.brighter(color, 128);
        g.backColor = is3d ? color : ((type & GRADIENT_INVERT) != 0) ? color : fade;
        g.foreColor = is3d ? g.backColor : ((type & GRADIENT_INVERT) != 0) ? fade : color;
        g.fillPieGradient(xx, yy, rr, last, current);
        if (!is3d) {
          g.foreColor = 0;
          g.drawPie(xx, yy, rr, last, current);
        }
      } else {
        g.foreColor = is3d || sum == v ? color : 0; // fixed color when only 1 serie has value > 0
        g.backColor = color;
        g.fillPie(xx, yy, rr, last, current);
        Control.safeUpdateScreen();
      }
      last = current;
         	
      if (showTextBox) {
	    String text = Convert.toCurrencyString(legendValueSuffix.indexOf('%') >= 0 ? (s.yValues[0] / sum * 100) : s.yValues[0],
	              yDecimalPlaces) + legendValueSuffix;
	    drawTextBox(g, rect.x + space, rect.y + 25, "  ", color);
	    space += 10;
	    drawTextBox(g, rect.x + space, rect.y + 25, text);
	    space += 50;
      }
    }
    
  }

  @Override
  public void onEvent(Event e) {
    switch (e.type) {
    case PenEvent.PEN_DOWN:
    case PenEvent.PEN_DRAG: {
      PenEvent pe = (PenEvent) e;
      lastPenX = pe.x;
      lastPenY = pe.y;
      break;
    }
    case ControlEvent.PRESSED:
      if (e.target == tip) {
        if (currentSelection != -1) {
          setTipText((Series) series.items[currentSelection]);
        } else {
          // get the angle
          int deltax = lastPenX - xx;
          int deltay = lastPenY - yy;
          int distance = (int) Math.sqrt(deltax * deltax + deltay * deltay);
          double tan;
          try {
            tan = (double) deltay / (double) deltax;
          } catch (ArithmeticException e1) {
            tan = 0;
          } // guich@tc123_4: prevent divide by 0 to close the program
          double degree = Math.atan(tan) * 180 / Math.PI;
          if (degree < 0) {
            degree = -degree;
          }
          if (deltax >= 0) {
            if (deltay > 0) {
              degree = 360 - degree;
            }
          } else if (deltay < 0) {
            degree = 180 - degree;
          } else {
            degree += 180;
          }
          // find the slice that contains this angle
          if (sum == 0) {
            break;
          }
          int sCount = series.size(), i;
          double last = 0, current;
          for (i = 0; i < sCount; i++) // for each series
          {
            Series s = (Series) series.items[i];
            double v = s.yValues[0];
            current = last + (v * 360 / sum);
            if (last <= degree && degree <= current) {
              int r = i == selectedSeries ? (rr + distanceOfSelectedPie) : rr;
              if (r < distance) {
                i = sCount; // don't show anything
              } else {
                setTipText(s);

                if (offsetSelectedSlice) {
                  this.selectedSeries = i; // make the slice user tapped on be the 'selected' one
                }
                selectedSlice = i; // field so after receiving a Pie event, can retrieve selected slice
              }
              break;
            }
            last = current;
          }
          if (i == sCount) {
            tip.setText("");
          }
        }
      }
      break;
    case ControlEvent.FOCUS_IN:
      lastPenX = width / 8 - 10;
      lastPenY = 0 - 10;
      currentSelection = -1; // don't change!
      Window.needsPaint = true;
      break;
    case ControlEvent.FOCUS_OUT:
      currentSelection = -1;
      Window.needsPaint = true;
      break;
    case KeyEvent.SPECIAL_KEY_PRESS: {
      KeyEvent ke = (KeyEvent) e;

      if (ke.key == SpecialKeys.ACTION || ke.key == SpecialKeys.ENTER) {
        parent.setHighlighting();
        tip.penUp(null);
      }
      break;

    }
    }
  }

  private void setTipText(Series s) {
	if (!showTextBox) {
      tip.setText(
          Convert.toCurrencyString(legendValueSuffix.indexOf('%') >= 0 ? (s.yValues[0] / sum * 100) : s.yValues[0],
              yDecimalPlaces) + legendValueSuffix);
      Rect r = getAbsoluteRect();
      rect.set(r.x + lastPenX + 10, r.y + lastPenY + 10, 0, 0);
      tip.setControlRect(rect);
	}
  }

  @Override
  public void onFontChanged() {
    tip.setFont(this.font);
  }
}
