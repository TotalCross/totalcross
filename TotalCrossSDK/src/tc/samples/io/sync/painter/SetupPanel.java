/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
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

// $Id: SetupPanel.java,v 1.11 2011-01-04 13:19:19 guich Exp $

package tc.samples.io.sync.painter;

import totalcross.ui.*;
import totalcross.ui.event.*;

public class SetupPanel extends Container
{
   private Check chBringImage;

   public void initUI()
   {
      add(chBringImage = new Check("Bring image"),LEFT,AFTER+5);
      chBringImage.setChecked(totalcross.io.sync.Conduit.isSyncingEnabled());
   }

   public void onEvent(Event e)
   {
      switch (e.type)
      {
         case ControlEvent.PRESSED:
            if (e.target == chBringImage)
               totalcross.io.sync.Conduit.setSyncingEnabled(chBringImage.isChecked());
            break;
      }
   }
}
