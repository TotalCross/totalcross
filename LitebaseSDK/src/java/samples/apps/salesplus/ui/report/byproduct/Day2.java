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

package samples.apps.salesplus.ui.report.byproduct;

import samples.apps.salesplus.db.*;
import samples.apps.salesplus.*;
import totalcross.ui.*;
import totalcross.ui.dialog.MessageBox;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;
import totalcross.sys.*;
import totalcross.util.*;

/**
 * The report by product and day.
 */
public class Day2 extends Container
{
   /**
    * The Ok button.
    */
   private Button btnOk;
   
   /**
    * The back button.
    */
   private Button btnBack;
   
   /**
    * The date edit.
    */
   private Edit edDate; 
   
   /**
    * The total edit.
    */
   private Edit edTotal;
   
   /**
    * The result grid.
    */
   private Grid grid;

   /**
    * Initializes the user interface.
    */
   public void initUI()
   {      
      // Date label and edit.
      add(new Label("Date: "), LEFT + 5, TOP + 5);
      add(edDate = new Edit("XX/XX/XXXX"), AFTER, SAME);
      edDate.setMode(Edit.DATE, true);
      edDate.setText(SalesPlus.dateAux.toString());      
      
      // Buttons.
      add(btnOk = new Button("Ok"), AFTER + 5, SAME);
      add(btnBack = new Button("Back"), RIGHT - 2, BOTTOM - 1);
      
      // The grid.
      add(grid = new Grid(new String[]{"Product", "Qty", "Amount"}, 
                          new int[]{fm.stringWidth("xxxxxxxxx"), fm.stringWidth("xxxx"), fm.stringWidth("xxxxxxxx")}, 
                          new int[]{LEFT, CENTER, RIGHT}, false), LEFT + 5, AFTER + 5, FILL - 5, FIT, btnOk);
      grid.verticalLineStyle = Grid.VERT_DOT;
      grid.setBackColor(Color.WHITE);

      // Total label and edit.
      add(new Label("Total: "), LEFT + 5, AFTER);
      add(edTotal = new Edit("xxxxxxx"), AFTER + 2, SAME);
      edTotal.setEditable(false);
      edTotal.alignment = RIGHT;
   }

   /**
    * Called to process the posted events.
    *
    * @param event The posted event.
    */
   public void onEvent(Event event)
   {
      switch (event.type)
      {
         case ControlEvent.PRESSED:
            if (event.target == btnOk) // Ok button: does the search.
            {
               Date date = SalesPlus.dateAux;
               try
               {
                  date.set(edDate.getText(), Settings.dateFormat);
               }
               catch (InvalidDateException exception)
               {
                  new MessageBox("Error", exception.getMessage()).popup();
                  return;
               }
               
               Order[] orders = OrderDB.getFromDate(date.getDateInt());
               int count1 = orders.length;
               
               grid.removeAllElements();
               if (count1 > 0)
               {
                  String[][][] strings = new String[count1][][];
                  ItemOrder[] items;
                  ItemOrder item;
                  int i1 = -1,
                      total = 0,
                      i2,
                      count2;
                 
                  while (++i1 < count1)
                  {                     
                     strings[i1] = new String[count2 = (items = ItemOrderDB.readAll(orders[i1].orderId)).length][3];
                     i2 = -1;
                     
                     while (++i2 < count2)
                     {
                        strings[i1][i2][0] = ProductDB.getFromCode((item = items[i2]).productId).name;
                        strings[i1][i2][1] = Convert.toString(item.quant);
                        strings[i1][i2][2] = Convert.toString((double)item.totalAmount * 0.01, 2);
                        total = total + item.totalAmount;
                     }
                     grid.add(strings[i1]);
                  }
                  
                  edTotal.setText(Convert.toString((double)total * 0.01, 2));
               }
            } 
            else if (event.target == btnBack) // Back button: goes back to the previous menu.
               MainWindow.getMainWindow().swap(SalesPlus.screens[SalesPlus.BY_PRODUCT_MENU]);            
            break;
            
         case ControlEvent.FOCUS_OUT: // Corrects the data inserted to the date format.
            
            if (event.target == edDate && edDate.getLength() > 0)
               try
               {
                  SalesPlus.dateAux.set(edDate.getText(), Settings.dateFormat);
               }
               catch (InvalidDateException exception)
               {
                  new MessageBox("Error", exception.getMessage()).popup();
               }   
      }
   }
}
