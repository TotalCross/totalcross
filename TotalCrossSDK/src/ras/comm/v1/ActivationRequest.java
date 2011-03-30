/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

// $Id: ActivationRequest.java,v 1.10 2011-01-04 13:19:21 guich Exp $
package ras.comm.v1;

import ras.Utils;
import ras.comm.Packet;
import totalcross.io.DataStream;
import totalcross.io.IOException;
import totalcross.util.Hashtable;

public class ActivationRequest extends Packet
{
   protected Hashtable productInfo;
   protected Hashtable deviceInfo;
   protected String activationCode;

   public ActivationRequest()
   {
      productInfo = new Hashtable(10);
      deviceInfo = new Hashtable(10);
      this.webServiceMethod = "activate";
   }

   public ActivationRequest(Hashtable productInfo, Hashtable deviceInfo, String activationCode)
   {
      this.productInfo = productInfo;
      this.deviceInfo = deviceInfo;
      this.activationCode = activationCode;
      this.webServiceMethod = "activate";
   }

   public Hashtable getProductInfo()
   {
      return productInfo;
   }

   public Hashtable getDeviceInfo()
   {
      return deviceInfo;
   }

   public String getActivationCode()
   {
      return activationCode;
   }

   protected void read(DataStream ds) throws IOException
   {
      productInfo.clear();
      Utils.readInfo(ds, productInfo);

      deviceInfo.clear();
      Utils.readInfo(ds, deviceInfo);

      activationCode = ds.readString();
   }

   protected void write(DataStream ds) throws IOException
   {
      Utils.writeInfo(ds, productInfo);
      Utils.writeInfo(ds, deviceInfo);
      ds.writeString(activationCode);
   }
}
