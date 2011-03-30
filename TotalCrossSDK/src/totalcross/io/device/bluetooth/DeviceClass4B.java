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



package totalcross.io.device.bluetooth;

public class DeviceClass4B
{
   javax.bluetooth.DeviceClass nativeInstance;

   public DeviceClass4B(int record) throws IllegalArgumentException
   {
      nativeInstance = new javax.bluetooth.DeviceClass(record);
   }

   DeviceClass4B(javax.bluetooth.DeviceClass nativeInstance)
   {
      this.nativeInstance = nativeInstance;
   }

   public int getMajorDeviceClass()
   {
      return nativeInstance.getMajorDeviceClass();
   }

   public int getMinorDeviceClass()
   {
      return nativeInstance.getMinorDeviceClass();
   }

   public int getServiceClasses()
   {
      return nativeInstance.getMinorDeviceClass();
   }
}
