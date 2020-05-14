// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.bytecode;

public class BC017_sipush extends LoadLocal {
  public BC017_sipush() {
    super(readInt16(pc + 1), INT);
    pcInc = 3;
  }

  @Override
  public void exec() {
    stack[stackPtr].asInt = localIdx;
  }
}
