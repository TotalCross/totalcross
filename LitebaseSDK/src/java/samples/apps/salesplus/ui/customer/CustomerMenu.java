// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package samples.apps.salesplus.ui.customer;

import samples.apps.salesplus.*;
import samples.apps.salesplus.ui.*;

/**
 * The customer menu.
 */
public class CustomerMenu extends BaseMenu
{
   /**
    * The constructor.
    */
   public CustomerMenu()
   {
      super(new String[]{"New Customer", "Search"},
             new int[]{SalesPlus.NEW_CUSTOMER, SalesPlus.CUSTOMER_SEARCH}, false);
   }
}
