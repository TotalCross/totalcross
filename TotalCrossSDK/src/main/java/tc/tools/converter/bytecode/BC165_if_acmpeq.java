// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.bytecode;

public class BC165_if_acmpeq extends ConditionalBranch {
  public BC165_if_acmpeq() {
    super(-2, -2, -1, readInt16(pc + 1), OBJECT);
  }

  @Override
  public void exec() {
    if (stack[stackPtr - 2].asObj == stack[stackPtr - 1].asObj) {
      pcInc = jumpIfTrue;
    } else {
      pcInc = jumpIfFalse;
    }
  }
}
