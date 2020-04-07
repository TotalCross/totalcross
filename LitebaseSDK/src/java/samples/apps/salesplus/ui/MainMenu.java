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

package samples.apps.salesplus.ui;

import samples.apps.salesplus.SalesPlus;

/**
 * The main menu, the one that is shown when the program is openned.
 */
public class MainMenu extends BaseMenu
{
   /**
    * The constructor
    */
   public MainMenu()
   {
      super(new String[]{ "Customers", "Orders", "Products", "Reports"},
            new int[]{SalesPlus.CUSTOMER_MENU, SalesPlus.ORDER_MENU, SalesPlus.PRODUCT_MENU,SalesPlus.REPORT_MENU}, true);
   }
}
