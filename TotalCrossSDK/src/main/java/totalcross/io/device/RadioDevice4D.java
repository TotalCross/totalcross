/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
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



package totalcross.io.device;

final public class RadioDevice4D
{
   // types
   public static final int WIFI = 0;
   public static final int PHONE = 1;
   public static final int BLUETOOTH = 2;

   // generic states
   public static final int RADIO_STATE_DISABLED = 0;
   public static final int RADIO_STATE_ENABLED = 1;

   // bluetooth states
   public static final int BLUETOOTH_STATE_DISCOVERABLE = 2;

   private RadioDevice4D()
   {
   }

   native public static boolean isSupported(int type) throws IllegalArgumentException;

   native public static int getState(int type) throws IllegalArgumentException;

   native public static void setState(int type, int state) throws IllegalArgumentException;
}
