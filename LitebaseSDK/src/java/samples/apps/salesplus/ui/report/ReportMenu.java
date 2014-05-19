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