/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/



package ras.comm;

import ras.comm.v1.ActivationFailure;
import ras.comm.v1.ActivationRequest;
import ras.comm.v1.ActivationSuccess;
import totalcross.io.DataStream;
import totalcross.io.IOException;
import totalcross.util.Hashtable;

public abstract class Packet
{
   static final Hashtable packetClasses = new Hashtable(10);
   protected String webServiceMethod;
   
   // Register packets
   static
   {
      packetClasses.put("ras.comm.Hello", Hello.class);
      packetClasses.put("ras.comm.v1.ActivationRequest", ActivationRequest.class);
      packetClasses.put("ras.comm.v1.ActivationFailure", ActivationFailure.class);
      packetClasses.put("ras.comm.v1.ActivationSuccess", ActivationSuccess.class);
   }
   
   protected abstract void read(DataStream ds) throws IOException;
   protected abstract void write(DataStream ds) throws IOException;
}
