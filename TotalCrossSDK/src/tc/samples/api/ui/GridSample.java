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

import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.font.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;
import totalcross.util.*;

public class GridSample extends BaseContainer
{
   TabbedContainer tp;

   public void initUI()
   {
      super.initUI();
      String tabCaptions[] = { "Line", "Check 1", "Check 2", "Edit&Pop", "Sort", "CellControl", "Image" };
      tp = new TabbedContainer(tabCaptions);
      tp.setBackColor(Color.brighter(BKGCOLOR));
      tp.extraTabHeight = fmH/2;
      tp.activeTabBackColor = Color.ORANGE;
      tp.pressedColor = Color.YELLOW;
      add(tp, LEFT+fmH/2,TOP,FILL-fmH/2,FILL);
      tp.setContainer(0, new Grid1());
      tp.setContainer(1, new Grid2());
      tp.setContainer(2, new Grid3());
      tp.setContainer(3, new Grid4());
      tp.setContainer(4, new Grid5());
      tp.setContainer(5, new Grid6());
      tp.setContainer(6, new Grid7());
   }

   private class Grid1 extends Container
   {
      Grid grid;
      Button btnAdd, btnRemove, btnChange;

      public void initUI()
      {
         setBackColor(Color.WHITE);
         add(btnRemove = new Button("Remove Line"), LEFT, TOP+2);
         add(btnAdd = new Button("Add Line"), AFTER+2, SAME);
         add(btnChange = new Button("Replace Line"), AFTER+2, SAME);
         add(new Label("Grid 1"), RIGHT - 1, BOTTOM -1);

         String []gridCaptions = {" Caption 1 ", " Caption 2 ", " Caption 3 ", " Caption 4 ",
               "Caption 5", "Caption 6", "Caption 7" };
         Grid.useHorizontalScrollBar = true;
         grid = new Grid(gridCaptions, false);
         Grid.useHorizontalScrollBar = false;
         grid.firstStripeColor = Color.GREEN;
         grid.secondStripeColor = Color.YELLOW;
         grid.verticalLineStyle = Grid.VERT_NONE;
         add(grid, LEFT, AFTER+2, FILL, FIT, btnRemove);

         String items[][] = new String[50][7];
         for(int i = 0; i < 50; i++)
            for(int j = 0; j < 7; j++)
               items[i][j] = "BRAZIL "+i;

         grid.setItems(items);
      }

      public void onEvent(Event e)
      {
         switch(e.type)
         {
            case ControlEvent.PRESSED:
               if(e.target == btnAdd)
               {
                  String newLine[] = {"column 1", "column 2", "column 3", "column 4",
                     "column 5","column 6","column 7"};
                  grid.add(newLine);
                  //its up to the programmer to call repaint() after inserting a new line
                  repaint();
               }
               else
               if(e.target == btnRemove)
               {
                  int idx = grid.getSelectedIndex();
                  if(idx == -1)
                     return;

                  grid.del(idx);

                  // its up to the programmer to call repaint() after deleting a new line
                  repaint();
               } else
               if(e.target == btnChange)
               {
                  int index = grid.getSelectedIndex();
                  if(index == -1)
                     return;

                  String newLine[] = {"new line 1", "new line 2", "new line 3", "new line 4",
                              "new line 5", "new line 6", "new line 7" };
                  grid.replace(newLine, index);

                  // its up to the programmer to call repaint() after replacing a new line
                  repaint();
               }
               break;
         }
      }
   }

   private class Grid2 extends Container
   {
      Grid grid;
      Label lMarked;
      Button btnShowMarked;

      public void initUI()
      {
         setBackColor(Color.WHITE);
         String []gridCaptions = {"Name", "Address", "Phone" };
         int gridWidths[] = {-25, -50, -25};
         int gridAligns[] = {LEFT,LEFT,LEFT};
         String items[][] =
         {
            {"SuperWaba Ltda.", "Av. Padre Leonel Franca, 480, sala 27A", "2222-2222"},
            {"Company A",       "Av. Ataulfo de Paiva",                   "2233-2233"},
            {"Company B",       "Av. Nossa Senhora de Copacabana",        "2244-2244"},
            {"Company C",       "Av. Vieira Souto",                       "2343-3434"}
         };

         grid = new Grid(gridCaptions, gridWidths, gridAligns, true);
         add(btnShowMarked = new Button("Show list of checked lines"), CENTER, AFTER + 2);
         add(lMarked = new Label("Marked: "), LEFT, BOTTOM -1, FILL, PREFERRED);

         add(grid, LEFT+5, AFTER+2, FILL-10, FIT, btnShowMarked);
         grid.checkColor = Color.RED;
         grid.setItems(items);
      }

