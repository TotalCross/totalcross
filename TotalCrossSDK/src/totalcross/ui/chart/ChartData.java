package totalcross.ui.chart;

import totalcross.ui.*;
import totalcross.ui.gfx.*;

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

public class ChartData extends Container
{
   public int decimalPlaces;
   String[][] data;
   String[] title;
   Chart chart;
   public int lineColor = Color.DARK;
   
   public boolean snapToTop;
   public boolean snapToBottom;

   /** Constructs a ChartData without title. 
    * @param data The values to be displayed in the format [rows][cols] 
    */
   public ChartData(Chart chart, String[][] data)
   {
      this(chart, null, data);
   }
   
   /** Constructs a ChartData with the given title and data.
    * @param data The values to be displayed in the format [rows][cols] 
    */
   public ChartData(Chart chart, String[] title, String[][] data)
   {
      this.chart = chart;
      this.title = title;
      this.data = data;
   }
   
   public void onPaint(Graphics g)
   {
      g.backColor = backColor;
      g.fillRect(0,0,width,height);
      double inc = (chart.xAxisMaxValue - chart.xAxisMinValue) / chart.xAxisSteps;
      double val = chart.xAxisMinValue;
      if (chart.getXValuePos(val) == 0) return;
      int v0 = chart.getXValuePos(val);
      int cw = chart.getXValuePos(val+inc) - v0;

      g.foreColor = foreColor;
      int yy = 0;
      for (int i = 0; i < data.length; i++)
      {
         double x0 = val;
         if (title != null)
         {
            g.setClip(0,yy,chart.getXValuePos(x0)-2,fmH);
            g.drawText(title[i],0,yy);
         }         
         for (int j = 0, n = data[i].length; j < n; j++, x0 += inc)
         {
            int xx = chart.getXValuePos(x0);
            g.setClip(xx,yy,cw,fmH-1);
            String d = data[i][j];
            int sw = fm.stringWidth(d);
            g.drawText(d,xx+(cw-sw)/2,yy);
         }
         yy += fmH;
      }
      if (lineColor != -1)
      {
         g.clearClip();
         g.backColor = lineColor;
         g.foreColor = backColor;
         int xf = chart.getXValuePos(val + inc * data[0].length),xx; 
         double x0 = val;
         for (int j = 0, n = data[0].length; j <= n; j++, x0 += inc) // vertical lines
            g.drawDots(xx = chart.getXValuePos(x0),0,xx,height);
         yy = 0;
         for (int i = snapToBottom ? 1 : 0, n = data.length; i <= n; i++,yy += fmH) // horizontal lines
            g.drawDots(v0,yy,xf,yy);
         if (!snapToTop)
         {
            yy = data.length * fmH; g.drawDots(v0,yy-1,xf,yy-1);
         }
      }
   }
   
   public void reposition()
   {
      removeAll();
      initUI();
   }
   
   public int getPreferredHeight()
   {
      return fmH * data.length;
   }
}
