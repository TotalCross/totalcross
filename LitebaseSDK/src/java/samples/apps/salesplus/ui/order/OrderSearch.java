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

package samples.apps.salesplus.ui.order;

import samples.apps.salesplus.db.*;
import samples.apps.salesplus.*;
import totalcross.ui.gfx.*;
import totalcross.ui.*;
import totalcross.ui.event.*;
import totalcross.ui.dialog.*;
import totalcross.util.*;
import totalcross.sys.*;

/**
 * The order search menu.
 */
public class OrderSearch extends Container
{
   /**
    * The date edit.
    */
   private Edit edDate;
   
   /**
    * The result grid.
    */
   private Grid grid;
   
   /**
    * The search button.
    */
   private Button btnSearch; 
   
   /**
    * The back button;
    */
   private Button btnBack; 
   
   /**
    * The view button.
    */
   private Button btnView; 
   
   /**
    * The remove button.
    */
   private Button btnRemove;
   
   /**
    * The customer combo box.
    */
   private ComboBox cbCustomer;

   /**
    * Initializes the user interface.
    */
   public void initUI()
   {      
      Button.commonGap = 1;

      // Customer label and combo box.
      add(new Label("Customer: "), LEFT + 2, TOP + 2);
      add(cbCustomer = new ComboBox(), AFTER, SAME, FILL, PREFERRED);
      
      // Date label and edit.
      add(new Label("Date: "), LEFT + 2, AFTER + 2);
      add(edDate = new Edit("XX/XX/XXXX"), cbCustomer.getRect().x, SAME);
      edDate.setMode(Edit.DATE, true);
      
      // The buttons.
      add(btnSearch = new Button("Search"), AFTER + 2, SAME);
      add(btnView = new Button("View"), RIGHT - 1, BOTTOM - 1);
      add(btnBack = new Button("Back"), BEFORE - 2, SAME);
      add(btnRemove = new Button("Remove"), BEFORE - 2, SAME);

      // The grid.
      add(grid = new Grid(new String[]{"ID", "Date", "Customer", "Total Amount"}, 
                 new int[]{fm.stringWidth("xx"), fm.stringWidth("XX/XX/XXXX"), fm.stringWidth("name of the customer"), fm.stringWidth("123456789")}, 
                 new int[]{LEFT, CENTER, LEFT, RIGHT}, false), LEFT + 5, AFTER + 7, FILL - 5, FIT, btnSearch);
      grid.verticalLineStyle = Grid.VERT_DOT;
      grid.setBackColor(Color.WHITE);
      
      // Loads the customer names in the combo box.
      String[] names = CustomerDB.readAllNames();
      if (names.length == 0)
         new MessageBox("Error", "There are no customers registered").popup();
      else
         cbCustomer.add(names);
   }

   /**
    * Updates the grid.
    */
   public void onAddAgain()
   {      
      String nameStr = (String)cbCustomer.getSelectedItem();
      String dateStr = edDate.getText();
      Order[] orders = null;
      
      try // Tries to find the desired order.
      {
         orders = OrderDB.search(nameStr, dateStr);
      }
      catch (InvalidDateException exception)
      {
         new MessageBox("Error", exception.getMessage()).popup();
         return;
      }
      
      int count = orders.length;
      
      if (count == 0) // No order found: empties the grid.
         grid.removeAllElements();
      else // Fills the grid with the orders found.
      {
         String[][] strings = new String[count][4];
         int i = -1;

         while (++i < count)
         {            
            strings[i][0] = Convert.toString(orders[i].orderId);
            strings[i][1] = dateStr;
            strings[i][2] = nameStr;
            strings[i][3] = Convert.toString((double)orders[i].totalAmount * 0.01, 2);            
         }
         grid.setItems(strings);
      }
   }

   /**
    * Returns the selected order in the grid.
    * 
    * @return The selected order.
    */
   private Order getSelectedOrder()
   {
      int idx = grid.getSelectedIndex();
      
      if (idx < 0) 
         return null;
      String[] strings = grid.getItem(idx);
      
      try
      {
         return OrderDB.getFromId(Convert.toInt(strings[0]));
      }
      catch (InvalidNumberException exception)
      {
         return null; // Never occurs.
      }
   }

   /**
    * Called to the posted events.
    *
    * @param event The posted event.
    */
   public void onEvent(Event event)
   {
      switch (event.type)
      {
         case ControlEvent.PRESSED:
            if (event.target == btnBack) // Back button: goes back to the previous menu.
               MainWindow.getMainWindow().swap(SalesPlus.screens[SalesPlus.ORDER_MENU]);
            else if (event.target == btnSearch) // Search button: updates the grid with the results.
               onAddAgain();
            else if (event.target == btnView) // View button: views more details of the selected order.
            {
               Order order = getSelectedOrder();
               
               if (order != null) // If there is a selected order, views it in detail.
               {
                  NewOrder.currentOrder = order;
                  NewOrder.update = true;
                  MainWindow.getMainWindow().swap(SalesPlus.screens[SalesPlus.NEW_ORDER]);
               }
            }
            else if (event.target == btnRemove) // Remove button: removes the selected order.
            {
               Order order = getSelectedOrder();
               if (order != null)
               {
                  OrderDB.removeOrder(order);
                  ItemOrderDB.removeAll(order.orderId);
                  onAddAgain();
               }
            }
            break;

         case ControlEvent.FOCUS_OUT: // Corrects the data inserted to the date format.
            if (event.target == edDate && edDate.getLength() > 0)
            {               
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
}
