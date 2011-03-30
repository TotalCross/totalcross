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

public interface DiscoveryListener4B
{
   public static final int INQUIRY_COMPLETED = 0x00;

   public static final int INQUIRY_ERROR = 0x07;

   public static final int INQUIRY_TERMINATED = 0x05;

   public static final int SERVICE_SEARCH_COMPLETED = 0x01;

   public static final int SERVICE_SEARCH_DEVICE_NOT_REACHABLE = 0x06;

   public static final int SERVICE_SEARCH_ERROR = 0x03;

   public static final int SERVICE_SEARCH_NO_RECORDS = 0x04;

   public static final int SERVICE_SEARCH_TERMINATED = 0x02;

   public void deviceDiscovered(RemoteDevice4B btDevice, DeviceClass4B cod);

   public void inquiryCompleted(int discType);

   public void servicesDiscovered(int transID, ServiceRecord4B[] servRecord);

   public void serviceSearchCompleted(int transID, int respCode);
}
