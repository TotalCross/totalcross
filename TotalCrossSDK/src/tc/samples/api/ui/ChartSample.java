/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2014 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  This file is covered by the GNU LESSER GENERAL PUBLIC LICENSE VERSION 3.0    *
 *  A copy of this license is located in file license.txt at the root of this    *
 *  SDK or can be downloaded here:                                               *
 *  http://www.gnu.org/licenses/lgpl-3.0.txt                                     *
 *                                                                               *
 *********************************************************************************/

package tc.samples.api.ui;

import tc.samples.api.*;

import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.chart.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;

public class ChartSample extends BaseContainer
{
   ColumnChart column;
   LineChart line;
   PieChart pie;
   Slider sh,sv;

   Check is3D,showTitle,showCategories,showHGrids,
     showVGrids, isVGrad, isHGrad, isInvGrad, isDarker, showYValues;
   ComboBox legendPosition;
   TabbedContainer tp;

   public void initUI()
   {
      super.initUI();
      ScrollContainer sc = new ScrollContainer(false, true);
      sc.setInsets(gap,gap,gap,gap);
      add(sc,LEFT,TOP,FILL,FILL);
      int color1 = Chart.COLOR2;
      int color2 = Chart.COLOR3;
      int color3 = Chart.COLOR4;
      // setup the column chart
      String[] names = {"Jan", "Feb", "Mar", "Apr"};
      column = new ColumnChart(names);
      column.series.addElement(new Series("Rice", new double[] {1000, 1020, 1040, 1060}, color1));
      column.series.addElement(new Series("Beans", new double[] {850, 755, 859, 964},    color2));
      column.series.addElement(new Series("Oil", new double[] {930, 837, 943, 1000},     color3));

      column.setTitle("Sales Projection");
      column.setYAxis(0, 1100, 11);
      column.type = Chart.IS_3D;

      is3D = new Check("3D");
      is3D.setChecked(true);
      showTitle = new Check("Title");
      legendPosition = new ComboBox(new String[]{"Legend","Right","Left","Top","Bottom"});
      legendPosition.setSelectedIndex(0);
      showCategories = new Check("Category");
      showHGrids = new Check("HGrids");
      showVGrids = new Check("VGrids");
      showYValues = new Check("YValues");
      isHGrad = new Check("Horiz");
      isVGrad = new Check("Vert");
      isInvGrad = new Check("Invert");
      isDarker = new Check("Dark");
      
      int gap =Settings.screenWidth > 320 ? fmH/2 : 0;
      sc.add(showTitle, LEFT, AFTER+ 2+gap);
      sc.add(legendPosition, AFTER + 2, SAME,PREFERRED,SAME);
      sc.add(showCategories, AFTER + 2, SAME);
      sc.add(showYValues, AFTER+2, SAME);
      sc.add(showHGrids, LEFT, AFTER + 2 + gap);
      sc.add(showVGrids, AFTER + 2, SAME);
      sc.add(is3D, AFTER + 2, SAME);

      int r = width - is3D.getRect().x2()-6;
      sc.add(sh = new Slider(),AFTER+2,SAME,r/2,fmH);
      sc.add(sv = new Slider(),AFTER+2,SAME,r/2,fmH);
      sh.setMinimum(-6); sh.setMaximum(6); sv.setMaximum(6);
      sh.drawTicks = sv.drawTicks = true;
      sh.drawFilledArea = sv.drawFilledArea = false;
      sv.setValue(column.perspectiveV);
      sh.setValue(column.perspectiveH);
      sh.setLiveScrolling(true);
      sv.setLiveScrolling(true);

      sc.add(new Label("Shade"), LEFT, AFTER+gap, PREFERRED, SAME, is3D);
      sc.add(isHGrad, AFTER+2, SAME);
      sc.add(isVGrad, AFTER+2, SAME);
      sc.add(isInvGrad, AFTER+2, SAME);
      sc.add(isDarker, AFTER+2, SAME);
      isInvGrad.setVisible(false);
      isDarker.setVisible(false);

      int bg = Color.darker(backColor,16);
      tp = new TabbedContainer(new String[]{" Column "," Line "," Pie "});
      tp.extraTabHeight = fmH/2;
      sc.add(tp, LEFT,AFTER+gap*2,FILL,PARENTSIZE+90);

      tp.getContainer(0).add(column, LEFT,TOP,FILL,FILL);
      column.setBackColor(bg);

      // setup the line chart
      line = new LineChart(names);
      line.series.addElement(new Series("Rice", new double[] {100, 102, 104, 106}, color1));
      line.series.addElement(new Series("Beans", new double[] {150, 155, 159, 164}, color2));
      line.series.addElement(new Series("Oil", new double[] {130, 137, 143, 150}, color3));
      line.lineThickness = 2;
      line.setTitle("Sales Projection");
      line.setYAxis(0, 200, 10);
      tp.getContainer(1).add(line, LEFT,TOP,FILL,FILL);
      line.setBackColor(bg);

      // setup the pie chart
      pie = new PieChart();
      pie.series.addElement(new Series("Rice", new double[] {100}, color1));
      pie.series.addElement(new Series("Beans", new double[] {200}, color2));
      pie.series.addElement(new Series("Oil", new double[] {80}, color3));
      pie.selectedSeries = 2;
      pie.yDecimalPlaces = 1; // 1 decimal place
      pie.setTitle("Profit Share");
      pie.legendValueSuffix = "%"; // show % instead of the value in the tooltip
      tp.getContainer(2).add(pie, LEFT,TOP,FILL,FILL);
      pie.setBackColor(bg);
      pie.type = Chart.IS_3D;
      column.xDecimalPlaces = column.yDecimalPlaces = line.yDecimalPlaces = 0;
      line.legendPerspective = pie.legendPerspective = column.legendPerspective = 6;
      tp.activeTabBackColor = Color.ORANGE;
      tp.pressedColor = Color.YELLOW;
   }

