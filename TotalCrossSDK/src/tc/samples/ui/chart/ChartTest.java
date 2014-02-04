/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
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



package tc.samples.ui.chart;

import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.chart.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;

public class ChartTest extends MainWindow
{
   static 
   {
      Settings.applicationId = "Chrt";
      Settings.appVersion = "1.1";
   }

   
   ColumnChart column;
   LineChart line;
   PieChart pie;
   MenuBar mbar;
   RadioGroupController rgType;
   Slider sh,sv;

   Check is3D,showTitle,showCategories,showHGrids,
     showVGrids, isVGrad, isHGrad, isInvGrad, isDarker, showYValues;
   ComboBox legendPosition;
   Button bt;

   public ChartTest()
   {
      super("Chart Test", TAB_ONLY_BORDER);
   }

   public void initUI()
   {
      mbar = new MenuBar(new MenuItem[][] {new MenuItem[] {new MenuItem("File"), new MenuItem("Exit")}});
      setMenuBar(mbar);

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
      add(new Radio("Pie", rgType), RIGHT, 0);
      add(new Radio("Line", rgType), BEFORE-2, SAME);
      add(new Radio("Column",rgType), BEFORE-2, SAME);
      rgType.setSelectedIndex(2);
      add(bt = new Button("*"),BEFORE-fmH/2,SAME,PREFERRED,SAME);

      int gap =Settings.screenWidth > 320 ? fmH/2 : 0;
      add(showTitle, LEFT, TOP + 2+gap);
      add(legendPosition, AFTER + 2, SAME,PREFERRED,SAME);
      add(showCategories, AFTER + 2, SAME);
      add(showYValues, AFTER+2, SAME);
      add(showHGrids, LEFT, AFTER + 2 + gap);
      add(showVGrids, AFTER + 2, SAME);
      add(is3D, AFTER + 2, SAME);

      int r = width - is3D.getRect().x2()-6;
      add(sh = new Slider(),AFTER+2,SAME,r/2,fmH);
      add(sv = new Slider(),AFTER+2,SAME,r/2,fmH);
      sh.setMinimum(-6); sh.setMaximum(6); sv.setMaximum(6);
      sh.drawTicks = sv.drawTicks = true;
      sh.drawFilledArea = sv.drawFilledArea = false;
      sv.setValue(column.perspectiveV);
      sh.setValue(column.perspectiveH);
      sh.setLiveScrolling(true);
      sv.setLiveScrolling(true);

      add(new Label("Gradient"), LEFT, AFTER+2+gap, PREFERRED, SAME, is3D);
      add(isHGrad, AFTER+2, SAME);
      add(isVGrad, AFTER+2, SAME);
      add(isInvGrad, AFTER+2, SAME);
      add(isDarker, AFTER+2, SAME);
      add(new Ruler(),LEFT,AFTER+1);
      isInvGrad.setEnabled(false);
      isDarker.setEnabled(false);

      add(column, LEFT, AFTER+2, FILL,FILL);
      column.setBackColor(Color.darker(backColor,16));

      // setup the line chart
      line = new LineChart(names);
      line.series.addElement(new Series("Rice", new double[] {100, 102, 104, 106}, Color.YELLOW));
      line.series.addElement(new Series("Beans", new double[] {150, 155, 159, 164}, Color.GREEN));
      line.series.addElement(new Series("Oil", new double[] {130, 137, 143, 150}, Color.RED));
      line.lineThickness = 2;
      line.setTitle("Sales Projection");
      line.setYAxis(0, 200, 10);
      add(line, SAME, SAME, SAME, SAME);
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
      add(pie, SAME, SAME, SAME, SAME);
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
            for (int i = 0; i < 50; i++)
               repaintNow();
            int fim = Vm.getTimeStamp();
            new MessageBox("Benchmark", "Elapsed: "+(fim-ini)+"ms").popup();
         }
         else
         if (e.target == mbar && mbar.getSelectedIndex() == 1)
            exit(0);
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
