// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.bytecode;

public class BC173_lreturn extends Return {
  public BC173_lreturn() {
    super(1, -1, LONG);
  }

  @Override
  public void exec() {
    returnValue.asLong = stack[stackPtr - 1].asLong;
    returnValue.type = LONG;
  }
}
