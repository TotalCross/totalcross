// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package samples.apps.salesplus.ui.report;

import samples.apps.salesplus.ui.*;
import samples.apps.salesplus.*;

/**
 * The report menu.
 */
public class ReportMenu extends BaseMenu
{
   /**
    * The constructor.
    */
   public ReportMenu()
   {
      super(new String[]{"Summary", "By Product"}, new int[]{SalesPlus.SUMMARY_MENU, SalesPlus.BY_PRODUCT_MENU}, false);
   }
}