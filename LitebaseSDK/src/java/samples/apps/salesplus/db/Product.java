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
