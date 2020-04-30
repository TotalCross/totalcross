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
import totalcross.ui.Window;
import totalcross.ui.event.Event;
import totalcross.ui.event.PenEvent;
import totalcross.ui.gfx.Color;
import totalcross.ui.gfx.Graphics;
import totalcross.util.Properties;
import totalcross.util.Vector;

/**
    The ChartData class represents a table with data that would be displayed in the chart.
    Here's a sample:
    <pre>
      double[] xAxis = new double[0];
      double[] pressureyAxis = new double[0]; 
      Series pressureSeries = new Series("Pression", xAxis, pressureyAxis, Color.BLUE);
      int cols = 24, rows = 5;
      XYChart chart = new XYChart();
      chart.showHGrids = chart.showVGrids = true; // Shows grids.
      chart.showTitle = true; // Shows title.
      chart.showYValues = true; // Shows the Y axis values.
      chart.yDecimalPlaces = 0; // No decimal places.
      chart.setXAxis(0, 240, 24); // The X axis is time in minutes. It has an interval of 5 minutes and a maximum of 2 hours.
      chart.setYAxis(0, 240, 24); // The Y axis is different for each graph.

      chart.snapToTop = chart.snapToBottom = true;
      String[][] data = new String[rows][cols];
      String[] tit = new String[rows];
      for (int r = 0; r < rows; r++)
      {
         tit[r] = "row "+(r+1);
         for (int c = 0; c < cols; c++)
            data[r][c] = "999";
      }

      ChartData cd1 = new ChartData(chart, tit, data);
      cd1.lineColor = Color.BLACK;
      cd1.setFont(font.adjustedBy(-2));
      cd1.snapToTop = true;

      ChartData cd2 = new ChartData(chart, data);
      cd2.lineColor = Color.BLACK;
      cd2.setFont(font.adjustedBy(-2));
      cd2.snapToBottom = true;

      add(cd1,LEFT,TOP+25,FILL,PREFERRED);
      add(cd2,LEFT,BOTTOM-25,FILL,PREFERRED);
      add(chart,LEFT,AFTER,FILL,FIT,cd1);
      cd2.bringToFront();
      cd1.bringToFront();
      chart.showLines = false;
      chart.yValuesSize = fm.stringWidth("99999");
      chart.series.addElement(pressureSeries);
      // setup the xy chart
      pressureSeries.xValues = new double[]{10,20,30,40,50};
      pressureSeries.yValues = new double[]{10,20,30,40,35};
      try
      {
         pressureSeries.dot = Resources.radioBkg.getNormalInstance(fmH,fmH,-1);
      }
      catch (ImageException e)
      {
         MessageBox.showException(e, true);
      }
      cd2.reposition();
    </pre>

    @since TotalCross 2.0
 */

public class ChartData extends Container {
  Vector rows = new Vector(10);
  protected Chart chart;
  public int lineColor = Color.DARK;
  public int fillColor2 = 0xDDDDDD;
  public int titleForeColor = -1, titleBackColor = -1;
  public int selectedCol = -1, selectedRow = -1;

  public boolean snapToTop;
  public boolean snapToBottom;

  public int use2ndColorEveryXColumns = 1;

  /**
   * The height of the cell when using PREFERRED, defined as a % of the control's font height. Default value is 100(%).
   */
  public int preferredCellHeight = 100;

  /**
   * Constructs an empty ChartData.
   */
  public ChartData(Chart chart) {
    this.chart = chart;
  }

  /**
   * Constructs a ChartData with the given data.
   * 
   * @param data
   *           The values to be displayed in the format [rows][cols]
   */
  public ChartData(Chart chart, ChartDataRow[] data) {
    this(chart);
    this.rows.addElements(data);
  }

