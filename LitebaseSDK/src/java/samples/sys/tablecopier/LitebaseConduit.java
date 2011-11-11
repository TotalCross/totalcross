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

import totalcross.io.sync.Conduit;
import totalcross.sys.*;

/**
 * Program used to copy Litebase files to and from a Palm OS device.
 */
public class LitebaseConduit extends Conduit
{
   static
   {
      totalcross.sys.Settings.applicationId = "LBcn";
      Settings.useNewFont = true;
   }

   /**
    * The constructor.
    */
   public LitebaseConduit()
   {
      super("LitebaseConduit", "LBcn", "/Litebase_DBs/", TAB_ONLY_BORDER);
   }
   
   /**
    * Configures the conduit.
    */
   protected void doConfig()
   {
      setTitle("Litebase Conduit - Configuration");
      swap(new SetupPanel());
   }
   
   /**
    * Does a synchronization with the device.
    */
   protected void doSync()
   {
      setTitle("Litebase Conduit - Synchronization");
      SyncPanel sp = new SyncPanel();
      sp.targetAppPath = targetAppPath;
      swap(sp);
   }
   
   /**
    * Operation done when registering the conduit.
    */
   protected void onRegister()
   {
      Settings.appSettings = "yes";
      setConduitRect(-2, -2, -1, 80, true);
      setConduitRect(-2, -2, 360, 240, false);
   }
}
