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

import samples.apps.salesplus.*;
import samples.apps.salesplus.db.*;
import totalcross.ui.*;
import totalcross.ui.event.*;
import totalcross.ui.dialog.*;
import totalcross.ui.gfx.*;
import totalcross.sys.*;
import totalcross.util.*;

/**
 * The new order menu.
 */
public class NewOrder extends Container
{
   /**
    * Indicates if the order is to be updated or inserted.
    */
   static boolean update;
   
   /**
    * An auxiliary order.
    */
   static Order currentOrder = new Order();
   
   /**
    * The product vector.
    */
   private Vector products = new Vector();
   
   /**
    * The deleted products vector.
    */
   private Vector vDeleted = new Vector();
   
   /**
    * The quantity of each product.
    */
   private IntVector vQuant = new IntVector();
   
   /**
    * The order grid.
    */
   private Grid grid;
   
   /**
    * The order date edit.
    */
   private Edit edDate; 
   
   /**
    * The order number edit.
    */
   private Edit edNum; 
   
   /**
    * The order total edit.
    */
   private Edit edTot;
   
   /**
    * A spin list to increase or decrease product quantity.
    */
   private SpinList spin;
   
   /**
    * The button to add a product to the order.
    */
   private Button btnAdd; 
   
   /**
    * The button to remove a product from the order.
    */
   private Button btnRemove; 
   
   /**
    * The back button.
    */
   private Button btnBack; 
   
   /**
    * The clear button.
    */
   private Button btnClear; 
   
   /**
    * The insert or update button.
    */
   private Button btnInsert;
   
   /**
    * The customer combo box.
    */
   private ComboBox cbCustomer; 
   
   /**
    * The product combo box.
    */
   private ComboBox cbProduct;

   /**
    * Initializes the user interface.
    */
   public void initUI()
   {            
      // The container before the grid. 
      Container container = new Container();
      add(container, LEFT + 2, TOP, FILL - 4, FILL);
      container.setBorderStyle(BORDER_RAISED);
      container.setBackColor(0xf0f0ff);

      // The order date edit and label.
      container.add(edDate = new Edit("XX/XX/XXXX"), RIGHT, TOP + 1);
      edDate.setMode(Edit.DATE, true);
      SalesPlus.dateAux.setToday();
      edDate.setText(SalesPlus.dateAux.toString());
      container.add(new Label("Date "), BEFORE, SAME);

      // Sets the current order id.
      if (currentOrder.orderId == 0)
         currentOrder.orderId = OrderDB.getLastOrderId();

      // The order number label and edit.
      container.add(new Label("Order no. "), LEFT + 2, SAME);
      container.add(edNum = new Edit("xxx"), AFTER + 20, SAME);
      edNum.setText("" + currentOrder.orderId);
      edNum.setEditable(false);

      // Customer label and combo box.
      container.add(new Label("Customer "), LEFT + 2, AFTER + 5);
      container.add(cbCustomer = new ComboBox(), edNum.getRect().x, SAME, FILL, PREFERRED);
      container.setRect(LEFT + 2, TOP + 2, FILL - 4, cbCustomer.getRect().y2() + 4);

      // The product label and combo box.
      add(new Label("Product "), LEFT + 2, AFTER + 2);
      add(cbProduct = new ComboBox(), AFTER, SAME, PREFERRED + Settings.screenWidth / 5, PREFERRED);

      // Quantity label and spin list.
      add(new Label("Qty "), AFTER + 3, SAME);
      try
      {
         add(spin = new SpinList(new String[]{"[1,99]"}, false), AFTER, SAME);
      }
      catch (InvalidNumberException exception) {}
      spin.hAlign = CENTER;

      Button.commonGap = 1;

      // The buttons to add or remove a product from the order.
      add(btnAdd = new Button(" + "), AFTER + 3, SAME);
      add(btnRemove = new Button(" - "), AFTER + 1, SAME);
      
      // The buttons for insert or update, cancel or back, and clear. 
      add(btnInsert = new Button("Insert "), RIGHT - 1, BOTTOM - 1);
      add(btnBack = new Button("Back"), BEFORE - 2, BOTTOM - 1);
      add(btnClear = new Button("Clear"), BEFORE - 2, BOTTOM - 1);
      
      // The grid.
      add(grid = new Grid(new String[]{"Name", "Code", "Qty", "Price"}, 
                          new int[]{fm.stringWidth("xxxxxxxxxx"), fm.stringWidth("xxxxxxxx"), fm.stringWidth("xxx"), fm.stringWidth("xxxxxxx")}, 
                          new int[]{LEFT, LEFT, LEFT, LEFT}, false), LEFT + 5, AFTER + 10, FILL - 5, FIT, btnAdd);
      grid.verticalLineStyle = Grid.VERT_DOT;
      
      // The order total cost edit.
      add(edTot = new Edit("xxxxxxx"), LEFT + 2, AFTER);
      edTot.setText("0.00");
      edTot.setEditable(false);

      Button.commonGap = 0;

      // Sets some components colors.
      edTot.setBackColor(Color.DARK);
      grid.setBackColor(Color.WHITE);
      edNum.setBackColor(Color.DARK);
      container.setBackColor(0xf0f0ff);
      
      Button.commonGap = 0;
      onAddAgain();
   }

