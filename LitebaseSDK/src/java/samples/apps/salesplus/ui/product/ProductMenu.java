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

package samples.apps.salesplus.ui.product;

import samples.apps.salesplus.ui.*;
import samples.apps.salesplus.*;

public class ProductMenu extends BaseMenu
{
   /**
    * The Constructor.
    */
   public ProductMenu()
   {
      super(new String[]{"New Product", "Search"}, new int[]{SalesPlus.NEW_PRODUCT, SalesPlus.PRODUCT_SEARCH}, false);
   }
}
