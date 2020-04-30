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

package samples.apps.salesplus.ui.product;

import samples.apps.salesplus.db.*;
import samples.apps.salesplus.*;
import totalcross.ui.*;
import totalcross.ui.event.*;
import totalcross.ui.dialog.*;
import totalcross.ui.gfx.*;
import totalcross.sys.*;

/**
 * The product search menu.
 */
public class ProductSearch extends Container
{
   /**
    * The back button.
    */
   private Button btnBack; 
   
   /**
    * The edit button.
    */
   private Button btnEdit; 
   
   /**
    * The remove button.
    */
   private Button btnRemove;
   
   /**
    * The search button.
    */
   private Button btnSearch;
   
   /**
    * The grid.
    */
   private Grid grid;
   
   /**
    * The name edit.
    */
   private Edit edName; 
   
   /**
    * The code edit.
    */
   private Edit edCode;

   /**
    * Initializes the user interface.
    */
   public void initUI()
   {
      Button.commonGap = 1;
      
      // Name label and edit.
      add(new Label("Name: "), LEFT + 5, TOP + 2);
      add(edName = new Edit(""), AFTER + 5, SAME, FILL - 5, PREFERRED);

      // Code label and edit.
      add(new Label("Code: "), LEFT + 5, AFTER + 2);
      add(edCode = new Edit(""), edName.getRect().x, SAME, FILL - 5, PREFERRED);

      // The buttons.
      add(btnSearch = new Button("Search"), RIGHT - 5, AFTER + 2);
      add(btnEdit = new Button("View"), RIGHT - 1, BOTTOM - 1);
      add(btnBack = new Button("Back"), BEFORE - 2, SAME);
      add(btnRemove = new Button("Remove"), BEFORE - 2, SAME);

      // The grid.
      add(grid = new Grid(new String[]{"Name", "Code", "Descr", "Price"}, new int[]{fm.stringWidth("xxxxxxxxxxxxxxxx"), fm.stringWidth("x"), fm.stringWidth("xxxxxxxxxxxxxxxxx"), fm.stringWidth("x")}, new int[]{LEFT, LEFT, LEFT, RIGHT}, false));
      grid.verticalLineStyle = Grid.VERT_DOT;
      grid.setBackColor(Color.WHITE);
      grid.setRect(LEFT + 5, AFTER + 5, FILL - 5, FIT, btnSearch);
   }

   /**
    * Updates the grid searching the product again.
    */
   public void onAddAgain()
   {
      String nameStr = edName.getText();
      String codeStr = edCode.getText();
      Product[] products = ProductDB.search(nameStr, codeStr);
      int count = products.length;
      
      if (count == 0) // No elements found: erases the grid.
         grid.removeAllElements();
      else // Loads the grid.
      {
         String[][] strings = new String[count][4];
         int i = -1;

         while (++i < count)
         {
            strings[i][0] = nameStr;
            strings[i][1] = codeStr;
            strings[i][2] = products[i].descr;
            strings[i][3] = Convert.toString((double)products[i].unitPrice * 0.01, 2);
         }
         grid.setItems(strings);
      }
   }

   /**
    * Returns the selected product in the grid.
    * 
    * @return The selected product or <code>null</code> if none was selected.
    */
   private Product getSelectedProduct()
   {
      int idx = grid.getSelectedIndex();
      if (idx < 0) 
         return null;
      return ProductDB.getFromCode(grid.getItem(idx)[1]);
   }

   /**
    * Called to the posted events.
    *
    * @param event The posted event.
    */
   public void onEvent(Event event)
   {
      if (event.type == ControlEvent.PRESSED)
      {
         if (event.target == btnSearch) // Search button: reloads the grid.
            onAddAgain();
         else if (event.target == btnBack) // Back button: goes back to the product menu.
            MainWindow.getMainWindow().swap(SalesPlus.screens[SalesPlus.PRODUCT_MENU]);
         else if (event.target == btnEdit) // Edit button: goes to the new product menu to update the selected product.
         {
            Product product = getSelectedProduct();
            if (product != null)
            {
               NewProduct.sentProduct = product;
               MainWindow.getMainWindow().swap(SalesPlus.screens[SalesPlus.NEW_PRODUCT]);
            }
         }
         else if (event.target == btnRemove) // Remove button: removes the selected product.
         {
            Product product = getSelectedProduct();
            if (product != null) // must always be true
            {
               if (!ItemOrderDB.hasProduct(product))
               {
                  ProductDB.remove(product);
                  onAddAgain();
               }
               else
                  new MessageBox("Error", "There are orders that belong to | this product. If you really want " 
                               + "| to remove it, delete the orders first.").popup();
            }
         }

      }
   }
}