   /**
    * Sets the new order menu to insert or update an order. 
    */
   public void onAddAgain()
   {
      String[] names = CustomerDB.readAllNames();
      
      cbCustomer.removeAll();
      
      if (names.length == 0) // There are no customer, so there can't add an order.
         new MessageBox("Error", "There are no customers registered").popup();
      else
      {
         cbCustomer.add(names); // Adds all customer names to the combo box.

         cbProduct.removeAll();
         names = ProductDB.readAllNames();

         if (names.length == 0) // There are no products, so there can't add an order.
            new MessageBox("Error", "There are no products registered").popup();
         else
         {
            cbProduct.add(names); // Adds all product names to the combo box.

            if (update) // The order is being updated.
            {
               ItemOrder[] items = ItemOrderDB.readAll(currentOrder.orderId);
               ItemOrder item;
               Product product;
               int length = items.length,
                   i = -1;
               
               while (++i < length) // Lists all the products and their quantities.
                  if ((product = ProductDB.getFromCode((item = items[i]).productId)) != null)
                  {
                     products.addElement(product);
                     vQuant.addElement(item.quant);
                  }

               // Sets the user interface.
               cbCustomer.setSelectedItem(currentOrder.customer);
               btnInsert.setText("Update");
               edTot.setText(Convert.toString((double)totalValue() * 0.01, 2));
               fillGrid();
            }
            else
            {
               btnInsert.setText("Insert ");
               currentOrder.orderId = OrderDB.getLastOrderId();
            }
            edNum.setText("" + currentOrder.orderId);
         }
      }
   }

   /**
    * Fills the order grid.
    */
   private void fillGrid()
   {
      int count = products.size(),
          i = -1;
      
      grid.removeAllElements();
      if (count == 0)
         return;

      String[][] strings = new String[count][4];
      Product product;

      while (++i < count)
      {
         strings[i][0] = (product = (Product)products.items[i]).name;
         strings[i][1] = product.code;
         strings[i][2] = Convert.toString(vQuant.items[i]);
         
         // The price is converted from cents to the right currency.
         strings[i][3] = Convert.toString((double)(product.unitPrice *  vQuant.items[i]) * 0.01, 2);
      }
      grid.setItems(strings);
   }

   /**
    * Calculates the total amount of the order based on the products selected and their quantities.
    *
    * @return The total amount in cents.
    */
   public int totalValue()
   {
      int total = 0;
      int i = products.size();
      
      while (--i >= 0)
         total += (((Product)products.items[i]).unitPrice * vQuant.items[i]);

      return total; // Result in cents.
   }

   /**
    * Cleans this menu, clearing the products and quantity vectors, rewinding the grid, reseting the date, selecting index 0 on both customer and 
    * product combo boxes, and reseting the current order number.
    */
   public void clean()
   {
      // Empties the vectors.
      products.removeAllElements();
      vQuant.removeAllElements();
      
      grid.removeAllElements(); // Clears the grid.
      spin.clear(); // Clears the spin list.
      
      // Resets the edits.
      SalesPlus.dateAux.setToday();
      edDate.setText(SalesPlus.dateAux.toString());
      edTot.setText("0.00");     

      // Sets the combo boxes to select index 0.
      cbCustomer.setSelectedIndex(0);
      cbProduct.setSelectedIndex(0);
   }

