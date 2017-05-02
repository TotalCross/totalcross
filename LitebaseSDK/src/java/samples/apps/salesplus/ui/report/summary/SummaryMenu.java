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
