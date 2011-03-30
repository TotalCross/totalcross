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

// $Id: ActivationResponse.java,v 1.4 2011-01-04 13:19:21 guich Exp $

package ras.comm.v1;

import ras.comm.Packet;
import totalcross.io.DataStream;
import totalcross.io.IOException;

public class ActivationResponse extends Packet
{
   protected ActivationRequest request;
   
   public ActivationResponse()
   {
      request = new ActivationRequest();
   }
   
   public ActivationResponse(ActivationRequest request)
   {
      this.request = request;
   }
   
   public ActivationRequest getRequest()
   {
      return request;
   }
   
   protected void read(DataStream ds) throws IOException
   {
      request.read(ds);
   }

   protected void write(DataStream ds) throws IOException
   {
      request.write(ds);
   }
}
