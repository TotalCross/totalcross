/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  Copyright (C) 2012-2020 TotalCross Global Mobile Platform Ltda.   
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  This file is covered by the GNU LESSER GENERAL PUBLIC LICENSE VERSION 2.1    *
 *  A copy of this license is located in file license.txt at the root of this    *
 *  SDK or can be downloaded here:                                               *
 *  http://www.gnu.org/licenses/lgpl-2.1.txt                                     *
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
