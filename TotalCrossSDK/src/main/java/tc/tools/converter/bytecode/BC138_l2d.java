// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.bytecode;

public class BC138_l2d extends Conversion {
  public BC138_l2d() {
    super(-1, -1, LONG, DOUBLE);
  }

  @Override
  public void exec() {
    stack[stackPtr - 1].asDouble = (double) stack[stackPtr - 1].asLong;
  }
}
