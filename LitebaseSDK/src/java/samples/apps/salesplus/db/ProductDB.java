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
 * Contains all database operations concerning a product.
 */
public class ProductDB
{
   /**
    * A prepared statement to insert data into the product table.
    */
   private static PreparedStatement psInsert;
   
   /**
    * A prepared statement to update data of the product table.
    */
   private static PreparedStatement psUpdate;
   
   /**
    * A prepared statement to select all product names.
    */
   private static PreparedStatement psSelectNames;
   
   /**
    * A prepared statement to delete an product.
    */
   private static PreparedStatement psDelete;
   
   /**
    * A prepared statement to select a product given its code.
    */
   private static PreparedStatement psSelectCode;
   
   /**
    * A prepared statement to select a product given its name.
    */
   private static PreparedStatement psSelectName;
   
   /**
    * A prepared statement to select a product given its name and code.
    */
   private static PreparedStatement psSelectCodeName;
   
   static
   {
      LitebaseConnection driver = SalesPlus.driver;
      
      if (!driver.exists("product"))
      {
         driver.execute("create table product(name char(30), code char(10) primary key, descr char(50), unitprice int)");
         driver.execute("CREATE INDEX IDX ON product(NAME)");
      }
      else if (!driver.isTableProperlyClosed("product"))
         driver.recoverTable("product");
      
      psInsert = driver.prepareStatement("insert into product values (?, ?, ?, ?)");
      psUpdate = driver.prepareStatement("update product set name = ?, code = ?, descr = ?, unitprice = ? where code = ?");
      psSelectNames = driver.prepareStatement("select name from product");
      psDelete = driver.prepareStatement("delete product where code = ?");
      psSelectCode = driver.prepareStatement("select * from product where code = ?");
      psSelectName = driver.prepareStatement("select * from product where name = ?");
      psSelectCodeName = driver.prepareStatement("select * from product where name = ? and code = ?");
   }

   /**
    * Inserts or updates a product.
    *
    * @param product The product to be inserted or updated.
    * @param update Indicates if the product is being updated or inserted.
    * @return <code>1</code> if the record was updated or inserted; <code>0</code> if no record was updated.
    */
   public static int write(Product product, boolean update)
   {
      PreparedStatement prepStmt;
      
      if (getFromName(product.name) != null) // There can't be repeated names.
         return 0;
      
      if (update)
      {
         prepStmt = psUpdate;
         prepStmt.setString(4, product.code);
      }
      else
         prepStmt = psInsert;
      
      prepStmt.setString(0, product.name);
      prepStmt.setString(1, product.code);
      prepStmt.setString(2, product.descr);
      prepStmt.setInt(3, product.unitPrice);
      return prepStmt.executeUpdate();
   }

   /**
    * Reads the names of all products of the product table.
    *
    * @return An array holding all product names available.
    */
   public static String[] readAllNames()
   {
      ResultSet resultSet = psSelectNames.executeQuery();
      String[] strings = new String[resultSet.getRowCount()];
      int i = 0;
      
      while(resultSet.next())
         strings[i++] = resultSet.getString(1);
      resultSet.close();
      
      return strings;
   }

   /**
    * Remove a specific product from the table.
    *
    * @param product The product to be removed
    */
   public static void remove(Product product)
   {
      psDelete.setString(0, product.code);
      psDelete.executeUpdate();
   }

   /**
    * Retrieves a product information based on the product code.
    *
    * @param prodCode The product code.
    * @return The product if found or <code>null</code> if none is found.
    */
   public static Product getFromCode(String prodCode) 
   {
      psSelectCode.setString(0, prodCode);
      
      ResultSet resultSet = psSelectCode.executeQuery();
      Product product = null;
      
      if (resultSet.first())
      {
         (product = new Product()).name = resultSet.getString(1);
         product.code = prodCode;
         product.descr = resultSet.getString(3);
         product.unitPrice = resultSet.getInt(4);
      }
      resultSet.close();
      
      return product;
   }

   /**
    * Retrieves a product information based on the product name.
    *
    * @param prodName The product name.
    * @return The product if found or <code>null</code> if none is found.
    */
   public static Product getFromName(String prodName) // infoName
   {
      psSelectName.setString(0, prodName);
      
      ResultSet resultSet = psSelectName.executeQuery();
      Product product = null;
      
      if (resultSet.first())
      {
         (product = new Product()).name = prodName;
         product.code = resultSet.getString(2);
         product.descr = resultSet.getString(3);
         product.unitPrice = resultSet.getInt(4);
      }
      resultSet.close();
      
      return product;
   }

   /**
    * Search for a product matching the searching fields.
    *
    * @param prodName The name of the product.
    * @param prodCode The code of the product.
    * @return An array of products.
    */
   public static Product[] search(String prodName, String prodCode)
   {      
      psSelectCodeName.setString(0, prodName);
      psSelectCodeName.setString(1, prodCode);
      
      ResultSet resultSet = psSelectCodeName.executeQuery();
      Product[] products = new Product[resultSet.getRowCount()];
      Product product;
      int i = 0;
      
      while (resultSet.next())
      {
         (product = products[i++] = new Product()).name = prodName;
         product.code = prodCode;
         product.descr = resultSet.getString(3);
         product.unitPrice = resultSet.getInt(4);
      }
      resultSet.close();
      
      return products;
   }
}