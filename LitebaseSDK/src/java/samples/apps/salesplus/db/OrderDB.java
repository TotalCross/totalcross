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
import totalcross.sys.Settings;
import totalcross.util.*;
import litebase.*;

/**
 * Contains all database operations concerning an order.
 */
public class OrderDB
{
   /**
    * A prepared statement to insert data into the order table.
    */
   private static PreparedStatement psInsert;
   
   /**
    * A prepared statement to update data of the order table.
    */
   private static PreparedStatement psUpdate;
   
   /**
    * A prepared statement to select all orders issued in a given date.
    */
   private static PreparedStatement psSelectDate;
   
   /**
    * A prepared statement to select all orders issued in a given date interval.
    */
   private static PreparedStatement psSelectDays;
   
   /**
    * A prepared statement to delete an order.
    */
   private static PreparedStatement psDelete;
   
   /**
    * A prepared statement to select all orders of a given customer issued in a given date.
    */
   private static PreparedStatement psSelectNameDate;
   
   /**
    * A prepared statement to select all orders of a given customer.
    */
   private static PreparedStatement psSelectName;
   
   /**
    * A prepared statement to select an order given its id.
    */
   private static PreparedStatement psSelectId;

   static
   {
      LitebaseConnection driver = SalesPlus.driver;
      
      if (!driver.exists("orders"))
      {
         driver.execute("create table orders(customer char(20), intdate int, totalAmount int)");
         driver.execute("CREATE INDEX IDX ON orders(rowid)");
         driver.execute("CREATE INDEX IDX ON orders(intdate)");
      }
      else if (!driver.isTableProperlyClosed("orders"))
         driver.recoverTable("orders");
      
      psInsert = driver.prepareStatement("insert into orders values (?, ?, ?)");
      psUpdate = driver.prepareStatement("update orders set customer = ?, intdate = ?, totalAmount = ? where rowid = ?");
      psSelectDate = driver.prepareStatement("select rowid, customer, intdate, totalamount from orders where intdate = ?");
      psSelectDays = driver.prepareStatement("select rowid, customer, intdate, totalamount from orders where intdate >= ? and intdate <= ?");
      psDelete = driver.prepareStatement("delete orders where rowid = ?");
      psSelectNameDate = driver.prepareStatement("select rowid, customer, intdate, totalamount from orders where customer = ? and intdate = ?");
      psSelectName = driver.prepareStatement("select rowid, customer, intdate, totalamount from orders where customer = ?");      
      psSelectId = driver.prepareStatement("select rowid, customer, intdate, totalamount from orders where rowid = ?");
   }
   
   /**
    * Inserts or updates an order.
    *
    * @param order The item related to the order.
    * @param update Indicates if the order is being updated or inserted.
    */
   public static void write(Order order, boolean update)
   {
      PreparedStatement prepStmt;
      
      if (update)
      {
         prepStmt = psUpdate;
         prepStmt.setInt(3, order.orderId);
      }
      else
         prepStmt = psInsert;
      
      prepStmt.setString(0, order.customer);
      prepStmt.setInt(1, order.date);
      prepStmt.setInt(2, order.totalAmount);
      prepStmt.executeUpdate();       
   }

   /**
    * Gets all the orders issued in a given date.
    * 
    * @param date The desired date.
    * @return An array with all orders issued in the given date.
    */
   public static Order[] getFromDate(int date)
   {
      psSelectDate.setInt(0, date);
      
      ResultSet resultSet = psSelectDate.executeQuery();
      Order[] orders = new Order[resultSet.getRowCount()];
      int i = 0;
      Order order;
      
      while (resultSet.next())
      {
         (order = orders[i++] = new Order()).orderId = resultSet.getInt(1);
         order.customer = resultSet.getString(2);
         order.date = date;
         order.totalAmount = resultSet.getInt(4);
      }
      resultSet.close();
      
      return orders;
   }

   /**
    * Fetches all order issued in all days of a given month and year.
    * 
    * @param year The desired year.
    * @param month The desired month.
    * @return All orders issued in the given date interval.
    */
   public static Order[] readAllDays(int year, int month)
   {      
      psSelectDays.setInt(0, year = (year * 10000 + month * 100 + 1));
      psSelectDays.setInt(1, year + 30);
      
      ResultSet resultSet = psSelectDays.executeQuery();
      Order[] orders = new Order[resultSet.getRowCount()];
      Order order;
      int i = 0;
      
      while (resultSet.next())
      {
         (order = orders[i++] = new Order()).orderId = resultSet.getInt(1);
         order.customer = resultSet.getString(2);
         order.date = resultSet.getInt(3);
         order.totalAmount = resultSet.getInt(4);
      }
      
      return orders;
   }

   /**
    * Removes an order order table. First all the items must be removed and just then the order itself can be removed (consistency).
    *
    * @param order The order to be removed.
    */
   public static void removeOrder(Order order)
   {
      psDelete.setInt(0, order.orderId);
      psDelete.executeUpdate();      
   }

   /**
    * Search for an order with specific name and date conditions.
    *
    * @param name The name of the customer related to the order
    * @param date The date in which the order was issued
    * @return An array with all orders of the given customer issued in the given date.
    */
   public static Order[] search(String name, String date) throws InvalidDateException
   {
      SalesPlus.dateAux.set(date, Settings.dateFormat);
      
      int dateInt = SalesPlus.dateAux.getDateInt();
      
      psSelectNameDate.setString(0, name);
      psSelectNameDate.setInt(1, dateInt);
      
      ResultSet resultSet = psSelectNameDate.executeQuery();
      Order[] orders = new Order[resultSet.getRowCount()];
      Order order;
      int i = 0;
      
      while (resultSet.next())
      {
         (order = orders[i++] = new Order()).orderId = resultSet.getInt(1);
         order.customer = name;
         order.date = dateInt;
         order.totalAmount = resultSet.getInt(4);
      }
      resultSet.close();
      
      return orders;
   }

   /**
    * Checks if an specific customer has an order..
    *
    * @param customer The customer being searched.
    * @return <code>true</code> if the customer has issued an order; <code>false</code>, otherwise.
    */
   public static boolean hasOrder(Customer customer) 
   {
      psSelectName.setString(0, customer.name);
      
      ResultSet resultSet = psSelectName.executeQuery();
      boolean ret = resultSet.first();
      
      resultSet.close();
      return ret;
   }

   /**
    * Returns the order associated with its unique order id.
    *
    * @param id Unique order id.
    * @return The order if found or <code>null</code>, otherwise.
    */
   public static Order getFromId(int id) 
   {
      psSelectId.setInt(0, id);
      
      ResultSet resultSet = psSelectId.executeQuery();
      Order order = null;
      
      if (resultSet.first())
      {
         (order = new Order()).orderId = id;
         order.customer = resultSet.getString(2);
         order.date = resultSet.getInt(3);
         order.totalAmount = resultSet.getInt(4);
      }
      resultSet.close();
      
      return order;
   }

   /**
    * Retrieves the order id of the last order to calculate a new order id.
    */
   public static int getLastOrderId()
   {
      return SalesPlus.driver.getCurrentRowId("orders");
   }
}