   /**
    * Gets the selected product.
    * 
    * @return The selected product.
    */
   private String[] getSelectedProduct()
   {
      int idx = grid.getSelectedIndex();
      if (idx < 0) 
         return null;
      return grid.getItem(idx);
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
         case ControlEvent.PRESSED: // A control was pressed.
           
            if (event.target == btnAdd && cbProduct.getSelectedIndex() >= 0) // Adds a product to the order.
            {
               int value = spin.getSelectedIndex() + 1; // Gets the selected quantity.
               String name = (String)cbProduct.getSelectedItem();
               boolean found = false;
               int n = products.size();
               
               while (--n >= 0) // Tries to find the product in the list and updates its quantity.
                  if (((Product)products.items[n]).name.equals(name))
                  {
                     vQuant.items[n] = value;
                     found = true;
                  }

               if (!found) // The product was not found, so it will be added to the list.
               {
                  products.addElement(ProductDB.getFromName(name));
                  vQuant.addElement(value);
               }

               // Updates the total value and the grid.
               edTot.setText(Convert.toString((double)totalValue() * 0.01, 2));
               fillGrid();
            }
            else if (event.target == btnClear) // Clears the screen.
               clean();
            else if (event.target == btnBack) // Goes back to the previous menu.
            {
               // Empties the vectors.
               products.removeAllElements();
               vDeleted.removeAllElements();
               vQuant.removeAllElements();
               
               clean(); // Clears the user interface.

               if (update)
                  MainWindow.getMainWindow().swap(SalesPlus.screens[SalesPlus.ORDER_SEARCH]);
               else
                  MainWindow.getMainWindow().swap(SalesPlus.screens[SalesPlus.ORDER_MENU]);
               
               update = false; // The next order won't be updated.
            }
            else if (event.target == btnRemove) // Removes a product from the order.
            {
               String[] strings = getSelectedProduct();
               int n = products.size();
               Product product;
              
               while (--n >= 0)
               {
                  if ((product = (Product)products.items[n]).name.equals(strings[0]))
                  {
                     vDeleted.addElement(product);
                     products.removeElementAt(n);
                     vQuant.removeElementAt(n);
                     break;
                  }
               }
               edTot.setText(Convert.toString((double)totalValue() * 0.01, 2));
               fillGrid();
            }
            else if (event.target == btnInsert) // Inserts an order. This actually creates an order.
            {
               int n = products.size();
               ItemOrder item;
               Product product;
               
               if (n == 0) // There can't be an order without products.
               {
                  new MessageBox("Error", "There are no products inserted.").popup();
                  return;
               }
               
               while (--n >= 0)
               {
                  (item = new ItemOrder()).productId = (product = (Product)products.items[n]).code;
                  item.unitPrice = product.unitPrice;
                  item.quant = vQuant.items[n];
                  item.orderId = currentOrder.orderId;
                  item.totalAmount = (item.quant * item.unitPrice);
                  ItemOrderDB.write(item, update);
               }

               if (update)
               {
                  n = vDeleted.size();
                  while (--n >= 0)
                     ItemOrderDB.remove(currentOrder.orderId, ((Product)vDeleted.items[n]).code);
               }

               String name = (String)cbCustomer.getSelectedItem();
               if (name.length() != 0)
               {
                  Customer customer = CustomerDB.getFromName(name);
                  if (customer == null) 
                     return;

                  currentOrder.customer = customer.name;
                  try
                  {
                     SalesPlus.dateAux.set(edDate.getText(), Settings.dateFormat);
                     currentOrder.date = SalesPlus.dateAux.getDateInt();
                  }
                  catch (InvalidDateException exception)
                  {
                     new MessageBox("Error", exception.getMessage()).popup();
                     return;
                  }
                  currentOrder.totalAmount = totalValue();

                  OrderDB.write(currentOrder, update);

                  if (update)
                     new MessageBox("Message", "Order updated").popup();
                  else
                     new MessageBox("Message", "Order inserted").popup();

                  currentOrder = new Order();
                  currentOrder.orderId = OrderDB.getLastOrderId();
                  edNum.setText(Convert.toString(currentOrder.orderId));

                  clean();
                  if (update)
                  {
                     update = false;
                     MainWindow.getMainWindow().swap(SalesPlus.screens[SalesPlus.ORDER_SEARCH]);
                  }
               }
            }

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
