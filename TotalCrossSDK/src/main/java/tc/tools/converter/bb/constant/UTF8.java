// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
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
