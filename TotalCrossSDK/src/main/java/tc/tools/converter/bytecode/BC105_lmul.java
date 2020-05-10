// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.bytecode;

public class BC105_lmul extends Arithmetic {
  public BC105_lmul() {
    super(-1, -2, -1, LONG);
  }

  @Override
  public void exec() {
    stack[stackPtr - 2].asLong *= stack[stackPtr - 1].asLong;
  }
}