   public void onEvent(Event e)
   {
      if (e.type == ControlEvent.PRESSED)
      {
         if (e.target == tp)
         {
            int sel = tp.getActiveTab();
            boolean c = sel == 0;
            boolean l = sel == 1;
            boolean p = sel == 2;
            is3D.setVisible(!l);
            showHGrids.setVisible(!p);
            showVGrids.setVisible(!p);
            showCategories.setVisible(!p);
            showYValues.setVisible(!p);
            isHGrad.setVisible(c); if (!c) isHGrad.setChecked(false);
            isVGrad.setVisible(c || p); if (!c && !p) isHGrad.setChecked(false);
            isInvGrad.setVisible(c || p);
            isDarker.setVisible(c || p);
            isInvGrad.setVisible(isHGrad.isChecked() || isVGrad.isChecked());
            isDarker.setVisible(isInvGrad.isVisible());
            sv.setVisible(is3D.isChecked() && !l);
            sh.setVisible(is3D.isChecked() && !l);
            if (c)
            {
               sv.setMinimum(0);
               sh.setValue(column.perspectiveH);
               sv.setValue(column.perspectiveV);
            }
            else
            if (p)
            {
               sv.setMinimum(-6);
               sh.setValue(pie.perspectiveH);
               sv.setValue(pie.perspectiveV);
            }
         }
         else
         if (e.target instanceof Check || e.target instanceof ComboBox)
         {
            if (e.target == isHGrad && isHGrad.isChecked() && isVGrad.isChecked())
               isVGrad.setChecked(false);
            else
            if (e.target == isVGrad && isHGrad.isChecked() && isVGrad.isChecked())
               isHGrad.setChecked(false);
            isInvGrad.setVisible(isHGrad.isChecked() || isVGrad.isChecked());
            isDarker.setVisible(isHGrad.isChecked() || isVGrad.isChecked());
            pie.showTitle = line.showTitle = column.showTitle = showTitle.isChecked();
            pie.showLegend = line.showLegend = column.showLegend = legendPosition.getSelectedIndex() != 0;
            pie.legendPosition = line.legendPosition = column.legendPosition = getLegendPosition();
            pie.showCategories = line.showCategories = column.showCategories = showCategories.isChecked();
            line.showHGrids = column.showHGrids = showHGrids.isChecked();
            line.showVGrids = column.showVGrids = showVGrids.isChecked();
            line.showYValues = column.showYValues = showYValues.isChecked();
            column.type = pie.type =
               (is3D.isChecked() ? Chart.IS_3D : 0) |
               (isHGrad.isChecked() ? Chart.GRADIENT_HORIZONTAL : 0) |
               (isVGrad.isChecked() ? Chart.GRADIENT_VERTICAL : 0) |
               (isInvGrad.isChecked() ? Chart.GRADIENT_INVERT : 0) |
               (isDarker.isChecked() ? Chart.GRADIENT_DARK : 0);
            sv.setVisible(is3D.isChecked() && !line.isVisible());
            sh.setVisible(is3D.isChecked() && !line.isVisible());
            repaint();
         }
         else
         if (e.target == sv)
         {
            column.perspectiveV = Math.max(sv.getValue(),0);
            pie.perspectiveV = sv.getValue();
            repaint();
         }
         else
         if (e.target == sh)
         {
            line.legendPerspective = pie.legendPerspective = column.legendPerspective = pie.perspectiveH = column.perspectiveH = sh.getValue();
            repaint();
         }
      }
   }

   private int getLegendPosition()
   {
      switch (legendPosition.getSelectedIndex())
      {
         case 2: return LEFT;
         case 3: return TOP;
         case 4: return BOTTOM;
         default: return RIGHT;
      }
   }
}
