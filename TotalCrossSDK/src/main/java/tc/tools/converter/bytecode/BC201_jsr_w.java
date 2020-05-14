// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.bytecode;

public class BC201_jsr_w extends Branch {
  public BC201_jsr_w() {
    super(1, readInt32(pc + 1));
    pcInc = 5;
  }

  @Override
  public void exec() {
    stack[stackPtr].asInt = pc + 5;
    pcInc = jumpTo;
  }
}
