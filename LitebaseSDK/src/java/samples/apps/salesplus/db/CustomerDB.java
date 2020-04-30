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
 * Contains all database operations concerning a customer.
 */
public class CustomerDB
{
   /**
    * A prepared statement to insert data into the customer table.
    */
   private static PreparedStatement psInsert;
   
   /**
    * A prepared statement to update data of the customer table.
    */
   private static PreparedStatement psUpdate;
   
   /**
    * A prepared statement to delete data from the customer table.
    */
   private static PreparedStatement psDelete;
   
   /**
    * A prepared statement to select all customers.
    */
   private static PreparedStatement psSelectAll;
   
   /**
    * A prepared statement to select a customer given the name.
    */
   private static PreparedStatement psSelectName;
   
   static
   {
      LitebaseConnection driver = SalesPlus.driver;
      
      if (!driver.exists("customer"))
      {
         driver.execute("create table customer(name char(30), fedid char (15) primary key, address char (30), city char(15), state char(10), " 
                      + "country char(15), zip char(10), tel1 char(15), tel2 char(15), fax char(15), cname char(30), role char(20), email char(20), " 
                      + "ctel char(15), cel char(15))");
         driver.execute("CREATE INDEX IDX ON customer(name)");
      }
      else if (!driver.isTableProperlyClosed("customer"))
         driver.recoverTable("customer");
      
      psInsert = driver.prepareStatement("insert into customer values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
      psUpdate = driver.prepareStatement("update customer set name = ?, fedid = ?, address = ?, city = ?, state = ?, country = ?, zip = ?, " 
                                       + "tel1 = ?, tel2 = ?, fax = ?, cname = ?, role = ?, email = ?, ctel = ?, cel = ? where fedid = ?");
      psDelete = driver.prepareStatement("delete customer where fedid = ?");
      psSelectAll = driver.prepareStatement("select name from customer");
      psSelectName = driver.prepareStatement("select * from customer where name = ?");
   }

   /**
    * Inserts or updates a customer in the table.
    *
    * @param customer The customer to be inserted or updated.
    * @param update Indicates if the customer is being updated or inserted.
    * @return <code>1</code> if the record was updated or inserted; <code>0</code> if no record was updated.
    */
   public static int write(Customer customer, boolean update)
   {
      PreparedStatement prepStmt;
      
      if (getFromName(customer.name) != null) // There can't be repeated names.
         return 0;
      
      if (update)
      {
         prepStmt = psUpdate;
         prepStmt.setString(15, customer.fedId);
      }
      else
         prepStmt = psInsert;

      prepStmt.setString(0, customer.name);
      prepStmt.setString(1, customer.fedId);
      prepStmt.setString(2, customer.address);
      prepStmt.setString(3, customer.city);
      prepStmt.setString(4, customer.state);
      prepStmt.setString(5, customer.country);
      prepStmt.setString(6, customer.zip);
      prepStmt.setString(7, customer.tel1);
      prepStmt.setString(8, customer.tel2);
      prepStmt.setString(9, customer.fax);
      prepStmt.setString(10, customer.cName);
      prepStmt.setString(11, customer.role);
      prepStmt.setString(12, customer.email);     
      prepStmt.setString(13, customer.cTel);
      prepStmt.setString(14, customer.cel);     
      
      return prepStmt.executeUpdate();
   }


   /**
    * Removes a customer from the customer table
    *
    * @param customer The customer to be removed.
    */
   public static void remove(Customer customer)
   {
      psDelete.setString(0, customer.fedId);
      psDelete.executeUpdate();
   }

   /**
    * Returns all the customer names.
    * 
    * @return An array with all the customer names.
    */
   public static String[] readAllNames()
   {
      ResultSet resultSet = psSelectAll.executeQuery();
      String[] strings = new String[resultSet.getRowCount()];
      int i = 0;
      
      while(resultSet.next())
         strings[i++] = resultSet.getString(1);
      resultSet.close();
      return strings;
   }


   /**
    * Returns a specific customer
    *
    * @param name The name of the customer.
    * @return The customer or <code>null</code> if the customer was not found.
    */
   public static Customer getFromName(String name) 
   {
      psSelectName.setString(0, name);
      
      ResultSet resultSet = psSelectName.executeQuery();     
      
      if (resultSet.first())
      {
         Customer customer = new Customer();
         customer.name = resultSet.getString(1);
         customer.fedId = resultSet.getString(2);
         customer.address = resultSet.getString(3);
         customer.city = resultSet.getString(4);
         customer.state = resultSet.getString(5);
         customer.country = resultSet.getString(6);
         customer.zip = resultSet.getString(7);
         customer.tel1 = resultSet.getString(8);
         customer.tel2 = resultSet.getString(9);
         customer.fax = resultSet.getString(10);
         customer.cName = resultSet.getString(11);
         customer.role = resultSet.getString(12);
         customer.email = resultSet.getString(13);
         customer.cTel = resultSet.getString(14);
         customer.cel = resultSet.getString(15);
         return customer;
      }
      resultSet.close();
      
      return null;
   }

   /**
    * Searches for a customer matching the specific search fields.
    *
    * @param name The name of the customer.
    * @return An array containing the result of the search.
    */
   public static Customer[] searchCustomers(String name)
   {
      psSelectName.setString(0, name);
      ResultSet resultSet = psSelectName.executeQuery();
      Customer[] customers = new Customer[resultSet.getRowCount()];
      int i = 0;
      Customer customer;
      
      while (resultSet.next())
      {
         (customer = customers[i++] = new Customer()).name = resultSet.getString(1);
         customer.fedId = resultSet.getString(2);
         customer.address = resultSet.getString(3);
         customer.city = resultSet.getString(4);
         customer.state = resultSet.getString(5);
         customer.country = resultSet.getString(6);
         customer.zip = resultSet.getString(7);
         customer.tel1 = resultSet.getString(8);
         customer.tel2 = resultSet.getString(9);
         customer.fax = resultSet.getString(10);
         customer.cName = resultSet.getString(11);
         customer.role = resultSet.getString(12);
         customer.email = resultSet.getString(13);
         customer.cTel = resultSet.getString(14);
         customer.cel = resultSet.getString(15);
      }
      resultSet.close();
      
      return customers;
   }
}