      public void onEvent(Event e)
      {
         switch(e.type)
         {
            case ControlEvent.PRESSED:
               if(e.target == btnShowMarked)
               {
                  int n = grid.size();
                  StringBuffer sb = new StringBuffer("Marked: ");
                  for(int i =0; i < n; i++)
                     if(grid.isChecked(i))
                        sb.append(' ').append(i);
                  lMarked.setText(sb.toString());
               }
         }
      }
   }

   private class Grid3 extends Container
   {
      String items[][] = new String[45][3];
      Grid grid;
      Button btnFill, btnClear, btnMark;
      Edit edSelected;

      public void initUI()
      {
         setBackColor(Color.WHITE);
         add(btnFill = new Button(" Fill "), LEFT + 2, TOP+2);
         add(btnClear = new Button(" Clear "), AFTER + 2, SAME);
         add(btnMark = new Button(" Mark some "), AFTER + 2, SAME);
         add(new Label("Selected Line x Column: "), LEFT + 2, BOTTOM - 3);
         add(edSelected = new Edit(""), AFTER, SAME);
         edSelected.setEditable(false);
         edSelected.hasCursorWhenNotEditable = false;

         String []gridCaptions = { "Info 1", "Info 2", "Info 3" };
         grid = new Grid(gridCaptions, true);
         add(grid);
         grid.drawCheckBox = false;
         grid.captionsBackColor = Color.CYAN; // background color of captions
         grid.checkColor = Color.BLUE;        // color of the check tick
         grid.highlightColor = Color.MAGENTA; // color of the selected line
         grid.verticalLineStyle = Grid.VERT_LINE; // vertical line style
         grid.lineScroll = true;
         grid.setRect(LEFT, AFTER + 2, FILL, FIT, btnMark);
         btnClear.setEnabled(false);
         btnMark.setEnabled(false);
         edSelected.setText("" + grid.getSelectedIndex());
      }

      Random r = new Random();
      public void onEvent(Event e)
      {
         switch(e.type)
         {
            case ControlEvent.PRESSED:
               if(e.target == btnFill)
               {
                  for(int i = items.length-1; i >= 0; i--)
                     for(int j = items[i].length-1; j >= 0; j--)
                        items[i][j] =(j+1)+"/"+i;

                  grid.setItems(items);
                  btnClear.setEnabled(true);
                  btnMark.setEnabled(true);
               }
               else
               if(e.target == btnClear)
               {
                  btnMark.setEnabled(false);
                  btnClear.setEnabled(false);
                  grid.removeAllElements();
               }
               else
               if(e.target == btnMark)
               {
                  int n = grid.size();
                  for(int i =0; i < 20; i++)
                     grid.setChecked(r.between(0,n-1),r.between(0,2)==1);
                  repaint();
               }
               break;
            case GridEvent.CHECK_CHANGED_EVENT:
               if(e.target == grid)
                  edSelected.setText(((GridEvent)e).checked ? "checked":"unchecked");
               break;
            case GridEvent.SELECTED_EVENT:
               if(e.target == grid)
               {
                  GridEvent ge =(GridEvent)e;
                  edSelected.setText(ge.col+"x"+ge.row+": "+grid.getCellText(ge.row, ge.col));
               }
               break;
         }
      }
   }
   private class Grid4 extends Container
   {
      Grid grid;
      Label lText;

      public void initUI()
      {
         setBackColor(Color.WHITE);
         String []gridCaptions = {"Platform", "Resolutions", "Comments" };
         int ww = fm.stringWidth("xxxxxxxxxxx");
         int gridWidths[] = {ww, ww, ww};
         int gridAligns[] = {LEFT,LEFT,LEFT};
         String items[][] =
         {
            {"Android", "320x480", "All in one"},
            {"Palm OS", "160x160 320x320", "Easy to use"},
            {"Windows CE", "240x320", "Most stable"},
            {"iPhone",  "320x480", "TotalCross runs on it!"}
         };

         grid = new Grid(gridCaptions, gridWidths, gridAligns, false);
         add(lText = new Label("Text: "), LEFT, BOTTOM, FILL, PREFERRED);

         add(grid, LEFT+5, TOP+1, FILL-10, FIT+1);
         grid.setItems(items);
         grid.setColumnChoices(0,new String[]{items[0][0],items[1][0],items[2][0],items[3][0]});
         grid.setColumnEditable(1,true);
         grid.setColumnEditable(2,true);
      }

