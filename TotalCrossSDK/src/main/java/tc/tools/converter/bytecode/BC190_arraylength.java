// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.bytecode;

import tc.tools.converter.TCValue;

public class BC190_arraylength extends ByteCode {
  public int resultPos, objPos;

  public BC190_arraylength() {
    objPos = resultPos = -1;
    stackInc = 0;
  }

  @Override
  public void exec() {
    TCValue v = stack[stackPtr - 1];
    if (!"array".equals(v.asObj)) {
      System.out.println("Not an array!");
    }
    stack[stackPtr - 1].asInt = v.asInt;
  }
}
