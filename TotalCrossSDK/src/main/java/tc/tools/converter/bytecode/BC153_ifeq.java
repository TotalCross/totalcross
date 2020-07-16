// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.bytecode;

public class BC153_ifeq extends ConditionalBranch {
  public BC153_ifeq() {
    super(-1, -1, 0, readInt16(pc + 1), INT);
  }

  @Override
  public void exec() {
    if (stack[stackPtr - 1].asInt == 0) {
      pcInc = jumpIfTrue;
    } else {
      pcInc = jumpIfFalse;
    }
  }
}
