// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.bytecode;

public class BC198_if_null extends ConditionalBranch {
  public BC198_if_null() {
    super(-1, -1, 0, readInt16(pc + 1), OBJECT);
  }

  @Override
  public void exec() {
    if (stack[stackPtr - 1].asObj == null) {
      pcInc = jumpIfTrue;
    } else {
      pcInc = jumpIfFalse;
    }
  }
}
