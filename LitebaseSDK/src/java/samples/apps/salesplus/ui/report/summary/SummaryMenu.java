// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package samples.apps.salesplus.ui.report.summary;

import samples.apps.salesplus.ui.*;
import samples.apps.salesplus.*;

/**
 * The report summary menu.
 */
public class SummaryMenu extends BaseMenu
{
   /**
    * The constructor.
    */
   public SummaryMenu()
   {
      super(new String[]{"Day", "Month"}, new int[]{SalesPlus.DAY, SalesPlus.MONTH}, false);
   }

}
