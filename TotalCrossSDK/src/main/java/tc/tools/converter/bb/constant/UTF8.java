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

public class UTF8 implements ConstantInfo {
  public java.lang.String value;

  @Override
  public java.lang.String toString() {
    return value;
  }

  @Override
  public int length() {
    return 2 + value.length();
  }

  @Override
  public void load(DataStream ds) throws IOException {
    value = ds.readString();
  }

  @Override
  public void save(DataStream ds) throws IOException {
    ds.writeString(value);
  }
}
