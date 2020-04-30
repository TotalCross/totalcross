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

package samples.apps.salesplus.ui.report.summary;

import samples.apps.salesplus.db.*;
import samples.apps.salesplus.*;
import totalcross.ui.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;
import totalcross.sys.*;
import totalcross.util.*;

/**
 * The report by summary and month.
 */
public class Month extends Container
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
    * The total edit.
    */
   private Edit edTotal;
   
   /**
    * The combo box for years.
    */
   private ComboBox combYear; 
   
   /**
    * The combo box for months.
    */
   private ComboBox combMonth;
   
   /**
    * The result grid.
    */
   private Grid grid;
   
   /**
    * Initializes the user interface.
    */
   public void initUI()
   {      
      SalesPlus.dateAux.setToday();
      
      // Year label and combo box.
      add(new Label("Year "), LEFT + 5, TOP + 5);
      add(combYear = new ComboBox(SalesPlus.years), AFTER, SAME);
      combYear.setSelectedItem(Convert.toString(SalesPlus.dateAux.getYear()));
      
      // Month label and combo box.
      add(new Label("Month "), AFTER + 5, SAME);
      add(combMonth = new ComboBox(Date.monthNames), AFTER, SAME);
      combMonth.setSelectedIndex(SalesPlus.dateAux.getMonth());
      
      // Buttons.
      add(btnOk = new Button("Ok"), AFTER + 5, SAME);
      add(btnBack = new Button("Back"), RIGHT - 2, BOTTOM - 1);
      
      // The grid.
      add(grid = new Grid(new String[]{"Day", "Amount"}, new int[]{fm.stringWidth("xxxxxx"), fm.stringWidth("xxxxxxxxxx")}, 
                          new int[]{CENTER, RIGHT}, false), LEFT + 5, AFTER + 5, FILL - 5, FIT, btnOk);
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
      if (event.type == ControlEvent.PRESSED)
      {
         if (event.target == btnOk)
         {
            Order[] orders = OrderDB.readAllDays(combYear.getSelectedIndex() + 2005, combMonth.getSelectedIndex());
            int count = orders.length;
            
            grid.removeAllElements(); 
            if (count > 0)
            {
               String[][] strings = new String[count][2];
               Order order;
               int i = -1,
                   total = 0;
               
               while (++i < count)
               {
                  total = total + (order = orders[i]).totalAmount;
                  strings[i][0] = Convert.toString(order.date % 100);
                  strings[i][1] = Convert.toString((double)order.totalAmount * 0.01, 2);                  
               }

               grid.setItems(strings);
               edTotal.setText(Convert.toString((double)total * 0.01, 2));
            }
         }
         else if (event.target == btnBack) // Back button: goes back to the previous menu.
            MainWindow.getMainWindow().swap(SalesPlus.screens[SalesPlus.BY_PRODUCT_MENU]);
      } 
   }
}
