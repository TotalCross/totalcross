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

  @Override
  protected void read(DataStream ds) throws IOException
  {
    request.read(ds);
  }

  @Override
  protected void write(DataStream ds) throws IOException
  {
    request.write(ds);
  }
}
