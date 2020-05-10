// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.bytecode;

public class BC192_checkcast extends ByteCode {
  public String targetClass, srcClass;

  public BC192_checkcast() {
    stackInc = 1;
    pcInc = 3;
    targetClass = cp.getString1(readUInt16(pc + 1));
  }

  @Override
  public void exec() {
    srcClass = (String) stack[stackPtr - 1].asObj;
    if (srcClass == null) {
      pcInc = 3;
    } else {
      // check the instance
    }
  }
}
