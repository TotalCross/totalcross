/*********************************************************************************
 *  TotalCross Software Development Kit - Litebase                               *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

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