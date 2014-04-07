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

package tc.samples.ui;

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
   RadioGroupController rgType;
   Slider sh,sv;

   Check is3D,showTitle,showCategories,showHGrids,
     showVGrids, isVGrad, isHGrad, isInvGrad, isDarker, showYValues;
   ComboBox legendPosition;
   Button bt;

   public void initUI()
   {
      super.initUI();
      setTitle("Chart");
      ScrollContainer sc = new ScrollContainer(false, true);
      sc.setInsets(gap,gap,gap,gap);
      add(sc,LEFT,TOP,FILL,FILL);

      // setup the column chart
      String[] names = {"Jan", "Feb", "Mar", "Apr"};
      column = new ColumnChart(names);
      column.series.addElement(new Series("Rice", new double[] {1000, 1020, 1040, 1060}, Color.YELLOW));
      column.series.addElement(new Series("Beans", new double[] {850, 755, 859, 964}, Color.GREEN));
      column.series.addElement(new Series("Oil", new double[] {930, 837, 943, 1000}, Color.RED));

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
      rgType = new RadioGroupController();
      
      sc.add(new Label("Type: "), LEFT, TOP);
      sc.add(new Radio("Column ",rgType), AFTER, SAME);
      sc.add(new Radio("Line ", rgType), AFTER, SAME);
      sc.add(new Radio("Pie ", rgType), AFTER, SAME);
      sc.add(bt = new Button("100x"),RIGHT,SAME,PREFERRED,SAME);

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

      sc.add(new Label("Gradient"), LEFT, AFTER+2+gap, PREFERRED, SAME, is3D);
      sc.add(isHGrad, AFTER+2, SAME);
      sc.add(isVGrad, AFTER+2, SAME);
      sc.add(isInvGrad, AFTER+2, SAME);
      sc.add(isDarker, AFTER+2, SAME);
      sc.add(new Ruler(),LEFT,AFTER+gap);
      isInvGrad.setEnabled(false);
      isDarker.setEnabled(false);

      sc.add(column, CENTER, AFTER+gap, PARENTSIZE+100,PARENTSIZE+90);
      column.setBackColor(Color.darker(backColor,16));

      // setup the line chart
      line = new LineChart(names);
      line.series.addElement(new Series("Rice", new double[] {100, 102, 104, 106}, Color.YELLOW));
      line.series.addElement(new Series("Beans", new double[] {150, 155, 159, 164}, Color.GREEN));
      line.series.addElement(new Series("Oil", new double[] {130, 137, 143, 150}, Color.RED));
      line.lineThickness = 2;
      line.setTitle("Sales Projection");
      line.setYAxis(0, 200, 10);
      sc.add(line, SAME, SAME, SAME, SAME);
      line.setVisible(false);
      line.setBackColor(Color.darker(backColor,16));

      // setup the pie chart
      pie = new PieChart();
      pie.series.addElement(new Series("Rice", new double[] {100}, Color.YELLOW));
      pie.series.addElement(new Series("Beans", new double[] {150}, Color.GREEN));
      pie.series.addElement(new Series("Oil", new double[] {130}, Color.RED));
      pie.selectedSeries = 2;
      pie.yDecimalPlaces = 1; // 1 decimal place
      pie.setTitle("Profit Share");
      pie.legendValueSuffix = "%"; // show % instead of the value in the tooltip
      sc.add(pie, SAME, SAME, SAME, SAME);
      pie.setBackColor(Color.darker(backColor,16));
      pie.setVisible(false);
      pie.type = Chart.IS_3D;
      column.xDecimalPlaces = column.yDecimalPlaces = line.yDecimalPlaces = 0;
      line.legendPerspective = pie.legendPerspective = column.legendPerspective = 6;
   }

   public void onEvent(Event e)
   {
      if (e.type == ControlEvent.PRESSED)
      {
         if (e.target == bt)
         {
            int ini = Vm.getTimeStamp();
            for (int i = 0; i < 100; i++)
               repaintNow();
            int fim = Vm.getTimeStamp();
            setInfo("Paint 100x elapsed: "+(fim-ini)+"ms");
         }
         else
         if (e.target instanceof Radio)
         {
            char caption = rgType.getSelectedItem().getText().charAt(0);
            boolean c,l,p;
            column.setVisible(c = caption == 'C');
            line  .setVisible(l = caption == 'L');
            pie   .setVisible(p = caption == 'P');
            is3D.setEnabled(!l);
            showHGrids.setEnabled(!p);
            showVGrids.setEnabled(!p);
            showCategories.setEnabled(!p);
            showYValues.setEnabled(!p);
            isHGrad.setEnabled(c);
            isVGrad.setEnabled(c || p);
            isInvGrad.setEnabled(c || p);
            isDarker.setEnabled(c || p);
            isInvGrad.setEnabled(isHGrad.isChecked() || isVGrad.isChecked());
            isDarker.setEnabled(isHGrad.isChecked() || isVGrad.isChecked());
            sv.setEnabled(is3D.isChecked() && !l);
            sh.setEnabled(is3D.isChecked() && !l);
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
            isInvGrad.setEnabled(isHGrad.isChecked() || isVGrad.isChecked());
            isDarker.setEnabled(isHGrad.isChecked() || isVGrad.isChecked());
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
            sv.setEnabled(is3D.isChecked() && !line.isVisible());
            sh.setEnabled(is3D.isChecked() && !line.isVisible());
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
