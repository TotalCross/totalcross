// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.bytecode;

public class BC143_d2l extends Conversion {
  public BC143_d2l() {
    super(-1, -1, DOUBLE, LONG);
  }

  @Override
  public void exec() {
    stack[stackPtr - 1].asLong = (long) stack[stackPtr - 1].asDouble;
  }
}
