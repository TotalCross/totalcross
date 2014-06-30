/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2014 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  This file is covered by the GNU LESSER GENERAL PUBLIC LICENSE VERSION 3.0    *
 *  A copy of this license is located in file license.txt at the root of this    *
 *  SDK or can be downloaded here:                                               *
 *  http://www.gnu.org/licenses/lgpl-3.0.txt                                     *
 *                                                                               *
 *********************************************************************************/

package tc.samples.api.ui;

import tc.samples.api.*;

import totalcross.res.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.image.*;

public class TopMenuSample extends BaseContainer
{
   ScrollContainer sc;
   
   public void initUI()
   {
      super.initUI();
      try
      {
         super.initUI();
         setInfo("Click outside to close the menu");
         String [] tits =
         {
            "Videocalls",
            "Insert emoticon",
            "Add text",
            "See contact",
            "Add slide",
            "Add subject",
            "Add persons",
            "Programmed messages",
            "Add to the phone book",
         };
         Image [] icons =
            {
               Resources.warning,
               Resources.exit,
               Resources.back,
               Resources.menu,
               Resources.warning,
               Resources.exit,
               Resources.back,
               Resources.menu,
               Resources.warning,
            };
         new TopMenu(tits,icons,CENTER).popup();
         new TopMenu(tits,icons,BOTTOM).popup();
         new TopMenu(tits,icons,TOP).popup();
         new TopMenu(tits,icons,LEFT).popup();
         new TopMenu(tits,icons,RIGHT).popup();
         back();
      }
      catch (Exception e)
      {
         MessageBox.showException(e,true);
         back();
      }
   }
}