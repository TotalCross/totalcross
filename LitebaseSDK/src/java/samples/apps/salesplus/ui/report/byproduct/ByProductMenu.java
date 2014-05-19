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
