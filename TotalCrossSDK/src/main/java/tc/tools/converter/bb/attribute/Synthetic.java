// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.bb.attribute;

import totalcross.io.DataStream;
import totalcross.io.IOException;

public class Synthetic implements AttributeInfo {
  @Override
  public int length() {
    return 0;
  }

  @Override
  public void load(DataStream ds) throws IOException {
  }

  @Override
  public void save(DataStream ds) throws IOException {
  }
}
