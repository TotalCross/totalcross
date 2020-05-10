// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package samples.apps.salesplus.ui.order;

import samples.apps.salesplus.ui.*;
import samples.apps.salesplus.*;

/**
 * The order menu.
 */
public class OrderMenu extends BaseMenu
{
   /**
    * The Constructor.
    */
   public OrderMenu()
   {
      super(new String[]{"New Order", "Search"}, new int[]{SalesPlus.NEW_ORDER, SalesPlus.ORDER_SEARCH}, false);
   }
}
