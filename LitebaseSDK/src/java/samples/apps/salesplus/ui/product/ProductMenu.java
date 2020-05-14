// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

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