  @Override
  public void onPaint(Graphics g) {
    g.backColor = backColor;
    if (!transparentBackground) {
      g.fillRect(0, 0, width, height);
      if (chart.axisBackColor != -1) {
        g.backColor = chart.axisBackColor;
        g.fillRect(chart.xAxisX1, 0, chart.xAxisX2 - chart.xAxisX1, height);
      }
    }
    double inc = (chart.xAxisMaxValue - chart.xAxisMinValue) / chart.xAxisSteps;
    double val = chart.xAxisMinValue;
    if (chart.getXValuePos(val) == 0) {
      return;
    }
    int v0 = chart.getXValuePos(val), xx;
    int cw = chart.getXValuePos(val + inc) - v0;

    int rowsLength = rows.size();
    if (rowsLength > 0) {
      int ystep = this.height / rowsLength;
      int yxtra = this.height % ystep;

      if (fillColor2 != -1) {
        double x0 = val + inc * use2ndColorEveryXColumns;
        g.backColor = fillColor2;
        // vertical lines            
        for (int j = 1, n = getRow(0).columns.length; j <= n; j += 2, x0 += inc * use2ndColorEveryXColumns * 2) {
          xx = chart.getXValuePos(x0);
          g.fillRect(xx, 0, chart.getXValuePos(x0 + inc * use2ndColorEveryXColumns) - xx, height);
        }
      }

      int xx0 = chart.getXValuePos(val);
      if (titleBackColor != -1) {
        g.backColor = titleBackColor;
        g.fillRect(0, 0, xx0 + 1, height);
      }

      g.backColor = backColor;
      g.foreColor = foreColor;
      int yy = 0;
      for (int i = 0; i < rowsLength; i++) {
        ChartDataRow row = getRow(i);
        double x0 = val;
        int hh = ystep;
        if (i < yxtra) {
          hh++;
        }
        if (row.title != null) {
          g.setClip(0, yy, chart.getXValuePos(x0) - 2, hh);
          g.foreColor = titleForeColor != -1 ? titleForeColor : foreColor;
          g.drawText(row.title, 0, yy + (hh - fmH) / 2);
        }
        g.foreColor = foreColor;
        for (int j = 0, n = row.columns.length; j < n; j++, x0 += inc) {
          xx = chart.getXValuePos(x0);
          g.setClip(xx, yy, cw, hh - 1);
          Properties.Value value = row.columns[j];
          if (value != null) {
            String d = value.toString();
            int sw = fm.stringWidth(d);
            g.drawText(d, xx + (cw - sw) / 2, yy + (hh - fmH) / 2);
          }
        }
        yy += hh;
      }
      if (lineColor != -1) {
        g.clearClip();
        g.foreColor = chart.axisForeColor;
        g.drawLine(xx = chart.getXValuePos(val), 0, xx, height); // draw Y axis

        g.backColor = fillColor2 != -1 ? fillColor2 : lineColor;
        int xf = width - chart.border.right - 1;
        double x0 = val + (fillColor2 != -1 ? inc : 0);
        // vertical lines
        for (int j = fillColor2 != -1 ? 1 : 0, n = getRow(0).columns.length; j <= n; j++, x0 += inc) {
          g.drawDots(xx = chart.getXValuePos(x0), 0, xx, height);
        }
        yy = 0;
        int hh = 0;
        for (int i = snapToBottom ? 1 : 0, n = rowsLength; i <= n; i++, yy += hh) // horizontal lines
        {
          g.drawDots(v0, yy, xf, yy);
          hh = ystep;
          if (i < yxtra) {
            hh++;
          }
        }
        if (!snapToTop) {
          g.drawDots(v0, height - 1, xf, height - 1);
        }
      }
    }
    if (chart.markPos != Chart.UNSET) {
      g.foreColor = chart.categoryMarkColor;
      g.drawLine(chart.markPos - 1, 0, chart.markPos - 1, height);
      g.drawLine(chart.markPos, 0, chart.markPos, height);
      g.drawLine(chart.markPos + 1, 0, chart.markPos + 1, height);
    }
  }

  @Override
  public void onEvent(Event e) {
    switch (e.type) {
    case PenEvent.PEN_UP:
      if (isEnabled() && !hadParentScrolled()) {
        selectedRow = selectedCol = -1;
        PenEvent pe = (PenEvent) e;
        int pex = pe.x, pey = pe.y;
        int rowsLength = rows.size();
        if (rowsLength > 0 && chart.xAxisX1 <= pex && pex <= chart.xAxisX2 && chart.yAxisY2 <= pey
            && pey <= chart.yAxisY1) {
          int ystep = this.height / rowsLength;
          int yxtra = this.height % ystep;
          selectedCol = (pex - chart.xAxisX1) / chart.columnW;
          for (int i = 0, hh, yy = chart.yAxisY2; i <= rowsLength; i++, yy += hh) // horizontal lines
          {
            hh = ystep;
            if (i < yxtra) {
              hh++;
            }
            if (yy <= pey && pey < yy + hh) {
              selectedRow = i;
              break;
            }
          }
          postPressedEvent();
        }
      }
      break;
    }
  }

  @Override
  public void reposition() {
    super.reposition(false);
    removeAll();
    initUI();
  }

  @Override
  public int getPreferredHeight() {
    int rowsLength = rows.size();
    if (rowsLength == 0) {
      return 0;
    }
    return fmH * preferredCellHeight * rowsLength / 100;
  }

  public Properties.Value getSelectedCell() {
    if (selectedCol >= 0 && selectedRow >= 0) {
      return getRow(selectedRow).columns[selectedCol];
    }
    return null;
  }

  public Properties.Value getCell(int col, int row) {
    return getRow(row).columns[col];
  }

  public void setCell(Properties.Value value, int col, int row) {
    getRow(row).columns[col] = value;
    Window.needsPaint = true;
  }

  public ChartDataRow getRow(int row) {
    if (row < 0 || row >= rows.size()) {
      throw new IndexOutOfBoundsException();
    }
    return (ChartDataRow) rows.items[row];
  }

  public ChartDataRow getSelectedRow() {
    if (selectedRow >= 0) {
      return getRow(selectedRow);
    }
    return null;
  }

  /** Adds a new row. Pass -1 to add at the end */
  public void addLine(int pos, ChartDataRow row) {
    int rowCount = rows.size();
    if (pos < 0 || pos > rowCount) {
      pos = rowCount;
    }
    rows.insertElementAt(row, pos);
  }

  public void removeLine(int pos) {
    int rowCount = rows.size();
    if (pos == -1) {
      pos = rowCount - 1;
    }
    if (pos < 0 || pos >= rowCount) {
      return;
    }

    this.rows.removeElementAt(pos);
  }

  public void removeLine(String title) {
    for (int i = rows.size() - 1; i >= 0; i--) {
      if (getRow(i).title.equals(title)) {
        removeLine(i);
      }
    }
  }

  public class ChartDataRow {
    String title;
    Properties.Value[] columns;

    public ChartDataRow(int columnCount) {
      this(null, new Properties.Value[columnCount]);
    }

    public ChartDataRow(String title, int columnCount) {
      this(title, new Properties.Value[columnCount]);
    }

    public ChartDataRow(String title, Properties.Value[] data) {
      this.title = title;
      this.columns = data;
    }

    public String getTitle() {
      return title;
    }
  }
}
