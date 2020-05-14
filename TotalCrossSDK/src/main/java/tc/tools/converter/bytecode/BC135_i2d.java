// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.bytecode;

public class BC135_i2d extends Conversion {
  public BC135_i2d() {
    super(-1, -1, INT, DOUBLE);
  }

  @Override
  public void exec() {
    stack[stackPtr - 1].asDouble = (double) stack[stackPtr - 1].asInt;
  }
}
