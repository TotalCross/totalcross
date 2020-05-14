// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.bytecode;

public class BC168_jsr extends Branch {
  public BC168_jsr() {
    super(1, readInt16(pc + 1));
    pcInc = 3;
  }

  @Override
  public void exec() {
    stack[stackPtr].asInt = pc + 3;
    pcInc = jumpTo;
  }
}
