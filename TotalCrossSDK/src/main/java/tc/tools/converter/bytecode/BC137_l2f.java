// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.bytecode;

public class BC137_l2f extends Conversion {
  public BC137_l2f() {
    super(-1, -1, LONG, FLOAT);
  }

  @Override
  public void exec() {
    stack[stackPtr - 1].asDouble = (float) stack[stackPtr - 1].asLong;
  }
}
