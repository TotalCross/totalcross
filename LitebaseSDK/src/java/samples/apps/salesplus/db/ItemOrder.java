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
 * This is where an item of an order is specified: an item inside an order must have an associated order id (so that it is possible to know which 
 * item is related to which order), an id of the product (so that it is possible to know which product is this item), the quantity of items related
 * in that order, the price of one unit at the time that the order was done (products unit price can change), and the price of the total amount 
 * (quantity * unit price).
 */
public class ItemOrder
{
   /**
    * The order id.
    */
   public int orderId;
   
   /**
    * The product id.
    */
   public String productId;
   
   /**
    * The quantity of an item of an order.
    */
   public int quant;
   
   /**
    * The unit price of the product when included in the order multiplied by 100.
    */
   public int unitPrice;
   
   /**
    * The total amount of money spent buying the product in the order multiplied by 100.
    */
   public int totalAmount;
}
