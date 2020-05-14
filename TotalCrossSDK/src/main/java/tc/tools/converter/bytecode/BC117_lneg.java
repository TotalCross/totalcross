// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.bytecode;

public class BC117_lneg extends Arithmetic {
  public BC117_lneg() {
    super(0, -1, -1, LONG);
  }

  @Override
  public void exec() {
    stack[stackPtr - 1].asLong = -stack[stackPtr - 1].asLong;
  }
}
