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

package samples.apps.salesplus.db;

import samples.apps.salesplus.SalesPlus;
import litebase.*;

/**
 * Contains all database operations concerning an order item.
 */
public class ItemOrderDB
{
   /**
    * A prepared statement to insert data into the order item table.
    */
   private static PreparedStatement psInsert;
   
   /**
    * A prepared statement to update data of the order item table.
    */
   private static PreparedStatement psUpdate;
   
   /**
    * A prepared statement to delete data from the order item table.
    */
   private static PreparedStatement psDelete;
   
   /**
    * A prepared statement to delete all items from an order.
    */
   private static PreparedStatement psDeleteAll;
   
   /**
    * A prepared statement to select all items of an order.
    */
   private static PreparedStatement psSelectItems;
   
   /**
    * A prepared statement to check if there is a product in an order item.
    */
   private static PreparedStatement psSelectProduct;
   
   static
   {
      LitebaseConnection driver = SalesPlus.driver;
      
      if (!driver.exists("itemorder"))
      {
         driver.execute("create table itemorder(orderid int, productid char(20), quant int, unitPrice int, totalAmount int)");
         driver.execute("CREATE INDEX IDX ON itemorder(orderid)");
         driver.execute("CREATE INDEX IDX ON itemorder(productid)");
      }
      else if (!driver.isTableProperlyClosed("itemorder"))
         driver.recoverTable("itemorder");
      
      psInsert = driver.prepareStatement("insert into itemorder values (?, ?, ?, ?, ?)");
      psUpdate = driver.prepareStatement("update itemorder set orderid = ?, productid = ?, quant = ?, unitPrice = ?, totalAmount = ? " 
                                       + "where productid = ?");
      psDelete = driver.prepareStatement("delete itemorder where productid = ? and orderid = ?");
      psDeleteAll = driver.prepareStatement("delete itemorder where orderid = ?");
      psSelectItems = driver.prepareStatement("select * from itemorder where orderid = ?");
      psSelectProduct = driver.prepareStatement("select * from itemorder where productid = ?");
   }

   /**
    * Inserts or updates an item of an order in the table.
    *
    * @param item The item related to the order.
    * @param update Indicates if the item is being updated or inserted.
    */
   public static void write(ItemOrder item, boolean update)
   {
      PreparedStatement prepStmt;
      
      if (update)
      {
         prepStmt = psUpdate;
         prepStmt.setString(5, item.productId);
      }
      else
         prepStmt = psInsert;
      
      prepStmt.setInt(0, item.orderId);
      prepStmt.setString(1, item.productId);
      prepStmt.setInt(2, item.quant);
      prepStmt.setInt(3, item.unitPrice);
      prepStmt.setInt(4, item.totalAmount);
      prepStmt.executeUpdate();
   }

   /**
    * Searches for a specific order/product and removes it.
    *
    * @param orderId The order id;
    * @param prodCode The product code.
    */
   public static void remove(int orderId, String prodCode)
   {
      psDelete.setString(0, prodCode);
      psDelete.setInt(1, orderId);
      psDelete.executeUpdate();     
   }
   
   /**
    * Searches for a specific order id and removes all items containing it.
    *
    * @param orderId The order id;
    */
   public static void removeAll(int orderId)
   {
      psDeleteAll.setInt(0, orderId);
      psDeleteAll.executeUpdate();     
   }

   /**
    * Read all order items related to a specific order id.
    *
    * @param orderId The order id.
    * @return An array containing all items related to that order.
    */
   public static ItemOrder[] readAll(int orderId)
   {
      psSelectItems.setInt(0, orderId);
      
      ResultSet resultSet = psSelectItems.executeQuery();
      ItemOrder[] items = new ItemOrder[resultSet.getRowCount()];
      int i = 0;
      ItemOrder item;
      
      while (resultSet.next())
      {
         (item = items[i++] = new ItemOrder()).orderId = resultSet.getInt(1);
         item.productId = resultSet.getString(2);
         item.quant = resultSet.getInt(3);
         item.unitPrice = resultSet.getInt(4);
         item.totalAmount = resultSet.getInt(5);
      }
      resultSet.close();
      
      return items;
   }


   /**
    * Checks if there is an order whose list has a specific product as its related item. This is
    * useful when it is desired to delete a product but it is already related to an order (consistency).
    *
    * @param product The product.
    * @return <code>true</code> if there is at least one item that has the product or <code>false</code>, otherwise.
    */
   public static boolean hasProduct(Product product)
   {
      psSelectProduct.setString(0, product.code);
      ResultSet resultSet = psSelectProduct.executeQuery();
      boolean ret = resultSet.first();
      
      resultSet.close();
      return ret;
   }

}