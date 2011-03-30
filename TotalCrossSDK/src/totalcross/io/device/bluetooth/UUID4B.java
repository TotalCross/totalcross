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

// $Id: UUID4B.java,v 1.5 2011-01-04 13:18:57 guich Exp $

package totalcross.io.device.bluetooth;

public class UUID4B
{
   javax.bluetooth.UUID nativeInstance;
   static final String SHORT_UUID_BASE = "00001000800000805F9B34FB";

   public UUID4B(long uuidValue)
   {
      this.nativeInstance = new javax.bluetooth.UUID(uuidValue);
   }

   public UUID4B(String uuidValue, boolean shortUUID)
   {
      try
      {
         this.nativeInstance = new javax.bluetooth.UUID(uuidValue, shortUUID);
      }
      catch (NumberFormatException e)
      {
         throw new IllegalArgumentException(e.getMessage());
      }
   }

   public boolean equals(Object value)
   {
      return nativeInstance.equals(value);
   }

   public int hashCode()
   {
      return nativeInstance.hashCode();
   }

   public String toString()
   {
      return nativeInstance.toString();
   }
}
