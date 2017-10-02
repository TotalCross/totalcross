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

import totalcross.io.DataStream;
import totalcross.io.IOException;

public class ActivationFailure extends ActivationResponse {
  private String reason;

  public ActivationFailure() {
  }

  public ActivationFailure(ActivationRequest request, String reason) {
    super(request);
    this.reason = reason;
  }

  public String getReason() {
    return reason;
  }

  @Override
  protected void read(DataStream ds) throws IOException {
    super.read(ds);
    reason = ds.readString();
  }

  @Override
  protected void write(DataStream ds) throws IOException {
    super.write(ds);
    ds.writeString(reason);
  }
}
