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
