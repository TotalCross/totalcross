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



package samples.sys.migration;

import totalcross.ui.gfx.Color;
import totalcross.sys.*;
import totalcross.ui.*;

/** 
 * This application converts the Litebase tables from a previous to the current version.
 */
public class Migration extends MainWindow
{
   static
   {
      Settings.useNewFont = true;
   }

   /**
    * The <code>Migration</code> object.
    */
   static Migration mig;
   
   /**
    * The main window.
    */
   MainUI mainUI;
   
   /**
    * The list of tables.
    */
   ListDBs listDBs;

   static
   {
      Settings.useNewFont = true;
   }
   
   /**
    * The constructor.
    */
   public Migration()
   {
      super("Litebase Migration", TAB_ONLY_BORDER);
      Vm.debug(Vm.ERASE_DEBUG);
      setUIStyle(Settings.Vista);
      mig = this;
      mainUI = new MainUI();
      listDBs = new ListDBs();
      UIColors.controlsBack = Color.getRGB(0, 255, 255);
   }
   
   /**
    * Initializes the user interface.
    */
   public void initUI()
   {
      if (Settings.appSettings == null)
         Settings.appSettings = Settings.platform.equals(Settings.PALMOS)? "/Litebase_DBs" : Settings.appPath;
      swap(mainUI);
   }
}
