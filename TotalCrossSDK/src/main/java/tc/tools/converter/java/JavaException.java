// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.java;

import totalcross.io.DataStream;

public final class JavaException {
  public int startPC, endPC, handlerPC;
  public String catchType;

  public JavaException(DataStream ds, JavaConstantPool cp) throws totalcross.io.IOException {
    startPC = ds.readUnsignedShort();
    endPC = ds.readUnsignedShort();
    handlerPC = ds.readUnsignedShort();
    int c = ds.readUnsignedShort();
    if (c > 0) {
      catchType = cp.getString1(c);
    }
  }

  public boolean isFinallyHandler() {
    return catchType == null;
  }
}
