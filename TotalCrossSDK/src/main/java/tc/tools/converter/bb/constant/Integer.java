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

package tc.tools.converter.bb.constant;

import totalcross.io.DataStream;
import totalcross.io.IOException;

public class Integer implements ConstantInfo {
  public int value;

  @Override
  public java.lang.String toString() {
    return java.lang.Integer.toString(value);
  }

  @Override
  public int length() {
    return 4;
  }

  @Override
  public void load(DataStream ds) throws IOException {
    value = ds.readInt();
  }

  @Override
  public void save(DataStream ds) throws IOException {
    ds.writeInt(value);
  }
}