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
