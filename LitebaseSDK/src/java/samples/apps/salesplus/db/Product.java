// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package samples.apps.salesplus.db;

/** 
 * This where a product is specified: a product must have a name and an identification code,
 * a short description of what the product is and its current price per unit.
 */
public class Product
{
   /**
    * The product name.
    */
   public String name;
   
   /**
    * The product code.
    */
   public String code;
   
   /**
    * The product description.
    */
   public String descr;
   
   /**
    * The product unit price.
    */
   public int unitPrice;
}
