// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.bytecode;

public class BC136_l2i extends Conversion {
  public BC136_l2i() {
    super(-1, -1, LONG, INT);
  }

  @Override
  public void exec() {
    stack[stackPtr - 1].asInt = (int) stack[stackPtr - 1].asLong;
  }
}
