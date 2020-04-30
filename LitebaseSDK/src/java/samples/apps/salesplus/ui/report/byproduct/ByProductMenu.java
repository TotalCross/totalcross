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

package samples.apps.salesplus.ui.report.byproduct;

import samples.apps.salesplus.*;
import samples.apps.salesplus.ui.*;

/**
 * The report menu by product.
 */
public class ByProductMenu extends BaseMenu
{
   /**
    * The constructor.
    */
   public ByProductMenu()
   {
      super(new String[]{"Month", "Day", "Period"}, new int[]{SalesPlus.MONTH2, SalesPlus.DAY2, SalesPlus.PERIOD}, false);
   }
}
