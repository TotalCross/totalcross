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

// $Id: CellInfo.java,v 1.5 2011-01-04 13:19:17 guich Exp $

package totalcross.phone;

/** Contains information about the anthena that this cell phone is receiving signal. 
 * Used by the GPS class.
 * @since TotalCross 1.22
 */

public class CellInfo
{
   public static String cellId;
   public static String mnc;
   public static String mcc;
   public static String lac;
   public static int signal;
   
   static CellInfo instance = new CellInfo();
   
   private CellInfo()
   {
      loadResources();
   }
   
   public static void update()
   {
   }
   public native static void update4D();

   private void loadResources()
   {
   }
   native void loadResources4D();

   private void releaseResources()
   {
   }
   native void releaseResources4D();

   protected void finalize()
   {
      releaseResources();
   }   
}
