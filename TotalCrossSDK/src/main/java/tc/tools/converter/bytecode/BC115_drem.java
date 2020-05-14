// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.bytecode;

public class BC115_drem extends Arithmetic {
  public BC115_drem() {
    super(-1, -2, -1, DOUBLE);
  }

  @Override
  public void exec() {
    stack[stackPtr - 2].asDouble %= stack[stackPtr - 1].asDouble;
  }
}
