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


public class ServiceRecord4B
{
   private RemoteDevice4B hostDevice;
   Object nativeInstance;
   
   public static final int NOAUTHENTICATE_NOENCRYPT = 0x00;
   public static final int AUTHENTICATE_NOENCRYPT = 0x01;
   public static final int AUTHENTICATE_ENCRYPT = 0x02;   

   ServiceRecord4B()
   {
   }

   ServiceRecord4B(Object nativeInstance)
   {
      this.nativeInstance = nativeInstance;
   }
   
   public RemoteDevice4B getHostDevice()
   {
      if (hostDevice == null)
      {
         javax.bluetooth.RemoteDevice device = ((javax.bluetooth.ServiceRecord) nativeInstance).getHostDevice();
         if (device != null)
            hostDevice = new RemoteDevice4B(device);
      }
      return hostDevice;
   }
   
   public DataElement getAttributeValue(int attrID)
   {
      if (attrID < 0x0000 || attrID > 0xffff)
         throw new IllegalArgumentException();
      
      //TODO: FIX THIS!
      ((javax.bluetooth.ServiceRecord) nativeInstance).getAttributeValue(attrID);
      return null;
   }   

   public String getConnectionURL()
   {
      return ((javax.bluetooth.ServiceRecord) nativeInstance).getConnectionURL(javax.bluetooth.ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
   }
   
   public String getConnectionURL(int requiredSecurity, boolean mustBeMaster)
   {
      return ((javax.bluetooth.ServiceRecord) nativeInstance).getConnectionURL(requiredSecurity, mustBeMaster);
   }
}
