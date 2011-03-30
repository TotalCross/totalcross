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

// $Id: PainterConduit.java,v 1.8 2011-01-04 13:19:19 guich Exp $

package tc.samples.io.sync.painter;

import totalcross.io.sync.Conduit;

public class PainterConduit extends Conduit
{
   public PainterConduit()
   {
      super("PainterConduit", "PAin", "TotalCross/Painter", TAB_ONLY_BORDER);
   }

   protected void doConfig()
   {
      setTitle("Painter Conduit - Configuration");
      swap(new SetupPanel());
   }

   protected void doSync()
   {
      setTitle("Painter Conduit - Synchronization");
      swap(new SyncPanel());
   }
}