      public void onEvent(Event e)
      {
         switch(e.type)
         {
            case GridEvent.TEXT_CHANGED_EVENT:
               if(e.target == grid)
               {
                  GridEvent ge =(GridEvent)e;
                  lText.setText("Text: "+grid.getCellText(ge.row,ge.col));
               }
         }
      }
   }
   private class Grid5 extends Container
   {
      Grid grid;

      public void initUI()
      {
         try
         {
            setBackColor(Color.WHITE);
            String []gridCaptions = {" Int ", " Double ", " Date ", " String "};
            String items[][] =
            {
               {"10",  "3000.5",   new Date(19700325).toString(), "Bárbara"},
               {"1",   "1e4",      new Date(19750612).toString(), "André"},
               {"999", "-100.10",  new Date(19720519).toString(), "Júlia"},
               {"-5",  "3.141516", new Date(19640314).toString(), "Verinha"}
            };

            add(grid = new Grid(gridCaptions, false), LEFT+5, TOP+2, FILL-5, FILL-5);
            grid.setItems(items);
         } catch (InvalidDateException ide) {} // will not occur
      }
   }
   private class Grid6 extends Container
   {
      Grid grid;

      class CellController6 extends Grid.CellController
      {
         String[] choices0 = {"Verinha","Júlia","Barbara"};
         String[] choices3 = {"Renato","Zelia","Helio"};

         int[][] colors;

         public CellController6()
         {
            Random r = new Random();
            colors = new int[4][3];
            for(int j = 0; j < 4; j++)
               for(int i = 0; i < 3; i++)
               colors[j][i] = Color.getRGB(r.between(0,128), r.between(0,192), 255);
         }

         public int getBackColor(int row, int col)
         {
            return col < 2 ? colors[row][col] : -1;
         }

         public int getForeColor(int row, int col)
         {
            return row < 2 ? Color.BLACK : Color.RED;
         }

         public String[] getChoices(int row, int col)
         {
            if(row == 0)
               return choices0;
            if(row == 3)
               return choices3;
            return null;
         }

         public boolean isEnabled(int row, int col)
         {
            if((row == 1 && col == 1) ||(row == 0 && col == 0))
               return false;
            return true;
         }

         public Font getFont(int row, int col)
         {
            return (row == 0 && col == 1) ? grid.getFont().asBold() : null;
         }
      }

      public void initUI()
      {
         setBackColor(Color.WHITE);
         String []gridCaptions = {"Choice             ", "Value"};
         String items[][] =
         {
            {"choose it", "check disabled"},
            {"can't choose", "disabled combo"},
            {"choose it", "default values"},
            {"choose it", "urca family"}
         };

         add(grid = new Grid(gridCaptions, true));
         grid.setVisibleLines(4);
         grid.setRect(LEFT+5, TOP+2, FILL-5, PREFERRED);
         grid.checkColor = Color.RED;
         grid.highlightColor = Color.YELLOW;
         grid.setColumnChoices(0, new String[]{"Adams family","Tom and Jerry"});
         grid.setCellController(new CellController6());
         grid.setItems(items);
      }
   }

   private class Grid7 extends Container
   {
      public void initUI()
      {
         try
         {
            try
            {
               String []gridCaptions = {"Image", "Name", "Details" };
               int gridWidths[] = {-25, -50, -25};
               int gridAligns[] = {CENTER,LEFT,LEFT};
               String items[][] =
               {
                  {"@car1", "Car number one", "good car"},
                  {"@car2", "Car number two", "great car"},
               };
         
               Grid grid = new Grid(gridCaptions, gridWidths, gridAligns, false);
               add(grid, LEFT+5, AFTER+2, FILL-10, PREFERRED);
               grid.setImage("@car1",new Image("ui/images/car1.png"));
               grid.setImage("@car2",new Image("ui/images/car2.png"));
               grid.setItems(items);
            }
            catch (Exception ee)
            {
               MessageBox.showException(ee,true);
            }
         } catch (Exception ide) {ide.printStackTrace();}
      }
   }
}
