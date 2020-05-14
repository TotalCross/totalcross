// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.bb.attribute;

import totalcross.io.DataStream;
import totalcross.io.IOException;

public class Generic implements AttributeInfo {
  public byte[] bytes;

  public Generic(int length) {
    bytes = new byte[length];
  }

  @Override
  public int length() {
    return bytes.length;
  }

  @Override
  public void load(DataStream ds) throws IOException {
    ds.readBytes(bytes);
  }

  @Override
  public void save(DataStream ds) throws IOException {
    ds.writeBytes(bytes);
  }
}
