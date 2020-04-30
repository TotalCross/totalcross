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

import litebase.PrimaryKeyViolationException;
import samples.apps.salesplus.*;
import samples.apps.salesplus.db.*;
import totalcross.ui.*;
import totalcross.ui.event.*;
import totalcross.ui.dialog.*;
import totalcross.util.BigDecimal;
import totalcross.sys.*;

/**
 * The new product menu.
 */
public class NewProduct extends Container
{
   /**
    * An auxiliary product.
    */
   public static Product sentProduct;
   
   /**
    * The insert / update button.
    */
   private Button btnInsert; 
   
   /**
    * The back button.
    */
   private Button btnBack; 
   
   /**
    * The clear button.
    */
   private Button btnClear;
   
   /**
    * The name edit.
    */
   private Edit edName; 
   
   /**
    * The code edit.
    */
   private Edit edCode; 
   
   /**
    * The description edit.
    */
   private Edit edDescr; 
   
   /**
    * The price edit.
    */
   private Edit edPrice;

   /**
    * Initializes the user interface.
    */
   public void initUI()
   {      
      Button.commonGap = 1;
      
      // Name label and edit.
      add(new Label("Name: "), LEFT + 5, TOP + 5);
      add(edName = new Edit(""), AFTER + 20, SAME, PREFERRED - 5, PREFERRED);
      
      int initX = edName.getRect().x;
      
      // Code label and edit.
      add(new Label("Code: "), LEFT + 5, AFTER + 2);
      add(edCode = new Edit(""), initX, SAME);

      // Product description.
      add(new Label("Descrip.: "), LEFT + 5, AFTER + 2);
      add(edDescr = new Edit(""), initX, SAME);

      // Price label and edit.
      add(new Label("Price: "), LEFT + 5, AFTER + 2);
      add(edPrice = new Edit(""), initX, SAME);
      edPrice.setMode(Edit.CURRENCY, true);
      
      // Buttons.
      add(btnInsert = new Button("Insert "), RIGHT - 1, BOTTOM - 1);
      add(btnBack = new Button("Back"), BEFORE - 2, SAME);
      add(btnClear = new Button("Clear"), BEFORE - 2, SAME);
      
      onAddAgain();
   }

   /**
    * Sets the new product menu to insert or update a product. 
    */
   public void onAddAgain()
   {
      if (sentProduct == null) // No selected product: a new one will be inserted.
      {
         clearEdit();
         btnInsert.setText("Insert");
      }
      else // Updates the selected product.
      {
         edName.setText(sentProduct.name);
         edCode.setText(sentProduct.code);
         edDescr.setText(sentProduct.descr);
         edPrice.setText(Convert.toString((double)sentProduct.unitPrice * 0.01, 2));
         edCode.setEditable(false);
         btnInsert.setText("Update");
      }
   }

   /**
    * Called to process posted events.
    * 
    * @param event The posted event.
    */
   public void onEvent(Event event)
   {
      if (event.type == ControlEvent.PRESSED)
      {      
         if (event.target == btnInsert) // The insert button.
         {
            Product product = new Product();
            product.name = edName.getText();
            product.code = edCode.getText();
            product.descr = edDescr.getText();

            // Checks if the price is valid.
            String string = edPrice.getTextWithoutMask();
            if (string.length() != 0)
               try
               {
                  product.unitPrice = Convert.toInt(new BigDecimal(string).movePointRight(2).toString());
               }
               catch (InvalidNumberException exception)
               {
                  new MessageBox("Error", exception.getMessage()).popup();
                  return;
               }
            else
               product.unitPrice = -1;

            StringBuffer sBuffer = SalesPlus.sBuffer;
            sBuffer.setLength(0);
            
            // Checks that all the fields have data.
            if (product.name.length() == 0)  
               sBuffer.append(" Product name|");
            if (product.code.length() == 0)  
               sBuffer.append(" Product code|");
            if (product.descr.length() == 0) 
               sBuffer.append(" Product description|");
            if (product.unitPrice == -1)     
               sBuffer.append(" Unit Price");

            if (sBuffer.length() > 0)
            {
               new MessageBox("Error","Please fill in the following fields:|" + sBuffer.toString()).popup();
               return;
            }
            
            try
            {
               if (ProductDB.write(product, sentProduct != null) == 0) // Inserts or updates product. 
               {
                  new MessageBox("Error", "Product name can't be repeated.").popup();
                  return;
               }               
            } 
            catch (PrimaryKeyViolationException exception)
            {
               new MessageBox("Error", "Product code already exists").popup();
               return;
            }

            if (sentProduct != null) // Updated the product.
            {
               sentProduct = null;
               new MessageBox("Message", "Product updated").popup();
               MainWindow.getMainWindow().swap(SalesPlus.screens[SalesPlus.PRODUCT_SEARCH]);
            }
            else // Inserted a new product.
               new MessageBox("Message", "Product inserted").popupNonBlocking();
            
            clearEdit();
         } 
         else if (event.target == btnClear) // Clear button: clears the edits.
            clearEdit();

         if (event.target == btnBack) // Back button.
         {
            if (sentProduct != null) // Goes to the search screen if a product was being updated.
            {
               sentProduct = null;
               MainWindow.getMainWindow().swap(SalesPlus.screens[SalesPlus.PRODUCT_SEARCH]);
            }
            else // Goes to the product menu if a product was being inserted.
               MainWindow.getMainWindow().swap(SalesPlus.screens[SalesPlus.PRODUCT_MENU]);
         }
      }
   }

   /**
    * Clears all the edits.
    */
   public void clearEdit()
   {
      edName.setText("");
      edCode.setText("");
      edDescr.setText("");
      edPrice.setText("");
      edCode.setEditable(true);
   }
}
