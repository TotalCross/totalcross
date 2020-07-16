// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package samples.apps.salesplus.ui;

import samples.apps.salesplus.SalesPlus;

/**
 * The main menu, the one that is shown when the program is openned.
 */
public class MainMenu extends BaseMenu
{
   /**
    * The constructor
    */
   public MainMenu()
   {
      super(new String[]{ "Customers", "Orders", "Products", "Reports"},
            new int[]{SalesPlus.CUSTOMER_MENU, SalesPlus.ORDER_MENU, SalesPlus.PRODUCT_MENU,SalesPlus.REPORT_MENU}, true);
   }
}
