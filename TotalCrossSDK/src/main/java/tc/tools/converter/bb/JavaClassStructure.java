// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.bb;

import totalcross.io.DataStream;
import totalcross.io.IOException;

public interface JavaClassStructure {
  public int length();

  public void load(DataStream ds) throws IOException;

  public void save(DataStream ds) throws IOException;
}
