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

/** 
 * This where an order is specified: an order must have a unique identification number, it also needs a customer which is related to the order 
 * (each order has its own related customer and only one), the date in which the order was issued, and the total price of the order (each 
 * items quantity * unit price altogether).
 */
public class Order
{
   /**
    * The order id.
    */
   public int orderId;
   
   /**
    * The order customer.
    */
   public String customer;
   
   /**
    * The order date.
    */
   public int date;
   
   /**
    * The order total amount (each items quantity * unit price altogether). 
    */
   public int totalAmount;
}