/*********************************************************************************
 *  TotalCross Software Development Kit - Litebase                               *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/



package samples.sys.tablecopier;

import totalcross.ui.*;
import totalcross.ui.event.*;
import totalcross.sys.*;

/**
 * The setup panel for the conduit.
 */
class SetupPanel extends Container
{
   private Check chDoSync;

   /**
    * Initializes the user interface.
    */
   public void initUI()
   {
      add(chDoSync = new Check("Synchronize the files"), LEFT, AFTER + 5);
      if (Settings.appSettings != null)
         chDoSync.setChecked("yes".equals(Settings.appSettings));
   }

   /**
    * Called to process posted events.
    * 
    * @param event The posted event.
    */
   public void onEvent(Event event)
   {
      if (event.type == ControlEvent.PRESSED && event.target == chDoSync)
         Settings.appSettings = chDoSync.isChecked()? "yes" : null;
   }
}
