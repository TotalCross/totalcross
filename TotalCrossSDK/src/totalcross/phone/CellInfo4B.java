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



package totalcross.phone;

import net.rim.device.api.system.*;
import net.rim.device.api.system.GPRSInfo.*;

public class CellInfo4B
{
   public static String cellId;
   public static String mnc;
   public static String mcc;
   public static String lac;
   public static int signal;
   static CellInfo instance;
   
   public static void update()
   {
      GPRSCellInfo cellInfo = GPRSInfo.getCellInfo();
      int id = cellInfo.getCellId();
      if (id <= 0)
         cellId = mnc = mcc = lac = null;
      else
      {
         cellId = String.valueOf(cellId);
         lac = String.valueOf(cellInfo.getLAC());
         mcc = String.valueOf(cellInfo.getMCC());
         mnc = String.valueOf(cellInfo.getMNC());
      }
   }
   
   protected void finalize()
   {
   }      
}
