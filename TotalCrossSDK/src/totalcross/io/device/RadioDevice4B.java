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

// $Id: RadioDevice4B.java,v 1.3 2011-01-04 13:19:21 guich Exp $

package totalcross.io.device;

final public class RadioDevice4B
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

   private RadioDevice4B()
   {
   }

   public static boolean isSupported(int type) throws IllegalArgumentException
   {
      switch (type)
      {
         case WIFI:
            return net.rim.device.api.system.RadioInfo.areWAFsSupported(net.rim.device.api.system.RadioInfo.WAF_WLAN);
         case PHONE:
            return false;
         case BLUETOOTH:
            return false;
         default:
            throw new IllegalArgumentException();
      }
   }

   public static int getState(int type) throws IllegalArgumentException
   {
      switch (type)
      {
         case WIFI:
         {
            if ((net.rim.device.api.system.RadioInfo.getActiveWAFs() & net.rim.device.api.system.RadioInfo.WAF_WLAN) == 0)
               return RADIO_STATE_DISABLED;
            return RADIO_STATE_ENABLED;
         }
         case PHONE:
         {
            int activeWafs = net.rim.device.api.system.RadioInfo.getActiveWAFs();
            if ((activeWafs & net.rim.device.api.system.RadioInfo.WAF_3GPP) == 0
                  && (activeWafs & net.rim.device.api.system.RadioInfo.WAF_CDMA) == 0)
               return RADIO_STATE_DISABLED;
            return RADIO_STATE_ENABLED;
         }
         case BLUETOOTH:
         {
            return RADIO_STATE_DISABLED;
         }
         default:
            throw new IllegalArgumentException();
      }
   }

   public static void setState(int type, int state) throws IllegalArgumentException
   {
      if (state < RADIO_STATE_DISABLED || state > BLUETOOTH_STATE_DISCOVERABLE
            || (type != BLUETOOTH && state > RADIO_STATE_ENABLED))
         throw new IllegalArgumentException();

      switch (type)
      {
         case WIFI:
         {
            if (state == RADIO_STATE_DISABLED)
               net.rim.device.api.system.Radio.deactivateWAFs(net.rim.device.api.system.RadioInfo.WAF_WLAN);
            else
               //if (state == RADIO_STATE_ENABLED)
               net.rim.device.api.system.Radio.activateWAFs(net.rim.device.api.system.RadioInfo.WAF_WLAN);
         }
         break;
         case PHONE:
         {
         }
         break;
         case BLUETOOTH:
         {
         }
         break;
         default:
            throw new IllegalArgumentException();
      }
   }
}
