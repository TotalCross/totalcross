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

package samples.apps.salesplus.ui.customer;

import samples.apps.salesplus.db.*;
import samples.apps.salesplus.*;
import totalcross.ui.*;
import totalcross.ui.event.*;
import totalcross.ui.dialog.*;
import totalcross.ui.gfx.Color;

/**
 * The customer search menu.
 */
public class CustomerSearch extends Container
{
   /**
    * The back button;
    */
   private Button btnBack;
   
   /**
    * The search button.
    */
   private Button btnSearch;
   
   /**
    * The edit button.
    */
   private Button btnEdit;
   
   /**
    * The remove button.
    */
   private Button btnRemove;
   
   /**
    * The result grid.
    */
   private Grid grid;
   
   /**
    * The name edit.
    */
   private Edit edName;

   /**
    * Initializes the user interface.
    */
   public void initUI()
   {
      Button.commonGap = 1;

      // Name edit and label.
      add(new Label("Name: "), LEFT + 5, TOP + 5);
      add(edName = new Edit(""), AFTER, SAME, PREFERRED - 5, PREFERRED);
      
      // Buttons.
      add(btnSearch = new Button("Search"), RIGHT - 5, AFTER + 5, PREFERRED, PREFERRED);
      add(btnEdit = new Button("View"), RIGHT - 1, BOTTOM - 1);
      add(btnBack = new Button("Back"), BEFORE - 2, SAME);
      add(btnRemove = new Button("Remove"), BEFORE - 2, SAME);
      
      // Grid.
      add(grid = new Grid(new String[]{"Name", "City", "State"}, 
                 new int[]{fm.stringWidth("xxxxxxxxxxx"), fm.stringWidth("xxxxxxxxxx"), fm.stringWidth("xxx")}, 
                 new int[]{LEFT, LEFT, CENTER}, false), LEFT + 5, AFTER + 5, FILL - 5, FIT, btnSearch);
      grid.verticalLineStyle = Grid.VERT_DOT;
      grid.setBackColor(Color.WHITE);

      Button.commonGap = 0;
   }

   /**
    * Fetches a selected customer.
    * 
    * @return The customer data.
    */
   private Customer getSelectedCustomer()
   {
      int idx = grid.getSelectedIndex();
      if (idx < 0) 
         return null;
      return CustomerDB.getFromName(grid.getItem(idx)[0]);
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
         if (event.target == btnBack) // Pressed back button: goes to the customer menu.
            MainWindow.getMainWindow().swap(SalesPlus.screens[SalesPlus.CUSTOMER_MENU]);
         else if (event.target == btnSearch) // Pressed search button: searches for a customer.
            onAddAgain();
         else if (event.target == btnRemove) // Pressed removed button: removes the selected customer if possible.
         {
            Customer customer = getSelectedCustomer();
            if (customer != null)
            {
               if (!OrderDB.hasOrder(customer)) // The customer can't have a order.
               {
                  CustomerDB.remove(customer);
                  onAddAgain();
               }
               else
                  new MessageBox("Error", "There are orders that belong to | this customer. If you really want |" 
                                        + "to remove it, delete the orders first.").popupNonBlocking();
            }
         }
         else
         if (event.target == btnEdit) // Pressed edit button: edits the selected customer.
         {
            Customer cli = getSelectedCustomer();
            if (cli != null)
            {
               NewCustomer.sentCustomer = cli;
               MainWindow.getMainWindow().swap(SalesPlus.screens[SalesPlus.NEW_CUSTOMER]);
            }
         }
      }
   }

   /**
    * Updates the grid searching the customer again.
    */
   public void onAddAgain()
   {
      Customer[] customers = CustomerDB.searchCustomers(edName.getText());
      int count = customers.length;
      
      grid.removeAllElements();
      
      if (count != 0)
      {
         String[][] strings = new String[count][3];
         Customer customer;
         int i = count;
         
         while (--i >= 0)
         {
            strings[i][0] = (customer = customers[i]).name;
            strings[i][1] = customer.city;
            strings[i][2] = customer.name;
         }
         grid.setItems(strings);
      }
   }
}
