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
import totalcross.sys.SpecialKeys;
import totalcross.ui.Control;
import totalcross.ui.Insets;
import totalcross.ui.ToolTip;
import totalcross.ui.Window;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.Event;
import totalcross.ui.event.KeyEvent;
import totalcross.ui.event.PenEvent;
import totalcross.ui.gfx.Color;
import totalcross.ui.gfx.Graphics;
import totalcross.ui.gfx.Rect;

/** A simple pie chart.
 * <br><br>
 * The values do not have to be in percentage; the percentage is computed based on the series values.
 * If the user clicks on the slice, a popup shows the corresponding value.
 * By setting the legendValueSuffix to "%", the value displayed will be the percentage instead of the
 * serie's value.
 */

public class ArcChart extends Chart {
  /** Specifies the selected pie. */
  public int selectedSeries = -1;
  /** The suffix used in the legend to display the values. E.G.: "%". Defaults to blank.
   */
  public String legendValueSuffix = "";
  /** Set to true to show the values in the legend. */
  public boolean showValuesOnLegend;

  /** The percentage of the filled circle. */
  public int fillPerc = 80;

  /** A gap between the chart and the borders */
  public int borderGap;

  private ToolTip tip;
  private int lastPenX, lastPenY;
  private Rect rect = new Rect();
  private double sum;
  private int currentSelection = -1;
  private int xx, yy, rr;

  /**
   * Creates a new Pie chart.
   * The yDecimalPlaces defines the number of decimal places used to display the value in the legend.
   */
  public ArcChart() {
    drawAxis = false;
    //distanceOfSelectedPie = fmH*2;
    setXAxis(0, 100, 1);
    setYAxis(0, 100, 1);
    tip = new ToolTip(this, "");
    tip.setBackColor(Color.WHITE);
    tip.millisDelay = 50;
    tip.borderColor = 0;
    borderGap = fmH;
  }

  @Override
  protected void getCustomInsets(Insets r) {
    r.top += borderGap;
    r.bottom += borderGap;
    r.top += borderGap;
    r.left += borderGap;
    r.right += borderGap;
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
    int rr = Math.min(clientRect.width, clientRect.height) / 2;
    if (rr > 0) {
      this.rr = rr;
      drawPie(g, xx, yy, rr, false);
    }
  }

  int distanceOfSelectedPie;

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

      if (last == current) {
        ;
      } else {
        int bc = parent.getBackColor();
        g.foreColor = is3d || sum == v ? color : bc; // fixed color when only 1 serie has value > 0
        g.backColor = color;
        g.fillPie(xx, yy, rr, last, current);
        g.backColor = bc;
        g.fillCircle(xx, yy, rr * fillPerc / 100);
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
                this.selectedSeries = i; // make the slice user tapped on be the 'selected' one
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
