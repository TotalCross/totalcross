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

import totalcross.io.DataStream;
import totalcross.io.IOException;

public class Hello extends Packet {
  private int version;

  public Hello() {
  }

  public Hello(int version) {
    this.version = version;
  }

  public int getVersion() {
    return version;
  }

  @Override
  protected void read(DataStream ds) throws IOException {
    version = ds.readInt();
  }

  @Override
  protected void write(DataStream ds) throws IOException {
    ds.writeInt(version);
  }
}
